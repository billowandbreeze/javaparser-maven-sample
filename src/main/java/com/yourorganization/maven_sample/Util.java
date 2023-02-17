package com.yourorganization.maven_sample;

public class Util {
    public static String removeBrackets(String oldString, int start) {
        if (oldString.indexOf("(", start) == -1) {
            return oldString;
        }
        String bracket = oldString.substring(oldString.indexOf("(", start), oldString.indexOf(")", start) + 1);
        oldString = oldString.replace(bracket, "()");
        return removeBrackets(oldString, oldString.indexOf("(", start) + 2);
    }

    public static void main(String[] args) {
        System.out.println(removeBrackets("ArrayList<>.get(0000000000).trim(0).isEmpty(0).get(0000000000).trim(0).isEmpty(0).get(0000000000).trim(0).isEmpty(0)", 0));
    }
}
