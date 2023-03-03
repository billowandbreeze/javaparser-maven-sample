package com.yourorganization.maven_sample;

import java.util.*;

public class Util {

    /**
     * Transfer String[ObjectName, MethodName] to String ClassName.MethodName
     * @param classMethodList String[ObjectName, MethodName]
     * @param paraNameMap ClassName-ObjectName
     * @param className If the object name is null, then consider the method as custom method. So the file name is the class name.
     * @return ClassName.MethodName
     */
    public static List<String> objectNameToClassName(List<String[]> classMethodList, Map<String, String> paraNameMap, String className) {
        List<String> res = new ArrayList<>();
        classMethodList.forEach(classMethod -> {
            if (classMethod[0].isEmpty()) {
                // If object name is null
                classMethod[0] = className;
            } else if (classMethod[0].contains(".")) {
                // If object name is not null and has '.'
                String[] temp = classMethod[0].split("\\.");
                if (paraNameMap.containsKey(temp[0])) {
                    temp[0] = paraNameMap.get(temp[0]);
                }
                classMethod[0] = String.join(".", temp);
            } else if (paraNameMap.containsKey(classMethod[0])) {
                // If object name is not null and not has '.'
                classMethod[0] = paraNameMap.get(classMethod[0]);
            }
            res.add(classMethod[0] + "." + classMethod[1]);
        });
        return res;
    }

    /**
     * Remove content in the brackets and bracket itself
     * @param oldString Original string
     */
    private static String removeBrackets(String oldString, char left, char right) {

        Stack<int[]> stack = new Stack<>();
        List<String> brackets = new ArrayList<>();

        for(int i = 0; i < oldString.length(); i++) {
            if (oldString.charAt(i) == left) {
                stack.add(new int[]{0, i});
            } else if (oldString.charAt(i) == right) {
                if (stack.isEmpty()) {
                    return oldString;
                } else if (stack.peek()[0] == 0) {
                    int temp = stack.pop()[1];

                    if (stack.isEmpty()) {
                        brackets.add(oldString.substring(temp, i + 1));
                    }
                }
            }
        }

        for (String bracket : brackets) {
            oldString = oldString.replace(bracket, "");
        }
        return oldString;
    }

    /**
     * Process cascading condition
     */
    public static List<String> cascadingCondition(List<String> res) {
        for (int i = 0; i < res.size() - 1; i++) {
            try {
                String s = Util.removeBrackets(res.get(i), '(', ')');
                String sNext = Util.removeBrackets(res.get(i + 1), '(', ')');

                res.set(i, s);
                if (s.contains(sNext) && !s.equals(sNext)) {
                    res.remove(i + 1);
                    i--;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (!res.isEmpty()) {
            res.set(res.size() - 1, Util.removeBrackets(res.get(res.size() - 1), '(', ')'));
        }

        return res;
    }

    public static void main(String[] args) {
        System.out.println(removeBrackets("File((String) classpathElements.get(i)).toURI().toURL", '(', ')'));
    }
}
