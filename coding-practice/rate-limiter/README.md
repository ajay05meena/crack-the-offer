# Rate Limiter

Per-user rate limiting algorithms, each implementing `RateLimiter#tryAcquire(String userId)`.

| Algorithm | Class | Burst handling | Memory per user | Smoothing | Notes |
|---|---|---|---|---|---|
| Sliding Window Log | `SlidingWindowRateLimiter` | Accurate, no edge bursts | O(n) — one entry per request in window | Exact | Most accurate, highest memory cost |
| Time Bucketed | `TimeBucketedRateLimiter` | Same edge-burst issue as fixed window | O(1) per active bucket | None | Naive fixed window keyed by minute |
| Token Bucket | `TokenBucketRateLimiter` | Allows short bursts up to capacity | O(1) | Gradual (rate-based refill) | Refills continuously, not in discrete windows |
| Leaky Bucket | `LeakyBucketRateLimiter` | No bursts above capacity | O(1) | Gradual (rate-based leak) | Smooths traffic to a steady outflow rate |
