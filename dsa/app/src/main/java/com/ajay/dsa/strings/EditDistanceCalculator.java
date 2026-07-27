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
        int wordOneLength = wordOne.length();
        int wordTwoLength = wordTwo.length();

        int[][] dp = new int[wordOneLength + 1][wordTwoLength + 1];

        // 1. Initialize Base Cases (Empty string transformations)
        for (int i = 0; i <= wordOneLength; i++) {
            dp[i][0] = i; // Deleting all characters from wordOne
        }
        for (int j = 0; j <= wordTwoLength; j++) {
            dp[0][j] = j; // Inserting all characters to match wordTwo
        }

        // 2. Build the solution bottom-up
        for (int i = 1; i <= wordOneLength; i++) {
            for (int j = 1; j <= wordTwoLength; j++) {

                // Note: i and j are 1-indexed for the grid,
                // so we use i-1 and j-1 to check the actual string characters
                if (wordOne.charAt(i - 1) == wordTwo.charAt(j - 1)) {
                    // Characters match. Take the diagonal value (no new cost).
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    // Characters do not match. Take the minimum of the 3 operations + 1.
                    int insert = dp[i][j - 1];
                    int delete = dp[i - 1][j];
                    int substitute = dp[i - 1][j - 1];

                    dp[i][j] = 1 + Math.min(insert, Math.min(delete, substitute));
                }
            }
        }

        // The bottom-right corner holds the answer for the full strings
        return dp[wordOneLength][wordTwoLength];
    }
}


