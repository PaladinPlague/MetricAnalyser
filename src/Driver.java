import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.visitor.GenericVisitor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class Driver {
    private static final String DIRECTORY = "CS409TestSystem2022";

    public static void main(String[] args) {
        File directory = new File(DIRECTORY);

        analyseDirectory(directory);
    }

    public static void analyseDirectory(File directory) {
        //Instantiate visitors
        GenericVisitor<Integer, List<ClassMetricsResult>> WMCVisitor = new WMCVisitor();
        GenericVisitor<Set<NameExpr>, List<ClassMetricsResult>> LCOMVisitor = new LCOMVisitor();

        File outputFile = new File("output.csv");

        try(FileWriter output = new FileWriter(outputFile)) {


            for(File subDirectory : directory.listFiles()) {
                writeAndPrint(output, subDirectory.getName()+"\n");
                writeAndPrint(output, "Class Name,WCM,RFC,CBO,LCOM\n");

                List<ClassMetricsResult> resultMap = new ArrayList<>();

                for(File f : subDirectory.listFiles()) {


                    String extension = "";
                    String fileName = f.getName();

                    int i = fileName.lastIndexOf('.');
                    if (i > 0) {
                        extension = fileName.substring(i+1);
                    }

                    if(extension.equals("java")) {
                        CompilationUnit cu = StaticJavaParser.parse(new FileInputStream(f));

//                        NodeList<TypeDeclaration<?>> classes = cu.getTypes();
//
//
//                        NodeList<BodyDeclaration<?>> members = classes.get(0).getMembers();
//
//                        for(BodyDeclaration<?> b : members) {
//                            if(b.isMethodDeclaration() && b.asMethodDeclaration().getName().toString().equals("setDead")) {
//                                BlockStmt bs = b.asMethodDeclaration().getBody().get().asBlockStmt();
//                                ExpressionStmt es = bs.getStatements().get(0).asExpressionStmt();
//                                es.getExpression().asAssignExpr().getTarget().asNameExpr().resolve();
//                            }
//                        }

                        Set<NameExpr> r = LCOMVisitor.visit(cu, resultMap);
                        System.out.println(r.toArray().toString());









                        //WMCVisitor.visit(cu, resultMap);
//                        WMCVisitor.visit(cu, resultMap);
                    }


                }
                for(ClassMetricsResult cmr : resultMap) {
                    writeAndPrint(output, cmr.toCSVString());
                }
                writeAndPrint(output, "\n");
            }
        } catch (Exception e) {
            System.out.println("Failed to read directory");
        }
    }

    private static void writeAndPrint(FileWriter output, String line) {
        try {
            output.write(line);
            System.out.print(line);
        } catch (Exception ignored) {

        }


    }
}
