import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CBOVisitor extends MetricVisitor<CBOReturnType> {
    protected CBOReturnType initialiseEmpty() {
        return CBOReturnType.empty();
    }
    protected CBOReturnType combineResults(CBOReturnType first, CBOReturnType second) {
        return first.aggregate(second);
    }
    protected CBOReturnType getReturn(CBOReturnType possibleReturn) {
        return possibleReturn.isNonEmpty() ? possibleReturn : null;
    }

    @Override
    public CBOReturnType visit(final ClassOrInterfaceDeclaration n, final List<ClassMetricsResult> arg) {
        CBOReturnType result;
        CBOReturnType toReturn = initialiseEmpty();

        result = n.getTypeParameters().accept(this, arg);
        if (result != null)
            toReturn =  combineResults(toReturn, result);

        result = n.getMembers().accept(this, arg);
        if (result != null)
            toReturn = combineResults(toReturn, result);

        for(BodyDeclaration<?> td : n.getMembers()) {
            if(td.isClassOrInterfaceDeclaration()) {
                toReturn.removeInnerClass(td.asClassOrInterfaceDeclaration().getNameAsString());
            }
        }

        toReturn.setAssociatedCoid(n);

        return getReturn(toReturn);
    }

    @Override
    public CBOReturnType visit(final MethodDeclaration n, final List<ClassMetricsResult> arg) {
        CBOReturnType result;
        CBOReturnType toReturn = initialiseEmpty();

        if (n.getBody().isPresent()) {
            result = n.getBody().get().accept(this, arg);
            if (result != null)
                toReturn = combineResults(toReturn, result);
        }

        result = n.getType().accept(this, arg);
        if (result != null)
            toReturn = combineResults(toReturn, result);

        result = n.getParameters().accept(this, arg);
        if (result != null)
            toReturn = combineResults(toReturn, result);

        result = n.getTypeParameters().accept(this, arg);
        if (result != null)
            toReturn = combineResults(toReturn, result);

        return getReturn(toReturn);
    }


    @Override
    public CBOReturnType visit(final ClassOrInterfaceType n, final List<ClassMetricsResult> arg) {
        CBOReturnType result;

        Set<String> nameOfType = new HashSet<>();
        nameOfType.add(n.getNameAsString());
        CBOReturnType toReturn = new CBOReturnType(nameOfType);

        if (n.getScope().isPresent()) {
            result = n.getScope().get().accept(this, arg);
            if (result != null)
                toReturn = combineResults(toReturn, result);
        }
        if (n.getTypeArguments().isPresent()) {
            result = n.getTypeArguments().get().accept(this, arg);
            if (result != null)
                toReturn = combineResults(toReturn, result);
        }
        return getReturn(toReturn);
    }

    @Override
    public CBOReturnType visit(final MethodCallExpr n, final List<ClassMetricsResult> arg) {
        CBOReturnType toReturn = initialiseEmpty();
        CBOReturnType result;

        result = n.getArguments().accept(this, arg);
        if (result != null)
            toReturn = combineResults(toReturn, result);

        if (n.getScope().isPresent()) {
            if(n.getScope().get().isNameExpr()) {
                // assume it is a static call, it will be filtered out later (hopefully) if it turns out not to be
                toReturn = combineResults(toReturn, new CBOReturnType(n.getScope().get().asNameExpr().getNameAsString()));
            } else {
                result = n.getScope().get().accept(this, arg);
                if (result != null)
                    toReturn = combineResults(toReturn, result);
            }
        }
        return getReturn(toReturn);
    }

    @Override
    public CBOReturnType visit(final ObjectCreationExpr n, final List<ClassMetricsResult> arg) {
        CBOReturnType toReturn = initialiseEmpty();
        CBOReturnType result;

        result = n.getArguments().accept(this, arg);
        if (result != null)
            toReturn = combineResults(toReturn, result);

        if (n.getScope().isPresent()) {
            result = n.getScope().get().accept(this, arg);
            if (result != null)
                toReturn = combineResults(toReturn, result);
        }

        result = n.getType().accept(this, arg);
        if (result != null)
            toReturn = combineResults(toReturn, result);

        if (n.getTypeArguments().isPresent()) {
            result = n.getTypeArguments().get().accept(this, arg);
            if (result != null)
                toReturn = combineResults(toReturn, result);
        }

        return getReturn(toReturn);
    }
}
