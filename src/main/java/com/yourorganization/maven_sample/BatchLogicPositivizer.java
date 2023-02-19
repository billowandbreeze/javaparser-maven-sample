package com.yourorganization.maven_sample;

import com.github.javaparser.StaticJavaParser;
import org.json.JSONObject;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.utils.Log;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Some code that uses JavaParser.
 * Test code for single java file.
 */
public class BatchLogicPositivizer {
    public static void main(String[] args) {
        // JavaParser has a minimal logging class that normally logs nothing.
        // Let's ask it to write to standard out:
        Log.setAdapter(new Log.StandardOutStandardErrorAdapter());

        // Open source file
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader("src/main/resources/test.jsonl"))) {
            String line = bufferedReader.readLine();

            try (OutputStream outputStream = new FileOutputStream("output/test.txt")){
                while(line != null) {
                    JSONObject jsonObject = new JSONObject(line);
                    String code = jsonObject.getString("code");

                    // Our sample is in the root of this directory, so no package name.
                    // ___________________________________
                    System.out.println(jsonObject.getString("path"));
                    CompilationUnit cu = StaticJavaParser.parse("class TempClass { " + code + " }");
                    // System.out.println("class TempClass { " + code + " }");
                    outputStream.write((code + "\n").getBytes(StandardCharsets.UTF_8));

                    ModifierVisitorImpl<Void> modifierVisitor = new ModifierVisitorImpl<>();
                    cu.accept(modifierVisitor, null);
                    List<String> res = modifierVisitor.getResult();

                    res.forEach(s -> {
                        try {
                            outputStream.write((s + "\n").getBytes(StandardCharsets.UTF_8));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });

                    outputStream.write("----------------------------\n".getBytes(StandardCharsets.UTF_8));
                    line = bufferedReader.readLine();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
