package com.ajay.dsa.strings;

public class EditDistanceCalculator {
    /**
     * Calculates the minimum number of operations (insert, delete, substitute)
     * required to transform wordOne into wordTwo.
     *
     * @param wordOne The starting string
     * @param wordTwo The target string
     * @return The minimum edit distance
     */
    public int calculate(String wordOne, String wordTwo) {
        // Guarantee O(min(wordOneLength, wordTwoLength)) space by ensuring wordTwo is always the shorter string
        if (wordOne.length() < wordTwo.length()) {
            String temp = wordOne;
            wordOne = wordTwo;
            wordTwo = temp;
        }

        int wordOneLength = wordOne.length();
        int wordTwoLength = wordTwo.length();

        // We only need one row of space!
        int[] dp = new int[wordTwoLength + 1];

        // 1. Initialize the base case (first row)
        for (int j = 0; j <= wordTwoLength; j++) {
            dp[j] = j;
        }

        // 2. Build the solution using a rolling array
        for (int i = 1; i <= wordOneLength; i++) {
            // prevDiag tracks the top-left diagonal value: dp[i-1][j-1]
            int prevDiag = dp[0];

            // The first column of the current row (deleting 'i' characters)
            dp[0] = i;

            for (int j = 1; j <= wordTwoLength; j++) {
                // Save the current cell before we overwrite it (becomes prevDiag for the next loop)
                int temp = dp[j];

                if (wordOne.charAt(i - 1) == wordTwo.charAt(j - 1)) {
                    dp[j] = prevDiag; // Characters match
                } else {
                    // dp[j-1] is Left (Insert)
                    // dp[j] is Top (Delete) before it gets overwritten
                    // prevDiag is Top-Left (Substitute)
                    dp[j] = 1 + Math.min(dp[j - 1], Math.min(dp[j], prevDiag));
                }

                // Update the diagonal tracker for the next iteration
                prevDiag = temp;
            }
        }

        return dp[wordTwoLength];
    }
}


