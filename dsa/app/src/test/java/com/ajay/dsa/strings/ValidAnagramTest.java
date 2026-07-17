package com.ajay.dsa.strings;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ValidAnagramTest {

    @Test
    public void test(){

        Assertions.assertTrue(ValidAnagram.is("abcd", "dcba"));
        Assertions.assertFalse(ValidAnagram.is("abcd", "cba"));
    }
}
