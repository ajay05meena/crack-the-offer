package com.ajay.dsa.strings;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ValidAnagram {

    public static boolean is(String wordOne, String wordTwo){
        if(wordOne.length() != wordTwo.length()){
            return false;
        }
        Map<Character, Integer> frequencyCountFromWordOne = new HashMap<Character, Integer>();
        Map<Character, Integer> frequencyCountFromWordTwo = new HashMap<Character, Integer>();

        for(int i = 0; i< wordOne.length(); i++){
            frequencyCountFromWordOne.put(wordOne.charAt(i), frequencyCountFromWordOne.getOrDefault(wordOne.charAt(i), 0)+1);
            frequencyCountFromWordTwo.put(wordTwo.charAt(i), frequencyCountFromWordTwo.getOrDefault(wordTwo.charAt(i), 0)+1);
        }

        for(int i =0; i< wordOne.length(); i++){
            if(!Objects.equals(frequencyCountFromWordOne.getOrDefault(wordOne.charAt(i), 0), frequencyCountFromWordTwo.getOrDefault(wordOne.charAt(i), 0))){
                return false;
            }
        }

        return true;

    }
}
