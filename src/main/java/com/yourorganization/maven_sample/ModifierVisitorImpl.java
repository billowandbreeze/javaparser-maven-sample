package com.yourorganization.maven_sample;

import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.ast.visitor.Visitable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ModifierVisitorImpl<A> extends ModifierVisitor<A> {
    HashMap<String, String> paraNameMap = new HashMap<>();
    HashMap<String, String> typeNameMap = new HashMap<>();
    List<String[]> classMethodList = new ArrayList<>();
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
    public Visitable visit(Parameter n, A arg) {
        paraNameMap.put(n.getName().toString(), n.getType().toString());

        return super.visit(n, arg);
    }

    @Override
    public Visitable visit(VariableDeclarationExpr n, A arg) {
        // Find VariableDeclaration
        n.getVariables().forEach(variableDeclarator -> {
            // if type is ObjectCreationExpr
            if (variableDeclarator.getInitializer().isPresent()) {
                if (variableDeclarator.getInitializer().get().isObjectCreationExpr()) {
                    // type name
                    String typeName = variableDeclarator.getInitializer().get().asObjectCreationExpr().getType().toString();
                    // add to map
                    typeNameMap.put(variableDeclarator.getName().toString(), typeName);
                    // variable type
                    classMethodList.add(new String[] {typeName, "<init>"});
                }
            }
        });

        return super.visit(n, arg);
    }

    @Override
    public Visitable visit(MethodCallExpr n, A arg) {
        // class or method
        String className = "";
        if (n.getScope().isPresent()) {
            if (typeNameMap.containsKey(n.getScope().get().toString())) {
                className = typeNameMap.get(n.getScope().get().toString());
            } else {
                className = n.getScope().get().toString();
            }
        }
        classMethodList.add(new String[] {className, n.getName() + "()"});

        return super.visit(n, arg);
    }

    public List<String> getResult() {
        List<String> res = new ArrayList<>();

        classMethodList.forEach(classMethod -> {
            if (paraNameMap.containsKey(classMethod[0])) {
                classMethod[0] = paraNameMap.get(classMethod[0]);
            }
            res.add(classMethod[0] + "." + classMethod[1]);
        });

        return res;
    }
}
