import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.visitor.GenericVisitorAdapter;

import java.util.List;

public abstract class MetricVisitor<R> extends GenericVisitorAdapter<R, List<ClassMetricsResult>> {
    protected abstract R initialiseEmpty();
    protected abstract R combineResults(R first, R second);
    protected abstract R getReturn(R possibleReturn);

    @Override
    public R visit(final ArrayAccessExpr n, final List<ClassMetricsResult> arg) {
        R toReturn = initialiseEmpty();
        R result;

        result = n.getIndex().accept(this, arg);
        if (result != null)
            toReturn = combineResults(toReturn, result);

        result = n.getName().accept(this, arg);
        if (result != null)
            toReturn = combineResults(toReturn, result);

        return getReturn(toReturn);
    }

    @Override
    public R visit(final AssignExpr n, final List<ClassMetricsResult> arg) {
        R toReturn = initialiseEmpty();
        R result;

        result = n.getTarget().accept(this, arg);
        if (result != null)
            toReturn = combineResults(toReturn, result);

        result = n.getValue().accept(this, arg);
        if (result != null)
            toReturn = combineResults(toReturn, result);

        return getReturn(toReturn);
    }

    @Override
    public R visit(final BinaryExpr n, final List<ClassMetricsResult> arg) {
        R toReturn = initialiseEmpty();
        R result;

        result = n.getLeft().accept(this, arg);
        if (result != null)
            toReturn = combineResults(toReturn, result);

        result = n.getRight().accept(this, arg);
        if (result != null)
            toReturn = combineResults(toReturn, result);

        return getReturn(toReturn);
    }

    @Override
    public R visit(final ConditionalExpr n, final List<ClassMetricsResult> arg) {
        R toReturn = initialiseEmpty();
        R result;

        result = n.getCondition().accept(this, arg);
        if (result != null)
            toReturn = combineResults(toReturn, result);

        result = n.getElseExpr().accept(this, arg);
        if (result != null)
            toReturn = combineResults(toReturn, result);

        result = n.getThenExpr().accept(this, arg);
        if (result != null)
            toReturn = combineResults(toReturn, result);

        return getReturn(toReturn);
    }

    @Override
    public R visit(final DoStmt n, final List<ClassMetricsResult> arg) {
        R toReturn = initialiseEmpty();
        R result;

        result = n.getCondition().accept(this, arg);
        if (result != null)
            toReturn = combineResults(toReturn, result);

        result = n.getBody().accept(this, arg);
        if (result != null)
            toReturn = combineResults(toReturn, result);



        return getReturn(toReturn);
    }

    @Override
    public R visit(final ForEachStmt n, final List<ClassMetricsResult> arg) {
        R toReturn = initialiseEmpty();
        R result;

        result = n.getVariable().accept(this, arg);
        if (result != null)
            toReturn = combineResults(toReturn, result);

        result = n.getIterable().accept(this, arg);
        if (result != null)
            toReturn = combineResults(toReturn, result);

        result = n.getBody().accept(this, arg);
        if (result != null)
            toReturn = combineResults(toReturn, result);

        return getReturn(toReturn);
    }

    @Override
    public R visit(final ForStmt n, final List<ClassMetricsResult> arg) {
        R toReturn = initialiseEmpty();
        R result;

        result = n.getInitialization().accept(this, arg);
        if (result != null)
            toReturn = combineResults(toReturn, result);

        if (n.getCompare().isPresent()) {
            result = n.getCompare().get().accept(this, arg);
            if (result != null)
                toReturn = combineResults(toReturn, result);
        }

        result = n.getUpdate().accept(this, arg);
        if (result != null)
            toReturn = combineResults(toReturn, result);

        result = n.getBody().accept(this, arg);
        if (result != null)
            toReturn = combineResults(toReturn, result);

        return getReturn(toReturn);
    }

    @Override
    public R visit(final IfStmt n, final List<ClassMetricsResult> arg) {
        R toReturn = initialiseEmpty();
        R result;

        result = n.getCondition().accept(this, arg);
        if (result != null)
            toReturn = combineResults(toReturn, result);

        result = n.getThenStmt().accept(this, arg);
        if (result != null)
            toReturn = combineResults(toReturn, result);

        if (n.getElseStmt().isPresent()) {
            result = n.getElseStmt().get().accept(this, arg);
            if (result != null)
                toReturn = combineResults(toReturn, result);
        }


        return getReturn(toReturn);
    }

    @Override
    public R visit(final MethodCallExpr n, final List<ClassMetricsResult> arg) {
        R toReturn = initialiseEmpty();
        R result;

        result = n.getArguments().accept(this, arg);
        if (result != null)
            toReturn = combineResults(toReturn, result);

        if (n.getScope().isPresent()) {
            result = n.getScope().get().accept(this, arg);
            if (result != null)
                toReturn = combineResults(toReturn, result);
        }
        return getReturn(toReturn);
    }

    @Override
    public R visit(final ObjectCreationExpr n, final List<ClassMetricsResult> arg) {
        R toReturn = initialiseEmpty();
        R result;

        if (n.getAnonymousClassBody().isPresent()) {
            result = n.getAnonymousClassBody().get().accept(this, arg);
            if (result != null)
                toReturn = combineResults(toReturn, result);
        }

        result = n.getArguments().accept(this, arg);
        if (result != null)
            toReturn = combineResults(toReturn, result);

        if (n.getScope().isPresent()) {
            result = n.getScope().get().accept(this, arg);
            if (result != null)
                toReturn = combineResults(toReturn, result);
        }
        return getReturn(toReturn);
    }

    @Override
    public R visit(final SwitchEntry n, final List<ClassMetricsResult> arg) {
        R toReturn = initialiseEmpty();
        R result;

        result = n.getLabels().accept(this, arg);
        if (result != null)
            toReturn = combineResults(toReturn, result);

        result = n.getStatements().accept(this, arg);
        if (result != null)
            toReturn = combineResults(toReturn, result);

        return getReturn(toReturn);
    }

    @Override
    public R visit(final SwitchStmt n, final List<ClassMetricsResult> arg) {
        R toReturn = initialiseEmpty();
        R result;

        result = n.getEntries().accept(this, arg);
        if (result != null)
            toReturn = combineResults(toReturn, result);

        result = n.getSelector().accept(this, arg);
        if (result != null)
            toReturn = combineResults(toReturn, result);

        return getReturn(toReturn);
    }

    @Override
    public R visit(final TryStmt n, final List<ClassMetricsResult> arg) {
        R toReturn = initialiseEmpty();
        R result;

        result = n.getCatchClauses().accept(this, arg);
        if (result != null)
            toReturn = combineResults(toReturn, result);

        if (n.getFinallyBlock().isPresent()) {
            result = n.getFinallyBlock().get().accept(this, arg);
            if (result != null)
                toReturn = combineResults(toReturn, result);
        }

        result = n.getResources().accept(this, arg);
        if (result != null)
            toReturn = combineResults(toReturn, result);


        result = n.getTryBlock().accept(this, arg);
        if (result != null)
            toReturn = combineResults(toReturn, result);

        return getReturn(toReturn);
    }

    @Override
    public R visit(final VariableDeclarator n, final List<ClassMetricsResult> arg) {
        R toReturn = initialiseEmpty();
        R result;
        if (n.getInitializer().isPresent()) {
            result = n.getInitializer().get().accept(this, arg);
            if (result != null)
                toReturn = combineResults(toReturn, result);
        }

        result = n.getName().accept(this, arg);
        if (result != null)
            toReturn = combineResults(toReturn, result);

        return getReturn(toReturn);
    }

    @Override
    public R visit(final WhileStmt n, final List<ClassMetricsResult> arg) {
        R toReturn = initialiseEmpty();
        R result;

        result = n.getBody().accept(this, arg);
        if (result != null)
            toReturn = combineResults(toReturn, result);

        result = n.getCondition().accept(this, arg);
        if (result != null)
            toReturn = combineResults(toReturn, result);

        return getReturn(toReturn);
    }

    @Override
    public R visit(NodeList n, List<ClassMetricsResult> arg) {
        R toReturn = initialiseEmpty();
        R result;
        for (final Object v : n) {
            result = ((Node) v).accept(this, arg);
            if (result != null) {
                toReturn = combineResults(toReturn, result);
            }
        }
        return getReturn(toReturn);
    }
}
