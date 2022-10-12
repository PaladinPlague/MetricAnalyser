import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.visitor.GenericVisitorAdapter;
import java.util.List;

@SuppressWarnings("DuplicatedCode")
public class LCOMVisitor extends GenericVisitorAdapter<LCOMReturnType, List<ClassMetricsResult>> {
    @Override
    public LCOMReturnType visit(final ClassOrInterfaceDeclaration n, final List<ClassMetricsResult> arg) {
        NodeList<BodyDeclaration<?>> allMembers = n.getMembers();

        NodeList<FieldDeclaration> fieldsList = new NodeList<>();
        NodeList<MethodDeclaration> methodsList = new NodeList<>();
        NodeList<ClassOrInterfaceDeclaration> innerClassesList = new NodeList<>();

        for(BodyDeclaration<?> bd : allMembers) {
            if(bd.isFieldDeclaration()) {
                fieldsList.add(bd.asFieldDeclaration());
            } else if(bd.isMethodDeclaration()) {
                methodsList.add(bd.asMethodDeclaration());
            } else if(bd.isClassOrInterfaceDeclaration()) {
                innerClassesList.add(bd.asClassOrInterfaceDeclaration());
            }
        }

        LCOMReturnType fieldsReturn = fieldsList.accept(this, arg);

        // pseudo-class level
        LCOMReturnType methodsReturn = methodsList.accept(this, arg);

        // construct class level type
        LCOMReturnType classType = new LCOMReturnType(methodsReturn.getLCOMMap(), fieldsReturn.getNameSet());

        // aggregate with inner classes
        for(ClassOrInterfaceDeclaration coid : innerClassesList) {
            classType.aggregate(coid.accept(this, arg));
        }

        if(!n.isInnerClass()) {
            // TODO : Complete
            // Reduce sets and calculate LCOM! Then add to arg
        }

        // unused for top level classes - used in aggregation for inner classes
        return classType;
    }

    @Override
    public LCOMReturnType visit(final MethodDeclaration n, final List<ClassMetricsResult> arg) {
        // TODO : Complete

        LCOMReturnType toReturn = LCOMReturnType.empty();
        LCOMReturnType bodyResult;
        LCOMReturnType parameters;

        if (n.getBody().isPresent()) {
            bodyResult = n.getBody().get().accept(this, arg);
        }

        parameters = n.getParameters().accept(this, arg);

        return null;
    }

    @Override
    public LCOMReturnType visit(final FieldDeclaration n, final List<ClassMetricsResult> arg) {
        LCOMReturnType toReturn = LCOMReturnType.empty();
        LCOMReturnType result;

        for(VariableDeclarator vd : n.getVariables()) {
            result = vd.getName().accept(this, arg);
            if (result != null)
                toReturn = toReturn.aggregate(result);
        }

        return toReturn.isNonEmpty() ? toReturn : null;
    }

    @Override
    public LCOMReturnType visit(final SimpleName n, final List<ClassMetricsResult> arg) {
        return new LCOMReturnType(new VariableUse(n.asString()));
    }

    @Override
    public LCOMReturnType visit(final FieldAccessExpr n, final List<ClassMetricsResult> arg) {
        if(n.getScope().isThisExpr()) {
            return new LCOMReturnType(new VariableUse(n.getNameAsString(), true));
        }

        return n.getScope().accept(this, arg);
    }

    // All of the following visit overrides just make sure that all the parts of the relevant expressions
    // are visited and the results are joined together


    @Override
    public LCOMReturnType visit(final AssignExpr n, final List<ClassMetricsResult> arg) {
        LCOMReturnType toReturn = LCOMReturnType.empty();
        LCOMReturnType result;

        result = n.getTarget().accept(this, arg);
        if (result != null)
            toReturn = toReturn.aggregate(result);

        result = n.getValue().accept(this, arg);
        if (result != null)
            toReturn = toReturn.aggregate(result);

        return toReturn.isNonEmpty() ? toReturn : null;
    }

    @Override
    public LCOMReturnType visit(final BinaryExpr n, final List<ClassMetricsResult> arg) {
        LCOMReturnType toReturn = LCOMReturnType.empty();
        LCOMReturnType result;

        result = n.getLeft().accept(this, arg);
        if (result != null)
            toReturn = toReturn.aggregate(result);

        result = n.getRight().accept(this, arg);
        if (result != null)
            toReturn = toReturn.aggregate(result);

        return toReturn.isNonEmpty() ? toReturn : null;
    }

    @Override
    public LCOMReturnType visit(final ConditionalExpr n, final List<ClassMetricsResult> arg) {
        LCOMReturnType toReturn = LCOMReturnType.empty();
        LCOMReturnType result;

        result = n.getCondition().accept(this, arg);
        if (result != null)
            toReturn = toReturn.aggregate(result);

        result = n.getElseExpr().accept(this, arg);
        if (result != null)
            toReturn = toReturn.aggregate(result);

        result = n.getThenExpr().accept(this, arg);
        if (result != null)
            toReturn = toReturn.aggregate(result);

        return toReturn.isNonEmpty() ? toReturn : null;
    }

    @Override
    public LCOMReturnType visit(final DoStmt n, final List<ClassMetricsResult> arg) {
        LCOMReturnType toReturn = LCOMReturnType.empty();
        LCOMReturnType result;

        result = n.getBody().accept(this, arg);
        if (result != null)
            toReturn = toReturn.aggregate(result);

        result = n.getCondition().accept(this, arg);
        if (result != null)
            toReturn = toReturn.aggregate(result);

        return toReturn.isNonEmpty() ? toReturn : null;
    }

    @Override
    public LCOMReturnType visit(final ForEachStmt n, final List<ClassMetricsResult> arg) {
        LCOMReturnType toReturn = LCOMReturnType.empty();
        LCOMReturnType result;

        result = n.getBody().accept(this, arg);
        if (result != null)
            toReturn = toReturn.aggregate(result);

        result = n.getIterable().accept(this, arg);
        if (result != null)
            toReturn = toReturn.aggregate(result);

        result = n.getVariable().accept(this, arg);
        if (result != null)
            toReturn = toReturn.aggregate(result);

        return toReturn.isNonEmpty() ? toReturn : null;
    }

    @Override
    public LCOMReturnType visit(final ForStmt n, final List<ClassMetricsResult> arg) {
        LCOMReturnType toReturn = LCOMReturnType.empty();
        LCOMReturnType result;

        result = n.getBody().accept(this, arg);
        if (result != null)
            toReturn = toReturn.aggregate(result);

        if (n.getCompare().isPresent()) {
            result = n.getCompare().get().accept(this, arg);
            if (result != null)
                toReturn = toReturn.aggregate(result);
        }

        result = n.getInitialization().accept(this, arg);
        if (result != null)
            toReturn = toReturn.aggregate(result);

        result = n.getUpdate().accept(this, arg);
        if (result != null)
            toReturn = toReturn.aggregate(result);

        return toReturn.isNonEmpty() ? toReturn : null;
    }

    @Override
    public LCOMReturnType visit(final IfStmt n, final List<ClassMetricsResult> arg) {
        LCOMReturnType toReturn = LCOMReturnType.empty();
        LCOMReturnType result;

        result = n.getCondition().accept(this, arg);
        if (result != null)
            toReturn = toReturn.aggregate(result);

        if (n.getElseStmt().isPresent()) {
            result = n.getElseStmt().get().accept(this, arg);
            if (result != null)
                toReturn = toReturn.aggregate(result);
        }

        result = n.getThenStmt().accept(this, arg);
        if (result != null)
            toReturn = toReturn.aggregate(result);

        return toReturn.isNonEmpty() ? toReturn : null;
    }

    @Override
    public LCOMReturnType visit(final MethodCallExpr n, final List<ClassMetricsResult> arg) {
        LCOMReturnType toReturn = LCOMReturnType.empty();
        LCOMReturnType result;

        result = n.getArguments().accept(this, arg);
        if (result != null)
            toReturn = toReturn.aggregate(result);

        if (n.getScope().isPresent()) {
            result = n.getScope().get().accept(this, arg);
            if (result != null)
                toReturn = toReturn.aggregate(result);
        }
        return toReturn.isNonEmpty() ? toReturn : null;
    }

    @Override
    public LCOMReturnType visit(final ObjectCreationExpr n, final List<ClassMetricsResult> arg) {
        LCOMReturnType toReturn = LCOMReturnType.empty();
        LCOMReturnType result;

        if (n.getAnonymousClassBody().isPresent()) {
            result = n.getAnonymousClassBody().get().accept(this, arg);
            if (result != null)
                toReturn = toReturn.aggregate(result);
        }

        result = n.getArguments().accept(this, arg);
        if (result != null)
            toReturn = toReturn.aggregate(result);

        if (n.getScope().isPresent()) {
            result = n.getScope().get().accept(this, arg);
            if (result != null)
                toReturn = toReturn.aggregate(result);
        }
        return toReturn.isNonEmpty() ? toReturn : null;
    }

    @Override
    public LCOMReturnType visit(final SwitchEntry n, final List<ClassMetricsResult> arg) {
        LCOMReturnType toReturn = LCOMReturnType.empty();
        LCOMReturnType result;

        result = n.getLabels().accept(this, arg);
        if (result != null)
            toReturn = toReturn.aggregate(result);

        result = n.getStatements().accept(this, arg);
        if (result != null)
            toReturn = toReturn.aggregate(result);

        return toReturn.isNonEmpty() ? toReturn : null;
    }

    @Override
    public LCOMReturnType visit(final SwitchStmt n, final List<ClassMetricsResult> arg) {
        LCOMReturnType toReturn = LCOMReturnType.empty();
        LCOMReturnType result;

        result = n.getEntries().accept(this, arg);
        if (result != null)
            toReturn = toReturn.aggregate(result);

        result = n.getSelector().accept(this, arg);
        if (result != null)
            toReturn = toReturn.aggregate(result);

        return toReturn.isNonEmpty() ? toReturn : null;
    }

    @Override
    public LCOMReturnType visit(final TryStmt n, final List<ClassMetricsResult> arg) {
        LCOMReturnType toReturn = LCOMReturnType.empty();
        LCOMReturnType result;

        result = n.getCatchClauses().accept(this, arg);
        if (result != null)
            toReturn = toReturn.aggregate(result);

        if (n.getFinallyBlock().isPresent()) {
            result = n.getFinallyBlock().get().accept(this, arg);
            if (result != null)
                toReturn = toReturn.aggregate(result);
        }

        result = n.getResources().accept(this, arg);
        if (result != null)
            toReturn = toReturn.aggregate(result);


        result = n.getTryBlock().accept(this, arg);
        if (result != null)
            toReturn = toReturn.aggregate(result);

        return toReturn.isNonEmpty() ? toReturn : null;
    }

    @Override
    public LCOMReturnType visit(final VariableDeclarationExpr n, final List<ClassMetricsResult> arg) {
        LCOMReturnType toReturn = LCOMReturnType.empty();
        LCOMReturnType result;

        result = n.getVariables().accept(this, arg);
        if (result != null)
            toReturn = toReturn.aggregate(result);

        return toReturn.isNonEmpty() ? toReturn : null;
    }

    @Override
    public LCOMReturnType visit(final VariableDeclarator n, final List<ClassMetricsResult> arg) {
        LCOMReturnType toReturn = LCOMReturnType.empty();
        LCOMReturnType result;
        if (n.getInitializer().isPresent()) {
            result = n.getInitializer().get().accept(this, arg);
            if (result != null)
                toReturn = toReturn.aggregate(result);
        }

        result = n.getName().accept(this, arg);
        if (result != null)
            toReturn = toReturn.aggregate(result);

        return toReturn.isNonEmpty() ? toReturn : null;
    }

    @Override
    public LCOMReturnType visit(final WhileStmt n, final List<ClassMetricsResult> arg) {
        LCOMReturnType toReturn = LCOMReturnType.empty();
        LCOMReturnType result;

        result = n.getBody().accept(this, arg);
        if (result != null)
            toReturn = toReturn.aggregate(result);

        result = n.getCondition().accept(this, arg);
        if (result != null)
            toReturn = toReturn.aggregate(result);

        return toReturn.isNonEmpty() ? toReturn : null;
    }

    @Override
    public LCOMReturnType visit(NodeList n, List<ClassMetricsResult> arg) {
        LCOMReturnType toReturn = LCOMReturnType.empty();
        LCOMReturnType result;
        for (final Object v : n) {
            result = ((Node) v).accept(this, arg);
            if (result != null) {
                toReturn = toReturn.aggregate(result);
            }
        }
        return toReturn.isNonEmpty() ? toReturn : null;
    }
}
