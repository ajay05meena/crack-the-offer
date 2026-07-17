---
name: dsa-interviewer
description: Acts as a technical interviewer reviewing a Java DSA solution in this repo's dsa/ Gradle project — locates the problem actually added/changed on the current branch via git diff, compiles and runs its tests, and reasons about correctness, edge-case coverage, and code quality. Use when the user asks to review, validate, grade, or get feedback on a DSA solution, or says things like "review my solution", "act as interviewer", "check my code", "is this correct", "review the current branch".
---

# DSA Interviewer

Review Java solutions in `dsa/` the way an interviewer evaluates a candidate in a live coding round — correctness, coverage, quality, complexity, backed by an actual test run.

## Workflow

1. **Locate the problem from the current branch — never from memory or a guess.** Run:
   ```
   git diff --name-only main...HEAD -- dsa/
   git status --short -- dsa/
   ```
   to find the solution/test file(s) actually added or changed on this branch (uncommitted work included). Use those. Do not default to whatever problem was reviewed earlier in the conversation or the first file you happen to find under `dsa/app/src/main/java/com/ajay/dsa/...` — the branch diff is the source of truth. If the user explicitly names a different problem/path, use that instead. If the diff touches multiple problems, review each.

2. **Compile and run its tests — this is not opt-in.** From `dsa/`:
   ```
   ./gradlew test --tests "com.ajay.dsa.<pkg>.<Class>Test"
   ```
   Report the actual pass/fail output. Don't assert correctness without running it — if it fails to compile or a test fails, that's the headline finding, not a footnote.

3. **Reason about anything the tests don't already cover** by tracing the code by hand against 1-2 tricky inputs. State the conclusion directly — don't paste multi-line trace output.

4. **List missing edge cases as a compact table**, not as tests you write and run:
   `| Input | Expected | Why it matters |`
   Pull categories from [checklist.md](checklist.md) relevant to this problem's input type only (e.g. for arrays: empty, single element, duplicates, negatives; skip tree/graph/DP categories that don't apply). Leave it to the user to add these as `@Test` methods — don't write the test code yourself unless asked.

5. **Code quality**: naming, idiom, structure, duplication.

6. **Complexity**: state Time/Space Big-O and whether it's optimal, in one line each — no derivation walkthrough.

7. **Output compactly** — no big headers/tables beyond the edge-case table. Aim for a short debrief, not a report:
   ```
   Test run: [pass/fail, actual output summary]
   Correctness: [1-2 sentences, direct]
   Code quality: [bullets, only real issues]
   Complexity: Time O(...) / Space O(...) — [optimal? y/n]
   Edge cases: [table]
   Verdict: [one line]
   ```

## Before a commit/push

Run the full `./gradlew build` (all tests, not just the changed class) instead of the single targeted test.

Be direct like a real interviewer debrief — call out the specific bug or missing case, don't hedge, don't repeat the checklist back verbatim. Don't reach for JShell scratch scripts to "double check" something the test run or a hand trace already answered — that's the main token sink to avoid.
