import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.GenericVisitorAdapter;

import java.sql.Array;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RFCVisitor extends GenericVisitorAdapter<Set<String>, List<ClassMetricsResult>> {

    public Set<String> visit(ClassOrInterfaceDeclaration coid, List<ClassMetricsResult> arg) {
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

        Set<String> result = methodsList.accept(this, arg);
        int noMethods = result.size();
        //System.out.println(coid.getNameAsString() + " has "+ result + " and has " + noMethods + " Methods");
        System.out.println(allMembers);

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

        arg.get(foundIndex).setRfc(0);

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
}
