import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.visitor.GenericVisitorAdapter;

import java.sql.Array;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class RFCVisitor extends MetricVisitor<Set<String>> {

    public Set<String> visit(ClassOrInterfaceDeclaration coid, List<ClassMetricsResult> arg) {
        NodeList<BodyDeclaration<?>> allMembers = coid.getMembers();
        NodeList<MethodDeclaration> methodsList = new NodeList<>();
        NodeList<ClassOrInterfaceDeclaration> innerClassesList = new NodeList<>();
        int rfc = 0;

        for(BodyDeclaration<?> bd : allMembers) {
            if(bd.isMethodDeclaration()) {
                methodsList.add(bd.asMethodDeclaration());
            } else if(bd.isClassOrInterfaceDeclaration()) {
                innerClassesList.add(bd.asClassOrInterfaceDeclaration());
            }

        }

        Set<String> result = methodsList.accept(this, arg);
        Set<String> called = super.visit(coid, arg);
        System.out.println(coid.getNameAsString());
        System.out.println("Method List to check if it appears: " + result + " with a length of " + result.size());
        System.out.println("Methods that are actually called  : " + called + " with a length of " + called.size());

        Iterator<String> allResults = result.iterator();
        Iterator<String> allCalls = called.iterator();

        while(allResults.hasNext()){
            String outerResult = allResults.next();
            while(allCalls.hasNext()){
                String innerCall = allCalls.next();
                if(outerResult.equals(innerCall)){
                    rfc++;
                }
            }
            allCalls = called.iterator();
        }
        System.out.println(rfc);
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

        arg.get(foundIndex).setRfc(rfc + arg.get(foundIndex).getWcm());

        return !coid.isInnerClass() ? result : null;
    }



    @Override
    public Set<String> visit(MethodDeclaration md, List<ClassMetricsResult> arg) {
        super.visit(md, arg);
        Set<String> s = new HashSet<>();
        s.add(md.getNameAsString());
        return s;
    }

    @Override
    public Set<String> visit(NodeList n, List<ClassMetricsResult> arg) {
        Set<String> total = new HashSet<>();
        // n is a list of code
        boolean hasResult = false;

        for (final Object v : n) {
            Set<String> result = ((Node) v).accept(this, arg);
            if (result != null) {
                total.addAll(result);
                hasResult = true;
            }
        }
        return hasResult ? total : null;
    }

    @Override
    public Set<String> visit(MethodCallExpr calls, List<ClassMetricsResult> arg) {
        super.visit(calls, arg);
        Set<String> c = new HashSet<>();
        c.add(String.valueOf(calls.getName()));
        return c;
    }

    protected Set<String> initialiseEmpty() {
        return new HashSet<>();
    }
    protected Set<String> combineResults(Set<String> first, Set<String> second) {
        Set<String> combined = new HashSet<>();
        combined.addAll(first);
        combined.addAll(second);
        return combined;
    }
    protected Set<String> getReturn(Set<String> possibleReturn) {
        return possibleReturn.size() > 0 ? possibleReturn : null;
    }

}
