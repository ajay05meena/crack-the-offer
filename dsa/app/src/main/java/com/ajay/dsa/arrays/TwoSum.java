package com.ajay.dsa.arrays;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/*
Given an array of integers and a target, return the indices of the two numbers that add up to the target.

Example: nums = [2, 7, 11, 15], target = 9 → [0, 1] (since 2 + 7 = 9)

 */

record Pair<T>(T left, T right){}

public class TwoSum {

    public static Optional<Pair<Integer>> find(int [] nums, int target){
        Map<Integer, Integer> seen = new HashMap<>();
        for(int i= 0; i<nums.length;i++){
            if(seen.containsKey(target-nums[i])){
                return Optional.of(new Pair<Integer>(seen.get(target-nums[i]), i));
            }
            seen.put(nums[i], i);
        }
        return Optional.empty();
    }
}
