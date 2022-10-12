import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.NameExpr;

import java.util.Map;
import java.util.Set;

public interface ILCOMMap extends Map<MethodDeclaration, Set<String>> {
}
