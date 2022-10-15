import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;

import java.util.List;

public class RFCVisitor extends MetricVisitor<RFCReturnType> {
    protected RFCReturnType initialiseEmpty() {
        return RFCReturnType.empty();
    }
    protected RFCReturnType combineResults(RFCReturnType first, RFCReturnType second) {
        return first.aggregate(second);
    }
    protected RFCReturnType getReturn(RFCReturnType possibleReturn) {
        return possibleReturn.isNonEmpty() ? possibleReturn : null;
    }


    public RFCReturnType visit(ClassOrInterfaceDeclaration coid, List<ClassMetricsResult> arg) {
        NodeList<BodyDeclaration<?>> allMembers = coid.getMembers();
        NodeList<MethodDeclaration> methodsList = new NodeList<>();
        NodeList<ClassOrInterfaceDeclaration> innerClassesList = new NodeList<>();

        for(BodyDeclaration<?> bd : allMembers) {
            if(bd.isMethodDeclaration()) {
                methodsList.add(bd.asMethodDeclaration());
            } else if(bd.isClassOrInterfaceDeclaration()) {
                innerClassesList.add(bd.asClassOrInterfaceDeclaration());
            }

        }

        RFCReturnType toReturn = initialiseEmpty();
        RFCReturnType result = methodsList.accept(this, arg);

        if(result != null) {
            toReturn = combineResults(toReturn, result);
        }

        toReturn.setNumberOfOwnMethods(methodsList.size());

        RFCReturnType fromInner = innerClassesList.accept(this, arg);

        if(fromInner != null) {
            toReturn = combineResults(toReturn, fromInner);
        }

        if(!coid.isTopLevelType()) {
            return toReturn;
        }
        ClassMetricsResult cmr = Utils.getOrCreateCMRInList(coid,arg);
        cmr.setRfc(toReturn.calculateRfc());

        return toReturn;
    }



    @Override
    public RFCReturnType visit(MethodDeclaration md, List<ClassMetricsResult> arg) {
        RFCReturnType result;
        RFCReturnType s = initialiseEmpty();

        result = super.visit(md, arg);
        if(result != null) {
            s = combineResults(s, result);
        }

        return s;
    }

    @Override
    public RFCReturnType visit(MethodCallExpr call, List<ClassMetricsResult> arg) {
        RFCReturnType result;
        RFCReturnType toReturn = initialiseEmpty();

        result = super.visit(call, arg);
        if (result != null) {
            toReturn = combineResults(toReturn, result);
        }

        if(call.getScope().isEmpty() || call.getScope().get().isThisExpr()) {
            return getReturn(toReturn);
        } else {
            toReturn.addSingleMethodName(call.getNameAsString());
            return toReturn;
        }
    }



}
