# crack-the-offer

Personal interview prep repo — sharpening skills across DSA, coding practice, system design, and AI.

## Structure

- `dsa/` — data structures & algorithms practice
- `coding-practice/` — general coding exercises, katas, language practice (each substantial exercise is its own Gradle project, e.g. `coding-practice/rate-limiter/`)
- `system-design/` — system design notes, diagrams, case studies
- `design-patterns/` — reference notes per pattern/technique (e.g. `design-patterns/caching-strategy/`, `design-patterns/authentication-and-authorization/`)
- `ai-skills/` — AI/ML concepts, LLM tooling, applied projects

## Where to write things

**DSA solutions** go under `dsa/app/src/main/java/com/ajay/dsa/<topic>/<ProblemName>.java`, with a matching test at `dsa/app/src/test/java/com/ajay/dsa/<topic>/<ProblemName>Test.java`. `<topic>` is a package per category (`arrays`, `strings`, etc. — add a new package for a new category). E.g. `dsa/app/src/main/java/com/ajay/dsa/arrays/TwoSum.java` + `dsa/app/src/test/java/com/ajay/dsa/arrays/TwoSumTest.java`.

**System design write-ups** go under `system-design/<problem-name>/design.md` — a markdown doc per problem (requirements, high-level design, deep dive, tradeoffs; Mermaid diagrams inline if useful).

**Larger coding exercises** (a rate limiter, a small cache, anything bigger than a single DSA method) go under `coding-practice/<exercise-name>/` as their own Gradle project — mirror the `dsa/` project's structure (Gradle wrapper, Java 21 toolchain, JUnit Jupiter, `app/src/main/java/com/ajay/<exercise-name>/...`).

Work on a branch per problem, then ask Claude to review — see below.

## Claude Code skills

This repo defines custom Claude Code skills under `.claude/skills/` that act as interviewers reviewing your work:

- **`dsa-interviewer`** — reviews a Java DSA solution in `dsa/`. Locates the solution/test added or changed on the current branch (via `git diff` against `main`), compiles and runs its tests, then reports correctness, edge-case coverage, code quality, and complexity.
- **`system-design-interviewer`** — reviews a system design write-up in `system-design/` at a Staff/Principal bar. Locates the doc added or changed on the current branch, evaluates it against the standard rubric (scope, architecture, scale, reliability, tradeoffs), and calls out which specific things you did read as Staff-level vs Principal-level.
- **`principal-code-interviewer`** — reviews a larger Java implementation (e.g. `coding-practice/` exercises) at a Staff/Principal bar. Locates the code added or changed on the current branch, compiles and runs its tests, then evaluates correctness (including concurrency safety where relevant), API design, testing rigor, and failure handling — plus which specific things read as Staff-level vs Principal-level. For DSA puzzles, prefer `dsa-interviewer` instead.

### Triggering a skill

Claude auto-detects which skill to use from what you say — e.g. "review my solution", "act as interviewer", "review the current branch", "is this Staff/Principal level" will each match the relevant skill based on its description. You don't need to name it explicitly.

To invoke a skill by name directly, use a slash command with the skill name: `/dsa-interviewer`, `/system-design-interviewer`, or `/principal-code-interviewer`. All three work off whatever your current git branch has added/changed, so just check out the branch with your solution or write-up before asking for a review.
