package com.ajay.dsa.strings;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class EditDistanceCalculatorTest {

    // You can swap this out with new DPEditDistance() later!
    private final EditDistanceCalculator calculator = new EditDistanceCalculator();

    @Test
    public void testEmptyStrings() {
        // Both empty should require 0 edits
        assertEquals(0, calculator.calculate("", ""));
    }

    @Test
    public void testOneEmptyString() {
        // Requires 5 insertions
        assertEquals(5, calculator.calculate("", "hello"));
        // Requires 5 deletions
        assertEquals(5, calculator.calculate("world", ""));
    }

    @Test
    public void testIdenticalStrings() {
        // No edits needed
        assertEquals(0, calculator.calculate("java", "java"));
    }

    @Test
    public void testOnlyInsertions() {
        // Insert 's' at the end
        assertEquals(1, calculator.calculate("cat", "cats"));
    }

    @Test
    public void testOnlyDeletions() {
        // Delete 's' at the end
        assertEquals(1, calculator.calculate("dogs", "dog"));
    }

    @Test
    public void testOnlySubstitutions() {
        // Substitute 'c' -> 'b'
        assertEquals(1, calculator.calculate("cat", "bat"));
    }

    @Test
    public void testClassicHorseToRos() {
        /*
         * horse -> rorse (replace 'h' with 'r')
         * rorse -> rose (remove 'r')
         * rose -> ros (remove 'e')
         * Total: 3
         */
        assertEquals(3, calculator.calculate("horse", "ros"));
    }

    @Test
    public void testComplexTransformation() {
        /*
         * intention -> inention (remove 't')
         * inention -> enention (replace 'i' with 'e')
         * enention -> exention (replace 'n' with 'x')
         * exention -> exection (replace 'n' with 'c')
         * exection -> execution (insert 'u')
         * Total: 5
         */
        assertEquals(5, calculator.calculate("intention", "execution"));
    }
}