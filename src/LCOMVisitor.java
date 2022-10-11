import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.visitor.GenericVisitorAdapter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LCOMVisitor extends GenericVisitorAdapter<Set<NameExpr>, List<ClassMetricsResult>> {
  @Override
  public Set<NameExpr> visit(ClassOrInterfaceDeclaration coid, List<ClassMetricsResult> arg) {
    Set<NameExpr> result = super.visit(coid, arg);

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

    return !coid.isInnerClass() ? result : null;
  }

  @Override
  public Set<NameExpr> visit(NameExpr ne, List<ClassMetricsResult> arg) {
    super.visit(ne, arg);

    Set<NameExpr> result = new HashSet<>();
    result.add(ne);
    return result;
  }

  public Set<NameExpr> visit(MethodDeclaration md, List<ClassMetricsResult> arg) {
    Set<NameExpr> result = super.visit(md, arg);

    return result;
  }


  @Override
  public Set<NameExpr> visit(NodeList n, List<ClassMetricsResult> arg) {
    Set<NameExpr> all = new HashSet<>();

    for (final Object v : n) {
      Set<NameExpr> result = ((Node) v).accept(this, arg);

      if (result != null) {
        all.addAll(result);
      }
    }
    return all.size() > 0 ? all : null;
  }
}
