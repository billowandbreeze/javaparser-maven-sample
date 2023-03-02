package com.yourorganization.maven_sample;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Util {

    /**
     * Transfer String[ObjectName, MethodName] to String ClassName.MethodName
     * @param classMethodList String[ObjectName, MethodName]
     * @param paraNameMap ClassName-ObjectName
     * @return ClassName.MethodName
     */
    public static List<String> objectNameToClassName(List<String[]> classMethodList, Map<String, String> paraNameMap) {
        List<String> res = new ArrayList<>();
        classMethodList.forEach(classMethod -> {
            if (classMethod[0].contains(".")) {
                String[] temp = classMethod[0].split("\\.");
                if (paraNameMap.containsKey(temp[0])) {
                    temp[0] = paraNameMap.get(temp[0]);
                }
                classMethod[0] = String.join(".", temp);
            } else if (paraNameMap.containsKey(classMethod[0])) {
                classMethod[0] = paraNameMap.get(classMethod[0]);
            }
            res.add(classMethod[0] + "." + classMethod[1]);
        });
        return res;
    }

    /**
     * Remove content in the brackets
     * @param oldString Original string
     * @param start From which character
     */
    private static String removeBrackets(String oldString, int start) {
        if (start >= oldString.length() || oldString.indexOf("(", start) == -1) {
            return oldString;
        }
        String bracket = oldString.substring(oldString.indexOf("(", start), oldString.indexOf(")", start) + 1);
        oldString = oldString.replace(bracket, "()");
        return removeBrackets(oldString, oldString.indexOf("(", start) + 2);
    }

    /**
     * Process cascading condition
     */
    public static List<String> cascadingCondition(List<String> res) {
        for (int i = 0; i < res.size() - 1; i++) {
            try {
                String s = Util.removeBrackets(res.get(i), 0);
                String sNext = Util.removeBrackets(res.get(i + 1), 0);

                res.set(i, s);
                if (s.contains(sNext) && !s.equals(sNext)) {
                    res.remove(i + 1);
                    i--;
                }
            } catch (Exception e) {
                System.out.println(res.get(i));
                System.out.println(e.getMessage());
                System.out.println("Errors about brackets");
            }
        }

        return res;
    }

    public static void main(String[] args) throws Exception {
        System.out.println(removeBrackets("ArrayList<>.get(0000000000)", 0));
    }
}
