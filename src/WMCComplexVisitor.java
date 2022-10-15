import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.visitor.GenericVisitorAdapter;

import java.util.Arrays;
import java.util.List;

public class WMCComplexVisitor extends GenericVisitorAdapter<WMCCReturnType, List<ClassMetricsResult>> {

    public WMCCReturnType visit(ClassOrInterfaceDeclaration coid, List<ClassMetricsResult> arg) {
		WMCCReturnType result;

        result = super.visit(coid, arg);

        if(!coid.isTopLevelType()) {
        	return result;
        }
        ClassMetricsResult cmr = Utils.getOrCreateCMRInList(coid, arg);

		cmr.setWmc_c(result.getComplexity());

        return result;
    }
    
    @Override
    public WMCCReturnType visit(ConstructorDeclaration cd, List<ClassMetricsResult> arg) {
    	return null;
    }

	@Override
    public WMCCReturnType visit(MethodDeclaration md, List<ClassMetricsResult> arg) {
		WMCCReturnType result = super.visit(md, arg);

		if(result != null) {
			return new WMCCReturnType(result.getComplexity()+1);
		}

		return new WMCCReturnType(1);
    }
	
	@Override 
	public WMCCReturnType visit(IfStmt n, List<ClassMetricsResult> arg) {
		WMCCReturnType total = WMCCReturnType.empty();
		WMCCReturnType result;
		
		result = n.getCondition().accept(this, arg);
		if(result != null) {
			total = total.aggregate(result);
			total.confirmIfCondition();
		}
		
		if(n.getElseStmt().isPresent()) {
			result = n.getElseStmt().get().accept(this, arg);
			if(result != null) {
				total = total.aggregate(result);
			}
		}
		
		result = n.getThenStmt().accept(this, arg);
		if(result != null) {
			total = total.aggregate(result);
		}
		
		return total.aggregate(new WMCCReturnType(1));
	}
	
	@Override
	public WMCCReturnType visit(BinaryExpr n, List<ClassMetricsResult> arg) {
		WMCCReturnType total = WMCCReturnType.empty();
		WMCCReturnType result;

		result = n.getLeft().accept(this, arg);
		if(result != null) {
			total = total.aggregate(result);
		}
		result = n.getRight().accept(this, arg);
		if(result != null) {
			total = total.aggregate(result);
		}
		
		List<BinaryExpr.Operator> operators = Arrays.asList(BinaryExpr.Operator.AND,
																	BinaryExpr.Operator.OR,
																	BinaryExpr.Operator.BINARY_AND,
																	BinaryExpr.Operator.BINARY_OR);
		for (BinaryExpr.Operator operator : operators) {
			if (n.getOperator().equals(operator)) {
				total = total.aggregate(new WMCCReturnType(0,1));
			}
		}
		
		return total; 
	}
	
    @Override
    public WMCCReturnType visit(NodeList n, List<ClassMetricsResult> arg) {
		WMCCReturnType total = WMCCReturnType.empty();

        for (final Object v : n) {
            WMCCReturnType result = ((Node) v).accept(this, arg);

            if (result != null) {
                total = total.aggregate(result);
            }
        }
        return total.isNonEmpty() ? total : null;
    }
}
