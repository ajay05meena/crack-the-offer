package com.ajay.dsa.strings;

public class ValidAnagram {

    public static boolean is(String wordOne, String wordTwo){
        if(wordOne.length() != wordTwo.length()){
            return false;
        }
        int wordLength = wordOne.length();
        for(int i=0; i<wordOne.length(); i++){
            if(wordOne.charAt(i) != wordTwo.charAt(wordLength-i-1)){
                return false;
            }
        }
        return true;
    }
}
