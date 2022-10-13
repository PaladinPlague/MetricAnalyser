import com.github.javaparser.JavaToken;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.visitor.GenericVisitor;

import java.io.*;
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

                List<ClassMetricsResult> resultList = new ArrayList<>();

                for(File f : subDirectory.listFiles()) {
                    if(f.getName().endsWith(".java")) {
                        CompilationUnit cu = StaticJavaParser.parse(new FileInputStream(f));

//                        if(cu.getType(0).getNameAsString().equals("GridView")) {
                            LCOMVisitor.visit(cu, resultList);

//                        }












                        //WMCVisitor.visit(cu, resultList);
//                        WMCVisitor.visit(cu, resultMap);
                    }


                }
                for(ClassMetricsResult cmr : resultList) {
                    writeAndPrint(output, cmr.toCSVString());
                }
                writeAndPrint(output, "\n");
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
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
