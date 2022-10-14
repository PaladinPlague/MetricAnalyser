import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@SuppressWarnings("DuplicatedCode")
public class LCOMVisitor extends MetricVisitor<LCOMReturnType> {
    protected LCOMReturnType initialiseEmpty() {
        return LCOMReturnType.empty();
    }

    protected LCOMReturnType combineResults(LCOMReturnType first, LCOMReturnType second) {
        return first.aggregate(second);
    }
    protected LCOMReturnType getReturn(LCOMReturnType possibleReturn) {
        return possibleReturn.isNonEmpty() ? possibleReturn : null;
    }

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
        LCOMReturnType classType = new LCOMReturnType(methodsReturn.getTotalVariableUses(), fieldNames);

        // aggregate with inner classes
        for(ClassOrInterfaceDeclaration coid : innerClassesList) {
            classType = classType.aggregate(coid.accept(this, arg));
        }

        if(n.isTopLevelType()) {
            // Reduce maps and calculate LCOM, then add to arg
            LCOMReturnType reduced = classType.reduceListToFields();

            ClassMetricsResult cmr = Utils.getOrCreateCMRInList(n, arg);
            cmr.setLcom(reduced.calculateLCOM());
        }


        return classType;
    }

    @Override
    public LCOMReturnType visit(final MethodDeclaration n, final List<ClassMetricsResult> arg) {
        if(n.getBody().isEmpty()) {
            //empty body does not use any fields
            return new LCOMReturnType(new HashSet<>(), true);
        }

        Set<VariableUse> allVarsUsed;
        Set<String> parameters;

        LCOMReturnType parameterReturn = n.getParameters().accept(this, arg);

        parameters = parameterReturn != null ? VariableUse.convertVarUseSetToStrings(parameterReturn.getNameSet()) : new HashSet<>();

        allVarsUsed = n.getBody().get().accept(this, arg).getNameSet();
        allVarsUsed = allVarsUsed == null ? new HashSet<>() : allVarsUsed;

        Set<VariableUse> allVarsWithoutParameters = VariableUse.removeVarsFromSet(parameters, allVarsUsed);

        return new LCOMReturnType(allVarsWithoutParameters, true);
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
        LCOMReturnType toReturn = new LCOMReturnType(new HashSet<>(), false);
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
}
