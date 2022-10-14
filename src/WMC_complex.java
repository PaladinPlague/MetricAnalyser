import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.AnnotationDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.visitor.GenericVisitorAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WMC_complex extends GenericVisitorAdapter<Integer, List<ClassMetricsResult>> {

    public Integer visit(ClassOrInterfaceDeclaration coid, List<ClassMetricsResult> arg) {
        int result = super.visit(coid, arg);

        int foundIndex = -1;
        for(int i=0; i<arg.size(); i++) {
            ClassMetricsResult cmr = arg.get(i);
            if(cmr.getCoid().equals(coid)) {
                foundIndex = i;
                break;
            }
        }

        if(foundIndex == -1) {
            arg.add(new ClassMetricsResult(coid));
            foundIndex = arg.size()-1;
        }

        arg.get(foundIndex).setWcm_c(result);

        return !coid.isInnerClass() ? result : 0;
    }

	@Override
    public Integer visit(MethodDeclaration md, List<ClassMetricsResult> arg) {
        super.visit(md, arg);
        int value = 1;
        for(IfStmt ifStmt: md.getChildNodesByType(IfStmt.class)) {
        	value ++; 
        	Expression condExpr = ifStmt.getCondition();
        }
        return value;
    }
	
	@Override 
	public Integer visit(IfStmt n, List<ClassMetricsResult> arg) {
		int total = 0;
		Integer result;
		
		result = n.getCondition().accept(this, arg);
		if(result != null) {
			total += result; 
		}
		
		if(n.getElseStmt().isPresent()) {
			result = n.getElseStmt().get().accept(this, arg);
			if(result != null) {
				total += result; 
			}
		}
		
		result = n.getThenStmt().accept(this, arg);
		if(result != null) {
			total += result; 
		}
		
		return total; 
	}
	
	@Override
	public Integer visit(BinaryExpr n, List<ClassMetricsResult> arg) {
		int total = 0; 
		total += n.getLeft().accept(this, arg);
		total += n.getRight().accept(this, arg);
		
		List<BinaryExpr.Operator> operators = Arrays.asList(BinaryExpr.Operator.AND,
																	BinaryExpr.Operator.OR,
																	BinaryExpr.Operator.BINARY_AND,
																	BinaryExpr.Operator.BINARY_OR); 		
		for(int i = 0; i < operators.size(); i++) {
			if(n.getOperator().equals(operators.get(i))) {
				total ++; 
			}
		}
		
		return total; 
	}
	
    @Override
    public Integer visit(NodeList n, List<ClassMetricsResult> arg) {
        int total = 0;
        boolean hasResult = false;

        for (final Object v : n) {
            Integer result = ((Node) v).accept(this, arg);

            if (result != null) {
                total += result;
                hasResult = true;
            }
        }
        return hasResult ? total : null;
    }
}
