//import com.github.javaparser.ast.CompilationUnit;
//import com.github.javaparser.ast.Node;
//import com.github.javaparser.ast.NodeList;
//import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
//import com.github.javaparser.ast.body.MethodDeclaration;
//import com.github.javaparser.ast.expr.*;
//import com.github.javaparser.ast.stmt.DoStmt;
//import com.github.javaparser.ast.visitor.GenericVisitorAdapter;
//
//import java.util.HashSet;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//
//public class LCOMVisitorOld {
//  private int foo = 1;
//
//  public void myMethodA(int foo) {
//    System.out.println(foo);
//  }
//
//  public void myMethodB(int foo) {
//    System.out.println(this.foo);
//  }
//
//  class in {
//    private String foo;
//    public in() {
//      string blah = foo+ "";
//    }
//  }
//
//}



//public class LCOMVisitorOld extends GenericVisitorAdapter<LCOMReturnType, List<ClassMetricsResult>> {
//  @Override
//  public LCOMReturnType visit(CompilationUnit cu, List<ClassMetricsResult> arg) {
//    super.visit(cu, arg);
//    return null;
//  }
//
//  @Override
//  public LCOMReturnType visit(SimpleName sn, List<ClassMetricsResult> arg) {
//    return new LCOMReturnType(sn.getIdentifier());
//  }
//
//
//  @Override
//  public LCOMReturnType visit(ClassOrInterfaceDeclaration coid, List<ClassMetricsResult> arg) {
//    int classesSoFar = arg.size();
//    ILCOMMap result = coid.getMembers().accept(this, arg);
//
//    if(arg.size() > classesSoFar) {
//      //we have inner classes
//    }
//
//    Utils.getOrCreateCMRInList(coid, arg);
//
//    return result;
//  }
//
//  @Override
//  public LCOMReturnType visit(NameExpr ne, List<ClassMetricsResult> arg) {
//    super.visit(ne, arg);
//
//    LCOMMap result = new LCOMMap();
//    Set<NameExpr> sne = new HashSet<>();
//    sne.add(ne);
//    result.put(new MethodDeclaration(), sne);
//    return result;
//  }
//
//  public LCOMReturnType visit(MethodDeclaration md, List<ClassMetricsResult> arg) {
//    Map<MethodDeclaration, Set<NameExpr>> result = super.visit(md, arg);
//
//    return result;
//  }
//
//
//  @Override
//  public LCOMReturnType visit(NodeList n, List<ClassMetricsResult> arg) {
//    Set<NameExpr> all = new HashSet<>();
//
//    for (final Object v : n) {
//      Map<MethodDeclaration, Set<NameExpr>> result = ((Node) v).accept(this, arg);
//
//      if (result != null) {
//        all.addAll(result);
//      }
//    }
//    return all.size() > 0 ? all : null;
//  }
//
//
//  @Override
//  public LCOMReturnType visit(final AssignExpr n, final List<ClassMetricsResult> arg) {
//    LCOMReturnType result;
//    Set<String> allNames = new HashSet<>();
//    {
//      result = n.getTarget().accept(this, arg);
//      if (result != null)
//        allNames.add(result.getName());
//    }
//    {
//      result = n.getValue().accept(this, arg);
//      if (result != null)
//        allNames.add(result.getName());
//    }
//    result = new LCOMReturnType(allNames);
//
//    return result;
//  }
//
//  @Override
//  public LCOMReturnType visit(final BinaryExpr n, final List<ClassMetricsResult> arg) {
//    LCOMReturnType result;
//    Set<String> allNames = new HashSet<>();
//    {
//      result = n.getLeft().accept(this, arg);
//      if (result != null)
//        allNames.add(result.getName());
//    }
//    {
//      result = n.getRight().accept(this, arg);
//      if (result != null)
//        allNames.add(result.getName());
//    }
//    result = new LCOMReturnType(allNames);
//
//    return result;
//  }
//
//  @Override
//  public LCOMReturnType visit(final ConditionalExpr n, final List<ClassMetricsResult> arg) {
//    LCOMReturnType result;
//    Set<String> allNames = new HashSet<>();
//    {
//      result = n.getCondition().accept(this, arg);
//      if (result != null)
//        allNames.addAll(result.getNameSet());
//    }
//    {
//      result = n.getElseExpr().accept(this, arg);
//      if (result != null)
//        allNames.addAll(result.getNameSet());
//    }
//    {
//      result = n.getThenExpr().accept(this, arg);
//      if (result != null)
//        allNames.addAll(result.getNameSet());
//    }
//    result = new LCOMReturnType(allNames);
//
//    return result;
//  }
//
//  @Override
//  public LCOMReturnType visit(final DoStmt n, final List<ClassMetricsResult> arg) {
//    LCOMReturnType result;
//    Set<String> allNames = new HashSet<>();
//    {
//      result = n.getBody().accept(this, arg);
//      if (result != null)
//        allNames.addAll(result.getNameSet());
//    }
//    {
//      result = n.getCondition().accept(this, arg);
//      if (result != null)
//        allNames.addAll(result.getNameSet());
//    }
//    result = new LCOMReturnType(allNames);
//
//    return result;
//  }
//}
