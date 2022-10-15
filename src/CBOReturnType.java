import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CBOReturnType {
    private ClassOrInterfaceDeclaration associatedCoid;
    private Set<String> classesUsed;

    public CBOReturnType(Set<String> newClasses) {
        classesUsed = newClasses;
    }

    public CBOReturnType(String newClass) {
        classesUsed  = new HashSet<>();
        classesUsed.add(newClass);
    }

    public Set<String> getClassesUsed() {
        return classesUsed;
    }

    public void setClassesUsed(Set<String> classesUsed) {
        this.classesUsed = classesUsed;
    }

    public ClassOrInterfaceDeclaration getAssociatedCoid() {
        return associatedCoid;
    }

    public String getAssociatedCoidName() { return associatedCoid.getNameAsString(); }

    public void setAssociatedCoid(ClassOrInterfaceDeclaration associatedCoid) {
        this.associatedCoid = associatedCoid;
        this.classesUsed.remove(associatedCoid.getNameAsString());
    }

    public CBOReturnType aggregate(CBOReturnType that) {
        Set<String> allClasses = new HashSet<>(this.classesUsed);
        allClasses.addAll(that.getClassesUsed());

        CBOReturnType toReturn = new CBOReturnType(allClasses);
        if(this.associatedCoid != null ) {
            toReturn.setAssociatedCoid(this.associatedCoid);
        } else if(that.associatedCoid != null ) {
            toReturn.setAssociatedCoid(that.getAssociatedCoid());
        }

        return toReturn;
    }

    public static CBOReturnType empty() {
        return new CBOReturnType(new HashSet<>());
    }

    public boolean isNonEmpty() {
        return classesUsed.size() > 0;
    }

    public void removeInnerClass(String name) {
        this.classesUsed.remove(name);
    }

    public boolean usesClass(String className) {
        return this.classesUsed.contains(className);
    }

    public int calculateCbo(List<CBOReturnType> allReturns) {
        // construct a coupling set for this class from the class use sets

        String className = this.getAssociatedCoidName();

        // outgoing uses
        Set<String> couplingSet = new HashSet<>(this.getClassesUsed());

        // we have already removed references to itself from classes used,
        // so there is no need to exclude the comparison with itself
        for (CBOReturnType potentialCoupling : allReturns) {
            // incoming uses
            if (potentialCoupling.usesClass(className)) {
                couplingSet.add(potentialCoupling.getAssociatedCoidName());
            }
        }

        return couplingSet.size();
    }

    public static void processResults(List<CBOReturnType> results, List<ClassMetricsResult> allCmr) {
        reduceToDirectoryClasses(results);

        for(CBOReturnType classReturn : results) {
            ClassMetricsResult cmr = Utils.getOrCreateCMRInList(classReturn.getAssociatedCoid(), allCmr);
            cmr.setCbo(classReturn.calculateCbo(results));
        }
    }

    public static void reduceToDirectoryClasses(List<CBOReturnType> allReturns) {
        List<String> allClassNames = new ArrayList<>();

        for(CBOReturnType crt: allReturns) {
            allClassNames.add(crt.getAssociatedCoidName());
        }

        for(CBOReturnType crt : allReturns) {
            // reduce to only classes in allClassNames
            Set<String> reducedClasses = new HashSet<>();
            for(String className: crt.getClassesUsed()) {
                if(allClassNames.contains(className)) {
                    reducedClasses.add(className);
                }
            }

            crt.setClassesUsed(reducedClasses);
        }
    }


}
