# Evaluation Checklist

## Correctness & Edge Cases
- Core logic is correct for the *stated* semantics (e.g. token-bucket refill math, sliding-window boundaries) — not just correct for the one example the author happened to try.
- Boundary and adversarial inputs handled: zero/negative values, clock skew or time going backwards, integer overflow on long-running counters, empty/null inputs.
- If the component is meant to be shared across threads (a rate limiter is, by definition), concurrent access preserves the same guarantees as single-threaded use — no lost updates, no double-counting, no torn reads.
- Off-by-one errors in window boundaries are the classic bug class here (is the window `[start, end)` or `[start, end]`? — checked explicitly, not assumed).

## API Design & Contracts
- The public interface expresses intent (method/parameter names), not implementation details (callers shouldn't need to know it's a bucket vs a log vs a counter).
- What happens at the limit is a deliberate decision — exception, boolean return, blocking/wait — not whatever the first draft happened to do.
- Configuration (limit, window size, burst allowance) is externalized and validated at the boundary, not a hardcoded magic number buried in logic.
- If multiple strategies are plausible (token bucket, sliding window, fixed window), the design allows swapping the strategy without changing caller code — *if* the exercise's scope calls for that; not a mandatory abstraction otherwise.

## Testing Rigor
- Tests cover the boundary conditions (exactly at the limit, one over, window edges), not just a comfortably-under-the-limit happy path.
- If the component is concurrent, tests actually exercise concurrent access (multiple threads hammering the same key) — not just sequential calls that happen to run on one thread.
- Timing-sensitive behavior is tested deterministically — an injectable/fake clock, not `Thread.sleep` plus hope. A test suite built on real wall-clock sleeps is a flakiness time bomb, not rigor.
- Tests would actually fail if the core algorithm regressed — not just exercise the API surface without asserting the interesting behavior.

## Error Handling & Failure Modes
- Invalid configuration (negative rate, zero window) is rejected at construction/boundary, not silently accepted and misbehaving later.
- No silent over-permitting or under-permitting under error conditions (e.g. an exception mid-check that leaves internal state inconsistent).

## Performance & Complexity
- Per-request cost is appropriate for a hot-path component — an approach that's O(n) per check where n grows unboundedly (e.g. an unpruned request log) is a real defect here, not a nitpick.
- Memory is bounded per key/caller over time — old entries/state get pruned or expire, rather than accumulating forever.

## Code Quality & Maintainability
- Rate-limiting (or equivalent core) logic isn't tangled with unrelated concerns (logging, HTTP-layer specifics, metrics emission mixed directly into the algorithm).
- Comments/naming explain non-obvious *why* (a specific boundary choice, a subtle race avoided) — not restating what the code already says.
- No near-duplicate logic copy-pasted across strategies/branches that should share a common implementation.

## Level Signals — call out concretely, don't just assert a level

**Staff-level signals** (impact beyond the immediate exercise):
- Reasons about this being used as a shared dependency by other callers, not just the test harness in front of it.
- States the concurrency/thread-safety contract explicitly (e.g. in a doc comment) rather than leaving it to be reverse-engineered from the implementation.
- Names the actual algorithmic tradeoff made (token bucket vs. sliding window vs. fixed window) and why it fits this use case, rather than picking one silently.
- Considers operational needs: exposing allowed/rejected counts, or making limits tunable without a redeploy.
- Distinguishes "what this needs to do for this exercise" from "what a production version would also need" — shows sequencing judgment, not just the end state.

**Principal-level signals** (impact beyond this component):
- Frames the algorithm choice against a product/business constraint (burst tolerance vs. strict fairness) rather than "this is the well-known algorithm for this problem."
- Names the next real scaling boundary even if out of scope here — e.g. this is in-memory/single-node; multi-node correctness would need a shared store (Redis) with its own consistency tradeoffs — and says so explicitly rather than letting the single-node assumption go unstated.
- Considers whether this is built as a reusable, general-purpose piece other exercises/teams could import, vs. a one-off tied to this specific test harness.

## Red Flags (regardless of level)
- A component that's inherently concurrent (shared rate limiter) but was only ever tested/reasoned about single-threaded.
- Wall-clock time (`System.currentTimeMillis()`/`Instant.now()`) called directly inside core logic with no injectable clock — makes correctness at boundaries untestable deterministically.
- Off-by-one at a window boundary, caught only by re-deriving the boundary by hand, not by a test that targets it.
- Tests that exercise the API surface (call it, assert it doesn't throw) without asserting the actual behavior that makes this problem hard.
