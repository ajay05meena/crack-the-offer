# Evaluation Checklist

## Scope & Requirements
- Functional requirements stated explicitly, not inferred implicitly from the design.
- Non-functional requirements quantified: expected scale (DAU/QPS/data volume), latency targets, consistency needs, availability target (99.9% vs 99.99% changes the design).
- Explicit statement of what's *out of scope* — a doc that tries to solve everything is a mid-level tell, not a thorough one.
- Clarifying assumptions are named (e.g. "assuming read:write ratio of 100:1") rather than silently baked into the design.

## High-Level Architecture
- Major components and the data flow between them are identifiable from the doc (diagram or clear prose).
- API/interface boundaries are defined where two components disagree on ownership.
- Data model matches the access patterns actually described (e.g. don't propose a relational join-heavy schema for a system whose bottleneck is single-key lookups at scale).
- Justifies technology choices against the stated requirements, not by default/familiarity (e.g. "Postgres because we need multi-row transactions here," not just "Postgres").

## Scalability & Performance
- Back-of-envelope math present for at least the dimension that actually matters (storage growth, request rate, fan-out) — not obligatory arithmetic for its own sake.
- Identifies the actual bottleneck (the thing that breaks first as load grows), not a generic "add more servers."
- Caching, sharding, or partitioning strategy is tied to a specific access pattern, not bolted on as a buzzword.
- Addresses hot-key / hot-partition risk if the access pattern implies skew (celebrity user, trending item, etc.).

## Reliability & Consistency
- Failure modes named for each major component (what happens when it's down/slow/partitioned).
- Consistency model stated explicitly (strong vs eventual) and justified by the use case, not assumed.
- Redundancy/replication strategy addresses the actual failure domain (single-AZ vs multi-region).
- Idempotency / retry / dedup addressed wherever the design has at-least-once delivery or client retries.

## Depth on the Hard Part
- The doc identifies which sub-problem is actually the hard one for *this* system (not a generic template) and spends disproportionate depth there.
- The deep dive resolves the tradeoff it raises, rather than listing options and moving on.
- Numbers, not just architecture, back up the deep dive's chosen approach (e.g. actual latency budget breakdown, not just "this should be fast").

## Tradeoffs
- At least one credible alternative is named and explicitly rejected with a reason — not just the chosen path presented as the only option.
- Tradeoffs are framed against the stated requirements (why this system's specific constraints tip the tradeoff one way), not generic pros/cons.
- Cost is treated as a first-class tradeoff axis alongside performance/complexity, at least at Staff+.

## Operational Concerns
- Rollout/migration path addressed if this replaces or coexists with an existing system.
- Monitoring/alerting: names the signal that would tell you this system is failing, not just "add monitoring."
- Backward compatibility / versioning addressed if the API is client-facing.

## Level Signals — call out concretely, don't just assert a level

**Staff-level signals** (impact beyond a single team/service):
- Reasons about how this system interacts with or constrains *other* teams' systems, not just its own internals.
- Names an existing org convention/standard and either follows it deliberately or explains why deviating is worth it.
- Considers migration/rollout risk for people other than the author (on-call load, other teams' integration work).
- Distinguishes "what I'd ship v1" from "what I'd do at 10x scale" — shows judgment about sequencing, not just the end state.
- Mentions how the design would be validated/rolled out incrementally (canary, shadow traffic, staged migration).

**Principal-level signals** (impact beyond the immediate problem, org- or company-wide framing):
- Frames the decision in terms of build-vs-buy or vendor/platform choice with an explicit cost/risk argument, not just technical preference.
- Considers second-order organizational consequences (this design shapes what other teams will build on top of it for years).
- Explicitly ties the design decision to a business or strategic constraint (cost at scale, time-to-market, team's ability to operate it long-term), not purely technical elegance.
- Proposes or references a reusable pattern/standard other teams could adopt, rather than a one-off solution.
- Shows awareness of organizational/political feasibility — who needs to buy in, what's the path to get there — not just what's technically correct.

## Red Flags (regardless of level)
- Diagram/prose contradicts itself (e.g. claims strong consistency, then describes an async replication path with no reconciliation).
- Numbers that don't add up (stated QPS inconsistent with stated storage growth over the stated retention window).
- No mention of failure at all — a design that only describes the happy path.
- Copy-paste template structure (requirements/HLD/deep-dive/tradeoffs headers present) but each section is generic boilerplate with no numbers specific to *this* system.
