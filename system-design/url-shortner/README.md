# Problem statement

Design Url shortener app

## Scope and Requirements

* Expiration : assuming url will never expire
* Skew load : traffic across URLs is skewed (a small number of URLs — e.g. a viral link — get most of the reads). This does NOT mean the platform's total QPS depends on per-URL uniformity; it means the *distribution* of that QPS across URLs is uneven, which is a caching/hot-key concern for the Scalability section, not a scope-math concern here.
* Rate limiting : out of scope

## Functional requirements

* Client will come to platform and generate short url against the original url
* Customer will hit the short url and platform will redirect them to original url
* Custom alias (vanity URLs): in scope
* Deactivate a link: in scope
* Redirect type: 301 (permanent). Tradeoffs accepted with this choice:
  * No reliable click analytics — a 301 is cached by the browser/CDN, so repeat visits from the same client resolve locally and never reach our server. We can at best undercount (first-hit-per-client only), not track true click volume.
  * Deactivation is not guaranteed for clients that already cached the 301 — they'll keep redirecting to the old destination locally, unaware the link was deactivated server-side, until their cache expires.
  * Changing a short code's redirect target later is unreliable for the same caching reason.
  * Accepted because: fewer repeat hits reach our servers, which lowers real read load below the raw "click" count — cheaper to run at the scale we're targeting. If accurate analytics become a hard requirement later, revisit with 302 (or 301 + a separate analytics beacon).

## Quantified scale

* Writes: assume 1,000 new short URLs created per day.
* Reads: assume platform-wide average read QPS = 100,000 QPS. This is a *platform-wide average across all URLs combined* — it does not assume each URL gets equal traffic (see skew note above); it's just total reads/sec, however that load happens to be distributed.
* Read:write ratio (derived, stated explicitly): daily reads = 100,000 QPS × 86,400 s/day ≈ 8.64 × 10⁹ reads/day, vs 1,000 writes/day → read:write ≈ 8.64 × 10⁶ : 1. That's an extreme, very-viral-scale ratio (bit.ly-tier).
* Storage:
  * Assuming MySQL, single row = short url + original url + metadata ≈ 1 KB
  * Daily storage: 1 KB × 1,000 rows/day ≈ 1 MB/day
  * Yearly storage: 1 MB × 365 ≈ 365 MB/year
  * 10 years: 365 MB × 10 ≈ 3.65 GB
  * Simplification: ignores index overhead and assumes fixed row size.

## API endpoints

1. `POST /urls`
   * Header: Auth token — open, deferred, not blocking the rest of the design
   * Request body: `{ "url": "www.test.com/....", "customAlias": "optional-string" }`
   * Response body: `{ "shortUrl": "" }`, or `409 Conflict` if `customAlias` is already taken
2. `GET /{shortUrl}` — no `/urls` prefix. The redirect path IS the short code; a `/urls/` prefix wastes characters on the one URL that's supposed to be short.
3. `PATCH /urls/{shortUrl}` with body `{ "isActive": false }` — renamed from DELETE, since this is a soft deactivation (flips `isActive`), not row removal. The row and its history stay queryable/recoverable.

## Short code generation

### Option 1: Hash generation

* Problem: collision can happen

### Option 2: Base62-encoded auto-increment counter (Chosen option)

* Rejected ULID: 26 characters is not a short URL — defeats the requirement.
* Rejected "ties to MySQL" as a reason to avoid the counter: at 1,000 writes/day, a single auto-increment source isn't a bottleneck. The earlier objection was a generic distributed-systems instinct that doesn't apply at this stated scale.
* 6 base62 chars (`[0-9a-zA-Z]`) = 62⁶ ≈ 56.8 billion combinations, against ~3.65M codes generated over 10 years at this write rate — comfortably sufficient, no collision handling needed since the counter guarantees uniqueness.
* Custom alias uses the same `shortUrl` column/namespace; uniqueness enforced by a DB unique constraint, checked at insert time — collision surfaces as `409 Conflict` on the API (see above).

## Data model

* `shortUrl` (PK, base62 code or custom alias), `longUrl`, `createdAt`, `updatedAt`, `isActive`
* No `expiresAt` — matches the "never expires" assumption in Scope.
* Owner/creator field deferred along with the open auth question above — add `creatorId` if/when auth is resolved, needed to authorize who can deactivate a given link.

## Major components

* Write path: `Client → GW → ShortUrl Service → DB` (generates base62 code from counter, or validates+inserts custom alias)
* Read path: `Client → CDN/edge cache → ShortUrl Service → Redis (cache-aside) → DB (on miss)`
  * CDN/edge layer added: since redirects are 301 (permanent/cacheable), most repeat traffic from a given client resolves at the edge or in the browser and never reaches our infrastructure at all — this is the direct payoff of the 301 decision, and the main reason the real request volume hitting the service is far below the raw 8.64B/day click count.
  * Redis cache-aside: on deactivation (`PATCH`), explicitly invalidate/delete the Redis entry so the change takes effect promptly rather than waiting out a TTL — a TTL-only approach would let deactivated links keep resolving from cache for up to the TTL window.

### Why MySQL

The access pattern itself (`shortUrl → longUrl`, single-key lookup, no joins) doesn't require relational structure — a key-value store would fit that alone. MySQL is chosen instead because:

* Custom alias needs a straightforward uniqueness guarantee at write time — a unique index + insert-time constraint is simple and well-understood in a relational DB.
* MySQL only needs to serve cache misses + 1,000 writes/day, not the raw 100K QPS average — Redis (plus the CDN layer above) absorbs nearly all read traffic, which is what actually makes MySQL viable here despite not being the "natural" fit for pure key-value access.
* Master-follower replication handles the read-miss + write split; it doesn't need to defend against hot-key skew directly, since hot keys are cache hits, not DB hits.

### Scalability & Performance

* **Peak vs average**: average platform QPS is 100K (from Quantified Scale). Assume peak = 5x average during a viral spike ≈ 500,000 QPS raw clicks/sec. All capacity numbers below are checked against peak, not average.

* **301 traffic-reduction, quantified — and where it breaks down**: baseline assumption is that ~90% of daily clicks are repeat visits absorbed at the browser/CDN edge, so only ~10% of raw clicks reach our origin during steady state (origin QPS ≈ 10% × 100K ≈ 10,000 QPS avg). *However*, a viral spike is dominated by distinct first-time visitors, not repeat clicks from the same person — so edge caching provides little protection during exactly the scenario we're worried about. Worst-case assumption during a spike: ~80% first-touch ⇒ origin QPS during spike ≈ 80% × 500,000 = 400,000 QPS. 301 caching and hot-key risk are two different problems and shouldn't be conflated — 301 helps steady-state repeat load, not viral spikes.

* **Hot-key check (the skew assumption from Scope, resolved here)**: stated skew model — assume the single hottest URL can account for up to 5% of platform traffic during a spike. At peak origin QPS of 400,000, that's ≈ 20,000 QPS hitting one Redis key. A single-threaded Redis node handles roughly 100K-150K ops/sec in practice, so 20K QPS on one key has ~5-7x headroom — fine at this stated scale. If the skew concentration or peak multiplier were higher (e.g. one URL taking 20%+ of peak traffic), this would exceed a single node's ceiling — since Redis Cluster shards by key, more nodes don't help a single hot key. The escalation lever in that case: an in-process L1 cache in the ShortUrl Service for the top-N hottest keys, ahead of the Redis hop, to shave that specific key's QPS before it reaches the network at all. Not needed at current numbers, but naming the lever for when the assumption changes.

* **Cache sizing — the whole dataset fits in memory**: total 10-year storage is ~3.65 GB (from Quantified Scale). That comfortably fits entirely in a modestly-sized Redis cluster's RAM — not just a hot subset. Implication: instead of pure cache-aside with LRU eviction, warm the entire dataset (e.g. backfill on deploy, plus write-through on creation) so effectively every read is a cache hit by construction. The only real "miss" case is the brief window between a new URL being written and its cache entry being populated — handled by write-through on creation rather than waiting for a read-triggered fill.

* Write traffic (1,000/day) doesn't need partitioning; DB partitioning by short code remains available if write volume grows by orders of magnitude.

* Read traffic is served from Redis at low-single-digit-millisecond latency; DB is only in the path for the write-through on creation and the rare fallback described above.

* Redundancy (Redis replicas, MySQL replicas) for instance failure is a Reliability & Consistency concern — carried forward, addressed in that section rather than duplicated here.

## Reliability & Consistency

**Consistency model** — explicit, not assumed:

* MySQL is the strongly-consistent source of truth for `shortUrl → longUrl` mappings; a single primary handles all writes (creation, deactivation).
* Redis is an eventually-consistent cache of that source of truth — but because we write-through on creation and explicitly invalidate on deactivation (Scalability section), staleness in practice is bounded to "as fast as the invalidation call completes," not a lazy TTL window. This is closer to read-your-writes for both paths than to loose eventual consistency.
* A stale MySQL follower (replication lag) can only affect the rare cache-miss fallback path, since Redis serves the vast majority of reads — bounded, low-blast-radius staleness.

**Failure modes, per component**:

| Component | Failure | Effect | Mitigation |
|---|---|---|---|
| CDN/edge | Down | Traffic falls back to origin directly — sudden load spike on GW/Service | Origin tier sized with headroom for this (ties to the 5-7x hot-key headroom already established); CDN should have multi-region PoPs so a single-PoP failure doesn't take all edge caching down at once |
| API Gateway / LB | Instance down | Requests to that instance fail | Redundant, health-checked GW/LB instances — no single instance is load-bearing |
| ShortUrl Service | Instance down | Requests routed to it fail | Stateless service — horizontal scaling + health checks + LB removes it from rotation; no data loss since no state lives in the service itself |
| Redis primary | Down | Reads fall back to MySQL follower directly (existing fallback path) — origin load spikes toward the ~400K peak QPS case from Scalability until Redis recovers | Redis replica promoted automatically (Sentinel/Cluster failover); dataset is small (~3.65 GB) so a promoted replica or a cold rebuild-from-MySQL warms fast |
| Redis replica lag/partition | Stale reads from a lagging replica | A just-deactivated link could briefly still resolve as active from a stale replica | Acceptable bounded staleness; deactivation is not a safety-critical guarantee here (no requirement stated that demands instant global consistency) |
| MySQL primary | Down | All writes (creation, deactivation) fail until failover | Managed HA (e.g. automatic primary promotion, ~30-60s typical failover); low blast radius since writes are only ~1,000/day — a short write outage is tolerable, not an emergency |
| MySQL follower | Down | Cache-miss/write-through fallback path degrades to remaining followers | Multiple followers; not critical since Redis absorbs nearly all read load |

**Redundancy / failure domain**: assuming multi-AZ within a single region for both MySQL (primary + follower across AZs) and Redis (primary + replica across AZs) — no stated requirement for a globally-distributed user base, so multi-region is explicitly out of scope for now. If low-latency access from multiple continents became a requirement, this would need multi-region read replicas plus geo-routing at the CDN/DNS layer — naming it as the lever, not building it now.

**Idempotency**:

* `GET /{shortUrl}` (redirect) is a pure read — naturally idempotent, no concern.
* `PATCH /urls/{shortUrl}` (deactivation) is naturally idempotent — setting `isActive: false` twice has no different effect than once.
* `POST /urls` (creation) is the one at-least-once risk: if a client retries after a timeout without an idempotency key, a default (non-custom-alias) request mints a *second, different* short code for the same long URL on retry — wasteful but not a correctness bug (each code is independently valid). Custom-alias retries are accidentally protected by the unique constraint (retry hits `409` rather than duplicating). Recommend an `Idempotency-Key` header on `POST /urls` so retries return the original short code instead of minting a new one.

## Depth on the Hard Part

For this system, the genuinely hard sub-problem isn't storage or CRUD — it's **surviving a single URL going viral without violating latency, given a cache architecture that shards by key**. Short-code generation and cache sizing turned out to be simple once checked against the actual numbers (Short Code Generation, Scalability); this is the one place worth going a level deeper than the architecture diagram.

* **Detection, not a static threshold**: rather than permanently over-provisioning for the worst case, track per-key request rate at the Service layer with a sliding-window counter. When a single key crosses a threshold set with headroom below a Redis node's ceiling (e.g. ~30K QPS, against the ~100-150K single-node ceiling established in Scalability), promote that key into an in-process local cache (e.g. Caffeine) on each Service instance — shaving that key's traffic before it ever reaches the Redis network hop.
* **The subtlety this raises**: the keys most likely to need *urgent* deactivation (an abusive or infringing link) are, by nature, exactly the keys most likely to be under a viral spike and therefore promoted to local, per-instance caches. A TTL-only local cache would mean a "deactivated" link keeps resolving from scattered per-instance caches for the length of that TTL — undermining the one case where fast deactivation matters most.
* **Resolution**: deactivation (`PATCH`) publishes an invalidation event over Redis Pub/Sub (or an equivalent lightweight broadcast) to all Service instances, not just the shared Redis entry. Broadcasting to on the order of tens of instances is sub-second, so even with local-cache promotion active, deactivation propagation stays in the low-seconds range — the tradeoff (added invalidation-plumbing complexity) is accepted specifically because it closes the gap the naive local-TTL-cache approach would leave open.

## Tradeoffs

| Decision | Alternative(s) rejected | Why (tied to requirements/cost) |
|---|---|---|
| Redirect: 301 permanent | 302 temporary | 302 preserves click analytics and instant deactivation propagation, but hits origin on every click. Rejected 302 because origin cost at ~8.64B raw clicks/day would be far higher; accepted losing analytics/instant-deactivation as the price (Functional Requirements) |
| Short code: base62 counter | ULID; hash-of-URL | ULID (26 chars) fails the "short" requirement outright. Hash risks collisions needing detection/retry logic. Counter is simplest and collision-free, and the "ties to MySQL" objection doesn't hold at 1,000 writes/day |
| Datastore: MySQL | Pure key-value store (DynamoDB/Cassandra) | Access pattern (single-key lookup) is a natural KV fit, not relational — but custom alias needs a simple write-time uniqueness constraint, and write volume (1,000/day) never needs KV-style write scaling. MySQL costs less to operate here than standing up a second datastore for a benefit we don't need |
| Cache: fully-warmed, write-through | Cache-aside with LRU eviction | Entire 10-year dataset is ~3.65 GB — cheap to hold in full. Paying a small, fixed memory cost buys away all miss-path/eviction complexity |
| Consistency: single MySQL primary | Multi-primary / distributed-strong writes | Write volume is trivial (~0.01 QPS avg) — multi-primary complexity buys nothing here. Traded a theoretical SPOF for fast automatic failover (~30-60s), acceptable given the low blast radius |
| Hot-key mitigation: dynamic L1 promotion | Static over-provisioning of Redis/Service capacity | Viral spikes are rare and bursty — paying for standing capacity to cover the worst case 24/7 is the more expensive option; dynamic promotion only pays the (invalidation-plumbing) cost when actually needed |
| Region: single-region, multi-AZ | Multi-region | No stated requirement for a globally-distributed user base; multi-region adds cost/complexity (cross-region replication, geo-routing) that isn't justified yet — named as the lever if that requirement appears |

## Operational Concerns

* **Rollout**: greenfield system, nothing to migrate off of. Still worth rolling out progressively rather than switching 100% of traffic on day one — canary a small percentage of creation/redirect traffic through the new infra, watch error rate and latency, ramp up. Not a hard requirement here, but a cheap way to catch a bad deploy before it's system-wide.

* **Monitoring signals** (the specific thing that tells you this system is failing, not "add monitoring"):
  * Redis hit ratio — expected near 100% given the fully-warmed cache strategy; a sustained drop means the "whole dataset fits in memory" assumption has stopped holding (dataset grew past capacity, or an instance lost its warm state) and needs re-checking.
  * Per-key request rate at the Service layer — the exact signal that triggers L1-cache promotion in the hot-key deep dive above; also useful on its own as an early warning of an in-progress viral spike.
  * Origin QPS actually reaching the Service tier, compared against the modeled baseline (~10K avg / ~400K peak from Scalability) — sustained deviation means the "90% absorbed at the edge" assumption is wrong and capacity planning needs revisiting.
  * MySQL primary write latency/error rate — the signal for an in-progress failover.
  * `409` rate on `POST /urls` — a spike suggests alias-squatting or abuse patterns worth a closer look, not just noise.

* **Versioning / backward compatibility**: the redirect path (`GET /{shortUrl}`) intentionally carries zero prefix or version — it's the literal short link handed to end users and must stay short forever, so it can never gain a `/v1/` prefix later without breaking every link already issued. Management endpoints (`POST /urls`, `PATCH /urls/{shortUrl}`) are not public/short-lived like the redirect link, so they can safely carry a `/v1/` prefix for future evolution — worth adding now (`/v1/urls`) precisely because it's free today and expensive to retrofit later.
