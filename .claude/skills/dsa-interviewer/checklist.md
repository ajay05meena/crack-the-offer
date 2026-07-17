# Evaluation Checklist

## Functional Correctness
- Matches expected output on the stated examples.
- Also verify against a brute-force/naive re-derivation of 2-3 cases by hand (or a second, dumber implementation) — don't just trust the given examples, they're often not adversarial.
- If the method has preconditions (sorted input, no duplicates, etc.), confirm they're actually documented/enforced, not just assumed.

## Edge Cases — check what's relevant to the problem's input type

**Arrays / Lists**
- Empty array, single element, two elements
- All identical elements, all distinct
- Already sorted, reverse sorted
- Contains negatives / zero / `Integer.MIN_VALUE` / `Integer.MAX_VALUE`
- Duplicates when the problem implies uniqueness (or vice versa)
- Target/answer doesn't exist in the input

**Strings**
- Empty string, single character
- All same character
- Whitespace, mixed case (if relevant)
- Unicode/multi-byte characters (if not explicitly excluded)
- Palindromic or already-matching input

**Linked Lists**
- Empty list (null head), single node
- Two nodes
- Cycle present, if the problem's structure allows it
- Operating on head/tail boundary (removal, reversal at ends)

**Trees / Graphs**
- Empty tree (null root), single node
- Skewed tree (essentially a linked list) vs. balanced
- Disconnected graph, self-loops, duplicate edges
- Cyclic vs. acyclic where relevant (recursion/DFS stack depth)

**DP / Recursion**
- Base cases explicitly correct (n=0, n=1)
- Overlapping subproblems actually memoized (not exponential blowup)
- Stack overflow risk on deep recursion — should it be iterative?

**Sorting / Searching**
- Empty and single-element input
- Already sorted / reverse sorted (worst case for some algorithms)
- Search target at boundaries (first/last index) and absent

**Numeric**
- Overflow potential (int vs long)
- Zero, negative numbers, division edge cases

## Code Quality
- Meaningful names (no `a`, `tmp`, `x1` for anything non-trivial)
- No magic numbers — named constants where it aids clarity
- Appropriate data structures for the access pattern (e.g. `HashMap` for O(1) lookup vs. linear scan)
- No dead code, unused imports, or commented-out attempts left in
- Method does one thing; extract helpers if a method mixes concerns
- Immutability where it doesn't cost clarity (final locals/fields)
- Follows the repo's existing package/class layout (`com.ajay.dsa.<topic>.<ProblemName>`)

## Complexity Analysis
- State time and space Big-O explicitly.
- Compare to the known optimal for this problem class — if brute force is O(n²) and an O(n) or O(n log n) approach exists, flag it.
- Watch for hidden costs: `substring`/`toCharArray` copies, autoboxing in hot loops, `ArrayList.contains` (O(n)) used where a `Set` would be O(1).
- Space complexity should account for auxiliary structures, not just the output.

## Communication (as an interviewer would judge it)
- Would the approach be easy to explain out loud in under a minute?
- Are there comments only where the *why* is non-obvious (a trick, a non-obvious invariant) — not restating *what* the code does?
- Is there a simpler approach being overlooked in favor of a clever one, or vice versa?
