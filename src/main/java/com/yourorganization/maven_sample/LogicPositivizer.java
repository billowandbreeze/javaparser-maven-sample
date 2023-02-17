package com.yourorganization.maven_sample;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.printer.YamlPrinter;
import com.github.javaparser.utils.CodeGenerationUtils;
import com.github.javaparser.utils.Log;
import com.github.javaparser.utils.SourceRoot;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Scanner;

/**
 * Some code that uses JavaParser.
 */
public class LogicPositivizer {
    public static void main(String[] args) {
        System.out.println("File Location(eg. query/Query339):");
        Scanner scan = new Scanner(System.in); //声明一个Scanner对象，初始输入流为控制台
        String fileName = scan.nextLine();
        // JavaParser has a minimal logging class that normally logs nothing.
        // Let's ask it to write to standard out:
        Log.setAdapter(new Log.StandardOutStandardErrorAdapter());

        // SourceRoot is a tool that read and writes Java files from packages on a certain root directory.
        // In this case the root directory is found by taking the root from the current Maven module,
        // with src/main/resources appended.
        SourceRoot sourceRoot = new SourceRoot(CodeGenerationUtils.mavenModuleRoot(LogicPositivizer.class).resolve("src/main/resources"));

        // Our sample is in the root of this directory, so no package name.
        CompilationUnit cu = sourceRoot.parse("", fileName + ".java");

        // Print AST Tree
        YamlPrinter printer = new YamlPrinter(true);
        System.out.println(printer.output(cu));
        // Save AST Tree
        try (OutputStream outputStream = new FileOutputStream("output/" + fileName + ".txt")){
            outputStream.write(printer.output(cu).getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.info("Positivizing!");

        ModifierVisitorImpl<Void> modifierVisitor = new ModifierVisitorImpl<>();
        cu.accept(modifierVisitor, null);
        modifierVisitor.getResult().forEach(System.out::println);

        // This saves all the files we just read to an output directory.
        sourceRoot.saveAll(
                // The path of the Maven module/project which contains the LogicPositivizer class.
                CodeGenerationUtils.mavenModuleRoot(LogicPositivizer.class)
                        // appended with a path to "output"
                        .resolve(Paths.get("output")));
    }
}
