package com.yourorganization.maven_sample;

import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.CastExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.ast.visitor.Visitable;

import java.util.*;

public class ModifierVisitorImpl<A> extends ModifierVisitor<A> {

    final String INIT_STRING = "<init>";
    private String className;

    private Map<String, String> paraNameMap = new HashMap<>();
    private Map<String, String> typeNameMap = new HashMap<>();
    private List<String[]> classMethodList = new ArrayList<>();

    /**
     * Solve problems about custom method.
     */
    public ModifierVisitorImpl(String className) {
        this.className = className;
    }

    /**
     * eg. ((String) classpathElements.get(i))
     * type-CastExpr
     *  -expression
     *  -type-ClassOrInterfaceType
     *      -name
     *          -identifier
     */
    @Override
    public Visitable visit(CastExpr n, A arg) {
        if (n.getType().isClassOrInterfaceType()) {
            typeNameMap.put(n.getExpression().toString(), n.getType().asString());
        }


        return super.visit(n, arg);
    }

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
        if (n.getType().isClassOrInterfaceType()) {
            paraNameMap.put(n.getName().toString(), n.getType().asClassOrInterfaceType().getName().asString());
        } else {
            paraNameMap.put(n.getName().toString(), n.getType().toString());
        }

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
            if (variableDeclarator.getInitializer().isPresent()) {
                // if type is ObjectCreationExpr
                if (variableDeclarator.getInitializer().get().isObjectCreationExpr()) {
                    // type name
                    String typeName = variableDeclarator.getInitializer().get().asObjectCreationExpr().getType().toString();
                    // add to map
                    typeNameMap.put(variableDeclarator.getName().toString(), typeName);
                    // variable type
                    classMethodList.add(new String[] {typeName, INIT_STRING});
                // if type is MethodCallExpr
                } else if (variableDeclarator.getInitializer().get().isMethodCallExpr()) {
                    if (variableDeclarator.getType().isClassOrInterfaceType()) {
                        typeNameMap.put(variableDeclarator.getName().asString(), variableDeclarator.getType().asClassOrInterfaceType().getName().asString());
                    } else {
                        typeNameMap.put(variableDeclarator.getName().asString(), variableDeclarator.getType().asString());
                    }
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
        if (n.getScope().isPresent()
                && (n.getScope().get().isMethodCallExpr()
                || n.getScope().get().isNameExpr())) {

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

            classMethodList.add(new String[] {className, n.getName().asString()});
        } else if (n.getScope().isPresent()
                && n.getScope().get().isEnclosedExpr()
                && n.getScope().get().asEnclosedExpr().getInner().isCastExpr()
                && n.getScope().get().asEnclosedExpr().getInner().asCastExpr().getType().isClassOrInterfaceType()
        ) {
            classMethodList.add(new String[] {n.getScope().get().asEnclosedExpr().getInner().asCastExpr().getType().asClassOrInterfaceType().getName().asString(),
                    n.getName().asString()});
        }

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
        classMethodList.add(new String[] {n.getType().getName().asString(), INIT_STRING});

        return super.visit(n, arg);
    }


    /**
     * Return the API Sequence
     */
    public List<String> getResult() {

        // condition: object name to class name
        List<String> res = Util.objectNameToClassName(classMethodList, paraNameMap, className);

        // condition: cascading condition
         res = Util.cascadingCondition(res);

        return res;
    }
}
