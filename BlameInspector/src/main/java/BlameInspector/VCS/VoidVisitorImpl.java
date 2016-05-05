package blameinspector.vcs;

import com.github.antlrjavaparser.api.Comment;
import com.github.antlrjavaparser.api.body.*;
import com.github.antlrjavaparser.api.expr.LambdaExpr;
import com.github.antlrjavaparser.api.expr.MethodReferenceExpr;
import com.github.antlrjavaparser.api.visitor.VoidVisitorAdapter;

import java.util.ArrayList;

public class VoidVisitorImpl extends VoidVisitorAdapter {

    public static final String DOT = ".";

    private String className;
    private String methodName;
    private boolean classFound;
    private boolean methodFound;
    private ArrayList<String> methods;

    public VoidVisitorImpl(final String className, final String methodName) {
        this.className = className;
        this.methodName = methodName;
        this.classFound = false;
        this.methodFound = false;
        methods = new ArrayList<>();
    }

    public boolean isFound(){
        return classFound && methodFound;
    }

    public ArrayList<String> getMethods() {
        return this.methods;
    }

    @Override
    public void visit(ClassOrInterfaceDeclaration classOrInterfaceDeclaration, Object o) {
        className = classOrInterfaceDeclaration.getName();
        if (className.equals(this.className)){
            classFound = true;
        }
        for (BodyDeclaration member : classOrInterfaceDeclaration.getMembers()) {
            member.accept(this, null);
        }
    }


    @Override
    public void visit(Comment comment, Object o) {

    }


    @Override
    public void visit(ConstructorDeclaration constructorDeclaration, Object o) {
        if (constructorDeclaration.getName().equals(methodName)){
            methodFound = true;
        }
        methods.add(className + DOT + constructorDeclaration.getName());
    }

    @Override
    public void visit(MethodDeclaration methodDeclaration, Object o) {
        if (methodDeclaration.getName().equals(methodName)){
            methodFound = true;
        }
        methods.add(className + DOT + methodDeclaration.getName());
    }


    @Override
    public void visit(CatchParameter catchParameter, Object o) {

    }

    @Override
    public void visit(Resource resource, Object o) {

    }

    @Override
    public void visit(LambdaExpr lambdaExpr, Object o) {

    }

    @Override
    public void visit(MethodReferenceExpr methodReferenceExpr, Object o) {

    }

}
