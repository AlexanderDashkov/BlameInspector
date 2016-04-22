package blameinspector.vcs;

import com.github.antlrjavaparser.api.Comment;
import com.github.antlrjavaparser.api.body.*;
import com.github.antlrjavaparser.api.expr.LambdaExpr;
import com.github.antlrjavaparser.api.expr.MethodReferenceExpr;
import com.github.antlrjavaparser.api.visitor.VoidVisitorAdapter;

public class VoidVisitorImpl extends VoidVisitorAdapter {

    private boolean isClassFound;
    private boolean isMethodFound;

    private String className;
    private String methodName;


    public VoidVisitorImpl(final String className, final String methodName){
        isClassFound = false;
        isMethodFound = false;
        this.className = className;
        this.methodName = methodName;
    }

    public boolean isFound(){
        return isClassFound && isMethodFound;
    }



    @Override
    public void visit(ClassOrInterfaceDeclaration classOrInterfaceDeclaration, Object o) {
        if (classOrInterfaceDeclaration.getName().equals(className)){
            isClassFound = true;
            for (BodyDeclaration member : classOrInterfaceDeclaration.getMembers()){
                member.accept(this, null);
            }
        }
    }

    @Override
    public void visit(Comment comment, Object o) {

    }


    @Override
    public void visit(ConstructorDeclaration constructorDeclaration, Object o) {
         if (constructorDeclaration.getName().equals(methodName)){
             isMethodFound = true;
         }
    }

    @Override
    public void visit(MethodDeclaration methodDeclaration, Object o) {
        if (methodDeclaration.getName().equals(methodName)){
            isMethodFound = true;
        }

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
