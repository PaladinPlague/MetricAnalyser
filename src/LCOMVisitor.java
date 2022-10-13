import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.visitor.GenericVisitorAdapter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@SuppressWarnings("DuplicatedCode")
public class LCOMVisitor extends GenericVisitorAdapter<LCOMReturnType, List<ClassMetricsResult>> {
    @Override
    public LCOMReturnType visit(final ClassOrInterfaceDeclaration n, final List<ClassMetricsResult> arg) {
        if(n.isInterface()) {
            ClassMetricsResult cmr = Utils.getOrCreateCMRInList(n, arg);
            cmr.setLcom(0); // LCOM for interface is trivially 0
            return LCOMReturnType.empty();
        }

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

        if(fieldsList.size() == 0) {
            ClassMetricsResult cmr = Utils.getOrCreateCMRInList(n, arg);
            cmr.setLcom(0); // LCOM for class with no fields is trivially 0
            return LCOMReturnType.empty();
        }

        LCOMReturnType fieldsReturn = fieldsList.accept(this, arg);

        // pseudo-class level
        LCOMReturnType methodsReturn = methodsList.accept(this, arg);

        Set<String> fieldNames = VariableUse.convertVarUseSetToStrings(fieldsReturn.getNameSet());

        // construct class level type
        LCOMReturnType classType = new LCOMReturnType(methodsReturn.getLCOMMap(), fieldNames);

        // aggregate with inner classes
        for(ClassOrInterfaceDeclaration coid : innerClassesList) {
            classType = classType.aggregate(coid.accept(this, arg));
        }

        if(n.isTopLevelType()) {
            // Reduce maps and calculate LCOM, then add to arg
            LCOMReturnType reduced = classType.reduceMapsToFields();

            ClassMetricsResult cmr = Utils.getOrCreateCMRInList(n, arg);
            cmr.setLcom(reduced.calculateLCOM());
        }


        return classType;
    }

    @Override
    public LCOMReturnType visit(final MethodDeclaration n, final List<ClassMetricsResult> arg) {
        if(n.getBody().isEmpty()) {
            //empty body does not use any fields
            return new LCOMReturnType(n, new HashSet<>());
        }

        Set<VariableUse> allVarsUsed;
        Set<String> parameters;

        LCOMReturnType parameterReturn = n.getParameters().accept(this, arg);

        parameters = parameterReturn != null ? VariableUse.convertVarUseSetToStrings(parameterReturn.getNameSet()) : new HashSet<>();

        allVarsUsed = n.getBody().get().accept(this, arg).getNameSet();
        allVarsUsed = allVarsUsed == null ? new HashSet<>() : allVarsUsed;

        Set<VariableUse> allVarsWithoutParameters = VariableUse.removeVarsFromSet(parameters, allVarsUsed);

        return new LCOMReturnType(n, allVarsWithoutParameters);
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

    @Override
    public LCOMReturnType visit(final VariableDeclarationExpr n, final List<ClassMetricsResult> arg) {
        // visit the initializers the usual way, but add each declared variable as a local variable

        // do not use empty - we do not want to lose our local variables when aggregating with the next statement
        // if the variable declarator initializers did not use any named variables
        LCOMReturnType toReturn = new LCOMReturnType(new HashSet<>());
        LCOMReturnType result;

        Set<String> localVarNames = new HashSet<>();

        for(VariableDeclarator vd : n.getVariables()) {
            localVarNames.add(vd.getNameAsString());

            result = vd.getInitializer().isPresent() ? vd.getInitializer().get().accept(this, arg) : null;
            if (result != null)
                toReturn = toReturn.aggregate(result);
        }

        toReturn.setLocalVars(localVarNames);

        return toReturn;
    }

    @Override
    public LCOMReturnType visit(final BlockStmt n, final List<ClassMetricsResult> arg) {
        LCOMReturnType toReturn = LCOMReturnType.empty();
        LCOMReturnType result;

        result = n.getStatements().accept(this, arg);
        if (result != null)
            toReturn = toReturn.aggregate(result);


        toReturn.resetLocalVars();
        return toReturn;
    }

    @Override
    public LCOMReturnType visit(final Parameter n, final List<ClassMetricsResult> arg) {
        return n.getName().accept(this, arg);
    }

    // All of the following visit overrides just make sure that all the parts of the relevant expressions
    // are visited and the results are joined together


    @Override
    public LCOMReturnType visit(final ArrayAccessExpr n, final List<ClassMetricsResult> arg) {
        LCOMReturnType toReturn = LCOMReturnType.empty();
        LCOMReturnType result;

        result = n.getIndex().accept(this, arg);
        if (result != null)
            toReturn = toReturn.aggregate(result);

        result = n.getName().accept(this, arg);
        if (result != null)
            toReturn = toReturn.aggregate(result);

        return toReturn.isNonEmpty() ? toReturn : null;
    }

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

        result = n.getCondition().accept(this, arg);
        if (result != null)
            toReturn = toReturn.aggregate(result);

        result = n.getBody().accept(this, arg);
        if (result != null)
            toReturn = toReturn.aggregate(result);



        return toReturn.isNonEmpty() ? toReturn : null;
    }

    @Override
    public LCOMReturnType visit(final ForEachStmt n, final List<ClassMetricsResult> arg) {
        LCOMReturnType toReturn = LCOMReturnType.empty();
        LCOMReturnType result;

        result = n.getVariable().accept(this, arg);
        if (result != null)
            toReturn = toReturn.aggregate(result);

        result = n.getIterable().accept(this, arg);
        if (result != null)
            toReturn = toReturn.aggregate(result);

        result = n.getBody().accept(this, arg);
        if (result != null)
            toReturn = toReturn.aggregate(result);

        return toReturn.isNonEmpty() ? toReturn : null;
    }

    @Override
    public LCOMReturnType visit(final ForStmt n, final List<ClassMetricsResult> arg) {
        LCOMReturnType toReturn = LCOMReturnType.empty();
        LCOMReturnType result;

        result = n.getInitialization().accept(this, arg);
        if (result != null)
            toReturn = toReturn.aggregate(result);

        if (n.getCompare().isPresent()) {
            result = n.getCompare().get().accept(this, arg);
            if (result != null)
                toReturn = toReturn.aggregate(result);
        }

        result = n.getUpdate().accept(this, arg);
        if (result != null)
            toReturn = toReturn.aggregate(result);

        result = n.getBody().accept(this, arg);
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

        result = n.getThenStmt().accept(this, arg);
        if (result != null)
            toReturn = toReturn.aggregate(result);

        if (n.getElseStmt().isPresent()) {
            result = n.getElseStmt().get().accept(this, arg);
            if (result != null)
                toReturn = toReturn.aggregate(result);
        }


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
