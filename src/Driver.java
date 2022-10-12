import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.visitor.GenericVisitor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.util.*;

public class Driver {
    private static final String DIRECTORY = "CS409TestSystem2022";

    public static void main(String[] args) {
        File directory = new File(DIRECTORY);

        analyseDirectory(directory);
    }

    public static void analyseDirectory(File directory) {
        //Instantiate visitors
        GenericVisitor<Integer, List<ClassMetricsResult>> WMCVisitor = new WMCVisitor();
        GenericVisitor<LCOMReturnType, List<ClassMetricsResult>> LCOMVisitor = new LCOMVisitor();

        File outputFile = new File("output.csv");

        try(FileWriter output = new FileWriter(outputFile)) {
            for(File subDirectory : directory.listFiles()) {
                writeAndPrint(output, subDirectory.getName()+"\n");
                writeAndPrint(output, "Class Name,WCM,RFC,CBO,LCOM\n");

                List<ClassMetricsResult> resultMap = new ArrayList<>();

                for(File f : subDirectory.listFiles()) {
                    if(f.getName().endsWith(".java")) {
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
//                        if(cu.getTypes().get(0).getNameAsString().equals("Field")) {
//                            LCOMReturnType r = LCOMVisitor.visit(cu, resultMap);
//                            int i = 1;
//                        }












                        WMCVisitor.visit(cu, resultMap);
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
