import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

public class WMCVisitor extends VoidVisitorAdapter<Void> {

    @Override
    public void visit(ClassOrInterfaceDeclaration coid, Void arg) {
        super.visit(coid, arg);
        NodeList<BodyDeclaration<?>> members = coid.getMembers();

        int counter = 0;

        for(BodyDeclaration<?> member : members) {
            if(member.isMethodDeclaration()) {
                counter++;
            }
        }

        System.out.println("Class name: " + coid.getName());
        System.out.println("Number of methods: " + counter);
        System.out.println();
    }
}
