import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.visitor.VoidVisitor;

import java.io.File;
import java.io.FileInputStream;

public class Driver {
    private static final String DIRECTORY = "CS409TestSystem2022";

    public static void main(String[] args) throws Exception {
        File directory = new File(DIRECTORY);

        for(File subDirectory : directory.listFiles()) {
            System.out.println("Analysing directory: " + subDirectory.getName());
            for(File f : subDirectory.listFiles()) {
                String extension = "";
                String fileName = f.getName();

                int i = fileName.lastIndexOf('.');
                if (i > 0) {
                    extension = fileName.substring(i+1);
                }

                if(extension.equals("java")) {
                    CompilationUnit cu = StaticJavaParser.parse(new FileInputStream(f));

                    VoidVisitor<Void> WMCVisitor = new WMCVisitor();
                    WMCVisitor.visit(cu, null);
                }
            }
        }



    }
}
