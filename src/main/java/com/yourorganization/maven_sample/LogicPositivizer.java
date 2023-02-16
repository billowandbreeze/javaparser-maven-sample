package com.yourorganization.maven_sample;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.ast.visitor.Visitable;
import com.github.javaparser.printer.YamlPrinter;
import com.github.javaparser.utils.CodeGenerationUtils;
import com.github.javaparser.utils.Log;
import com.github.javaparser.utils.SourceRoot;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.HashMap;

/**
 * Some code that uses JavaParser.
 */
public class LogicPositivizer {
    public static void main(String[] args) {
        String fileName = "SimpleMethod";
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

        cu.accept(new ModifierVisitor<Void>() {
            HashMap<String, String> typeNameMap = new HashMap<>();
            /**
             * VariableDeclaration
             * Find statement
             *  -expression-Type=VariableDeclarationExpr
             *      -initializer-Type=ObjectCreationExpr
             *          -identifier(need)
             *      -name
             *          -identifier(need)
             *      -type
             *          -identifier(need)
             * MethodCall
             * Find statement
             *  -expression-Type=MethodCallExpr
             *      -name-Type=SimpleName
             *          -identifier(need)
             *      -scope-Type=NameExpr
             *          -identifier(need)
             */
            @Override
            public Visitable visit(VariableDeclarationExpr n, Void arg) {
                // Find VariableDeclaration
                n.getVariables().forEach(variableDeclarator -> {
                    // add to map
                    typeNameMap.put(variableDeclarator.getName().toString(), variableDeclarator.getType().toString());
                    // variable type
                    System.out.println(variableDeclarator.getType() + ".<init>");
                    // variable name
//                    System.out.println(variableDeclarator.getName());
                    // variable value
//                    System.out.println(variableDeclarator.getInitializer().get());
                });

                return super.visit(n, arg);
            }

            @Override
            public Visitable visit(MethodCallExpr n, Void arg) {
                // class or method
                if (n.getScope().isPresent()) {
                    if (typeNameMap.containsKey(n.getScope().get().toString())) {
                        System.out.print(typeNameMap.get(n.getScope().get().toString()));
                    } else {
                        System.out.print(n.getScope().get());
                    }
                }
                System.out.print("." + n.getName());
                System.out.println();

                return super.visit(n, arg);
            }
        }, null);

        // This saves all the files we just read to an output directory.
        sourceRoot.saveAll(
                // The path of the Maven module/project which contains the LogicPositivizer class.
                CodeGenerationUtils.mavenModuleRoot(LogicPositivizer.class)
                        // appended with a path to "output"
                        .resolve(Paths.get("output")));
    }
}
