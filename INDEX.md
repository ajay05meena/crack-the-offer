# Index

Map of what's actually in this repo. Update this when you add a problem, exercise, doc, or skill — see [`.claude/skills/commit`](.claude/skills/commit/SKILL.md), which checks this file against the diff before committing.

## DSA (`dsa/app/src/main/java/com/ajay/dsa/`)

| Topic | Problem |
|---|---|
| arrays | [TwoSum](dsa/app/src/main/java/com/ajay/dsa/arrays/TwoSum.java) |
| strings | [ValidAnagram](dsa/app/src/main/java/com/ajay/dsa/strings/ValidAnagram.java) |

## Coding practice (`coding-practice/`)

| Exercise | What it is |
|---|---|
| [rate-limiter](coding-practice/rate-limiter/) | Fixed-window rate limiter |
| [transaction-ledger](coding-practice/transaction-ledger/) | Account ledger with transaction recording/history |

## System design (`system-design/`)

| Problem | Doc |
|---|---|
| [url-shortner](system-design/url-shortner/README.md) | URL shortener design |

## Design patterns (`design-patterns/`)

| Category | Pattern | Status |
|---|---|---|
| caching-strategy | [cache-aside](design-patterns/caching-strategy/cache-aside.md) | stub |
| caching-strategy | [read-through](design-patterns/caching-strategy/read-through.md) | stub |
| caching-strategy | [write-around](design-patterns/caching-strategy/write-around.md) | stub |
| caching-strategy | [write-behind](design-patterns/caching-strategy/write-behind.md) | stub |
| caching-strategy | [write-through](design-patterns/caching-strategy/write-through.md) | stub |
| authentication-and-authorization | [README](design-patterns/authentication-and-authorization/README.md) | stub |

## AI skills (`.claude/skills/`)

| Skill | What it does |
|---|---|
| [dsa-interviewer](.claude/skills/dsa-interviewer/SKILL.md) | Reviews a Java DSA solution in `dsa/` |
| [system-design-interviewer](.claude/skills/system-design-interviewer/SKILL.md) | Reviews a system design write-up in `system-design/` |
| [principal-code-interviewer](.claude/skills/principal-code-interviewer/SKILL.md) | Reviews a larger Java implementation in `coding-practice/` |
| [commit](.claude/skills/commit/SKILL.md) | Reviews and creates commits, guards against committing to `main`, keeps this Index in sync |
