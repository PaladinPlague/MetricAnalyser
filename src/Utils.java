import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;

import java.util.List;

public class Utils {
  public static ClassMetricsResult getOrCreateCMRInList(ClassOrInterfaceDeclaration coid, List<ClassMetricsResult> arg) {
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

    return arg.get(foundIndex);
  }
}
