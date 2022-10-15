import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.GenericVisitorAdapter;
import java.util.List;

public class WMCVisitor extends GenericVisitorAdapter<Integer, List<ClassMetricsResult>> {

    public Integer visit(ClassOrInterfaceDeclaration coid, List<ClassMetricsResult> arg) {
        int result = super.visit(coid, arg);

        if(coid.isTopLevelType()) {
            Utils.getOrCreateCMRInList(coid, arg).setWmc(result);

        }

        return result;
    }

    @Override
    public Integer visit(MethodDeclaration md, List<ClassMetricsResult> arg) {
        super.visit(md, arg);
        return 1;
    }

    @Override
    public Integer visit(NodeList n, List<ClassMetricsResult> arg) {
        int total = 0;

        for (final Object v : n) {
            Integer result = ((Node) v).accept(this, arg);

            if (result != null) {
                total += result;
            }
        }
        return total != 0 ? total : null;
    }
}
