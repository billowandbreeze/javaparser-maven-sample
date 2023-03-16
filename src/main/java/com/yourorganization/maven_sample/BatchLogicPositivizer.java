package com.yourorganization.maven_sample;

import com.github.javaparser.ParseProblemException;
import com.github.javaparser.StaticJavaParser;
import org.json.JSONObject;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.utils.Log;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Some code that uses JavaParser.
 * Test code for single java file.
 */
public class BatchLogicPositivizer {
    public static void main(String[] args) {
        // JavaParser has a minimal logging class that normally logs nothing.
        // Let's ask it to write to standard out:
        Log.setAdapter(new Log.StandardOutStandardErrorAdapter());

        // File that contains batch of code snippets.
        String name = "train_all";

        // Open source file
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader("src/main/resources/batch/" + name + ".jsonl"));
             OutputStream outputStream = new FileOutputStream("output/res_with_api_sequence/" + name + "_with_api_sequence_v1.jsonl")
        ) {
            // Read line
            String line = bufferedReader.readLine();

            while(line != null) {
                // Get information from line
                JSONObject jsonObject = new JSONObject(line);
                String code = jsonObject.getString("code");
                String className = jsonObject.getString("func_name").substring(0, jsonObject.getString("func_name").indexOf('.'));

                CompilationUnit cu = null;
                // Parse code to AST tree
                try {
                    cu = StaticJavaParser.parse("class " + className + " { " + code + " }");
                } catch (ParseProblemException e) {
                    System.out.println(code);
                    System.out.println("Does it miss a '}'? (y/n)");
                    Scanner scan = new Scanner(System.in);
                    String check = scan.nextLine();
                    if (check.equals("n")) {
                        System.out.println(code);
                        System.out.println("Is it a '_' method? (y/n)");
                        scan = new Scanner(System.in);
                        check = scan.nextLine();
                        if (check.equals("n")) {
                            e.printStackTrace();
                        } else {
                            code = code.replaceFirst("_", "methodName");
                            cu = StaticJavaParser.parse("class TempClass { " + code + " }");
                        }
                    } else {
                        cu = StaticJavaParser.parse("class TempClass { " + code + " } }");
                    }
                }

                // Add api sequence
                ModifierVisitorImpl<Void> modifierVisitor = new ModifierVisitorImpl<>(className);
                cu.accept(modifierVisitor, null);
                List<String> res = modifierVisitor.getResult();
                jsonObject.put("api_sequence", res);

                // TODO: Human check
//                System.out.println(code);
                res.forEach(System.out::println);
//                System.out.println("Is the api sequence ok? (y/n)");
//                Scanner scan = new Scanner(System.in);
//                String check = scan.nextLine();
//                if (check.equals("n")) {
//                    throw new Exception("New Error!");
//                } else {
//                    System.out.println("Fine");
//                }

                // Write to file
                outputStream.write((jsonObject + "\n").getBytes(StandardCharsets.UTF_8));
                // Update line
                line = bufferedReader.readLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
