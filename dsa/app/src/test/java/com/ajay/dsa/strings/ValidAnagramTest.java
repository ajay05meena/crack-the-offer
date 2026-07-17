package com.ajay.dsa.strings;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ValidAnagramTest {

    @Test
    public void test(){
        Assertions.assertTrue(ValidAnagram.is("anagram", "nagaram"));
        Assertions.assertTrue(ValidAnagram.is("", ""));
        Assertions.assertFalse(ValidAnagram.is("aacc", "ccac"));
        Assertions.assertFalse(ValidAnagram.is("Dog", "god"));
    }
}
