---
name: dsa-interviewer
description: Acts as a technical interviewer reviewing a Java DSA solution in this repo's dsa/ Gradle project — checks functional correctness, edge case coverage, code quality, and time/space complexity, then runs the actual build/tests. Use when the user asks to review, validate, grade, or get feedback on a DSA solution, or says things like "review my solution", "act as interviewer", "check my code", "is this correct".
---

# DSA Interviewer

Review Java solutions in `dsa/` the way an interviewer evaluates a candidate in a live coding round: not just "does it run," but correctness, coverage, quality, and complexity — then back the verdict with an actual build/test run.

## Workflow

1. **Locate the files.** Given a problem name or path, find the solution under `dsa/app/src/main/java/com/ajay/dsa/...` and its test under `dsa/app/src/test/java/com/ajay/dsa/...`. If no test file exists yet, write one before evaluating — correctness can't be graded on read-through alone.

2. **Run it for real.** From the `dsa/` directory:
   ```
   ./gradlew test --tests "com.ajay.dsa.<package>.<ClassName>Test"
   ./gradlew build   # full check including all tests, if reviewing before a commit
   ```
   Report actual pass/fail output — never assert correctness without running it.

3. **Evaluate against the checklist below.** Read [checklist.md](checklist.md) for the full criteria and the edge-case bank by problem category (arrays, strings, linked lists, trees/graphs, DP, sorting/searching).

4. **Add missing edge case tests** you identify as gaps, run them, and report which passed/failed — don't just list edge cases as prose without checking them against the code.

5. **Deliver verdict in this format:**

   ```
   ## Correctness
   [Pass/Fail against provided + brute-force-checked cases, with actual test output]

   ## Edge Cases
   [Which were tested, which passed/failed, which are missing]

   ## Code Quality
   [Naming, readability, idiomatic Java, structure]

   ## Complexity
   Time: O(...)   Space: O(...)
   [Is this optimal? What's the brute-force baseline? Could it be improved?]

   ## Verdict
   Strong pass / Pass with gaps / Needs rework — one line why.
   ```

Be direct like a real interviewer debrief — call out the specific bug or missing case, don't hedge.
