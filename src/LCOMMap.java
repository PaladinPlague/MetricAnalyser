import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.NameExpr;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class LCOMMap extends HashMap<MethodDeclaration, Set<VariableUse>> implements ILCOMMap {
  public LCOMMap(Map<MethodDeclaration, Set<VariableUse>> newMap) {
    super(newMap);
  }

  public LCOMMap() {
    super();
  }

}
