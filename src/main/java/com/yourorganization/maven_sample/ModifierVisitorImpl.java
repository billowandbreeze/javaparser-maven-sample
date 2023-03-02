package com.yourorganization.maven_sample;

import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.ConditionalExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.ast.visitor.Visitable;

import java.util.*;

public class ModifierVisitorImpl<A> extends ModifierVisitor<A> {
    Map<String, String> paraNameMap = new HashMap<>();
    Map<String, String> typeNameMap = new HashMap<>();
    List<String[]> classMethodList = new ArrayList<>();

    /**
     * eg. String path
     * parameter-Parameter
     *  -name-SimpleName
     *      -identifier-path(need)
     *  -type-ClassOrInterfaceType
     *      -name-SimpleName
     *          -identifier-String(need)
     */
    @Override
    public Visitable visit(Parameter n, A arg) {
        paraNameMap.put(n.getName().toString(), n.getType().toString());

        return super.visit(n, arg);
    }

    /**
     * eg. List<Integer> list = new ArrayList<>();
     * expression-VariableDeclarationExpr
     * -variables
     *  -variable-VariableDeclarator
     *      -initializer-ObjectCreationExpr
     *          -type-ClassOrInterfaceType
     *              -name-SimpleName
     *                  -identifier-ArrayList(need)
     *      -name-SimpleName
     *          -identifier-list(need)
     *      -type-ClassOrInterfaceType
     *          -name-SimpleName
     *              -identifier-List
     */
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

    /**
     * eg. list.add(1)
     * expression-MethodCallExpr
     *  -name-SimpleName
     *      -identifier-add(need)
     *  -scope-NameExpr
     *      -name-SimpleName
     *          -identifier-list
     *
     * eg. lines.get(i).trim().isEmpty()
     * condition-MethodCallExpr
     *  -name-SimpleName
     *      -identifier-isEmpty(need)
     *  -scope-MethodCallExpr
     *      -name-SimpleName
     *          -identifier-trim(need)
     *      -scope-MethodCallExpr
     *          -name-SimpleName
     *              -identifier-get(need)
     *          -scope-NameExpr
     *              -name-SimpleName
     *                  -identifier-lines(need)
     */
    @Override
    public Visitable visit(MethodCallExpr n, A arg) {
        // class or method
        String className = "";
        if (n.getScope().isPresent()) {
            className = n.getScope().get().toString();

            if (className.contains(".")) {
                // cascading API calls
                String[] temp = className.split("\\.");
                if (typeNameMap.containsKey(temp[0])) {
                    temp[0] = typeNameMap.get(temp[0]);
                }
                className = String.join(".", temp);
            } else if (typeNameMap.containsKey(className)) {
                // one API call
                className = typeNameMap.get(className);
            }
        }
        classMethodList.add(new String[] {className, n.getName() + "()"});

        return super.visit(n, arg);
    }

    /**
     * eg. return ParameterizedTypeImpl(ownerType, rawType, typeArguments)
     * statement-ReturnStmt
     *  -expression-ObjectCreationExpr
     *      -type-ClassOrInterfaceType
     *          -name-SimpleName
     *              -identifier-ParameterizedTypeImpl
     */
    @Override
    public Visitable visit(ObjectCreationExpr n, A arg) {
        classMethodList.add(new String[] {n.getTypeAsString() + "()", "<init>"});

        return super.visit(n, arg);
    }


    /**
     * Return the API Sequence
     */
    public List<String> getResult() {

        // condition: object name to class name
        List<String> res = Util.objectNameToClassName(classMethodList, paraNameMap);

        // condition: cascading condition
        res = Util.cascadingCondition(res);

        return res;
    }
}
