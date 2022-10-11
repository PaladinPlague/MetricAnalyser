import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.visitor.GenericVisitor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

public class Driver {
    private static final String DIRECTORY = "CS409TestSystem2022";

    public static void main(String[] args) {
        File directory = new File(DIRECTORY);

        analyseDirectory(directory);
    }

    public static void analyseDirectory(File directory) {
        GenericVisitor<Integer, List<ClassMetricsResult>> WMCVisitor = new WMCVisitor();
        GenericVisitor<Integer, List<ClassMetricsResult>> WMC_complex = new WMC_complex();
        File outputFile = new File("output.csv");

        try(FileWriter output = new FileWriter(outputFile)) {


            for(File subDirectory : directory.listFiles()) {
                writeAndPrint(output, subDirectory.getName()+"\n");
                writeAndPrint(output, "Class Name,WCM,WCM_C,RFC,CBO,LCOM\n");

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

                        WMCVisitor.visit(cu, resultMap);
                        WMCVisitor.visit(cu, resultMap);
                        WMC_complex.visit(cu, resultMap);
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
