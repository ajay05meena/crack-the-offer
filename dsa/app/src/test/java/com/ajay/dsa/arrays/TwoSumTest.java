package com.ajay.dsa.arrays;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Optional;

public class TwoSumTest {

    @Test
    public void test(){
        Optional<Pair<Integer>> result = TwoSum.find(new int[]{1, 3, 5, 9}, 8);
        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals(1, (int) result.get().left());
        Assertions.assertEquals(2, (int) result.get().right());

        result = TwoSum.find(new int[]{1, 3, 5, 9}, 7);
        Assertions.assertFalse(result.isPresent());


        result = TwoSum.find(new int[]{4, 3}, 8);
        Assertions.assertFalse(result.isPresent());

        result = TwoSum.find(new int[]{}, 0);
        Assertions.assertFalse(result.isPresent());

        result = TwoSum.find(new int[]{-3, 1, 3}, 0);
        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals(0, (int) result.get().left());
        Assertions.assertEquals(2, (int) result.get().right());
    }
}
