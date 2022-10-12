import com.github.javaparser.ast.body.MethodDeclaration;

import java.util.HashSet;
import java.util.Set;

public class LCOMReturnType {
    private LCOMReturnEnum dataType;
    private boolean isEmpty = false;

    public enum LCOMReturnEnum {
        NAME_LEVEL,
        STATEMENT_LEVEL,
        METHOD_LEVEL,
        CLASS_LEVEL
    }

    public static LCOMReturnType empty() {
        return new LCOMReturnType();
    }

    private LCOMReturnType() {
        this.isEmpty = true;
    }

    public boolean isNonEmpty() {
        return !this.isEmpty;
    }

    public LCOMReturnEnum getDataType() {
        return this.dataType;
    }

    private Set<VariableUse> nameSet;

    public Set<VariableUse> getNameSet() {
        return this.nameSet;
    }

    //Name level
    //String
    public LCOMReturnType(VariableUse name) {
        this.nameSet = new HashSet<>();
        nameSet.add(name);
        this.dataType = LCOMReturnEnum.NAME_LEVEL;
    }

    //Statement level
    //Set<String>
    public LCOMReturnType(Set<VariableUse> nameSet) {
        this.nameSet = nameSet;
        this.dataType = LCOMReturnEnum.STATEMENT_LEVEL;
    }

    private MethodDeclaration md;

    //Method level
    //MethodDeclaration and Set<String>
    public LCOMReturnType(MethodDeclaration md, Set<VariableUse> nameSet) {
        this.md = md;
        this.nameSet = nameSet;
        this.dataType = LCOMReturnEnum.METHOD_LEVEL;
    }

    public MethodDeclaration getMethodDeclaration() {
        return this.md;
    }

    private ILCOMMap ilm;
    private Set<VariableUse> fieldNameSet;

    //Class level
    //ILCOMMap and Set<String> (field names)
    public LCOMReturnType(ILCOMMap ilm, Set<VariableUse> fieldNameSet) {
        this.ilm = ilm;
        this.fieldNameSet = fieldNameSet;
        this.dataType = LCOMReturnEnum.CLASS_LEVEL;
    }

    public ILCOMMap getLCOMMap() {
        return ilm;
    }

    public Set<VariableUse> getFieldNameSet() {
        return this.fieldNameSet;
    }

    public LCOMReturnType aggregate(LCOMReturnType that) {
        if(this.isEmpty) {
            return that;
        }

        // At the class level we will be aggregating (inner) classes and methods
        if (this.dataType == LCOMReturnEnum.CLASS_LEVEL) {
            if (that.getDataType() == LCOMReturnEnum.CLASS_LEVEL) {
                ILCOMMap newMap = this.getLCOMMap() != null ? new LCOMMap(this.getLCOMMap()) : new LCOMMap();
                newMap.putAll(that.getLCOMMap());

                Set<VariableUse> newSet = this.fieldNameSet != null ? new HashSet<>(this.fieldNameSet) : new HashSet<>();
                newSet.addAll(that.getFieldNameSet());

                return new LCOMReturnType(newMap, newSet);
            }

            if (that.getDataType() == LCOMReturnEnum.METHOD_LEVEL) {
                ILCOMMap newMap = this.getLCOMMap() != null ? new LCOMMap(this.getLCOMMap()) : new LCOMMap();
                newMap.put(that.getMethodDeclaration(), that.getNameSet());

                return new LCOMReturnType(newMap, this.fieldNameSet);
            }

            return null;
        }

        // At the method level we will be aggregating with other methods to produce a pseudo-class level
        // return type which is just an aggregation of methods without any fields,
        // this can be used to construct the actual class level type when we are at the class level
        if (this.dataType == LCOMReturnEnum.METHOD_LEVEL) {
            ILCOMMap newMap = new LCOMMap();
            newMap.put(this.md, this.nameSet);


            if (that.getDataType() == LCOMReturnEnum.METHOD_LEVEL) {
                newMap.put(that.getMethodDeclaration(), that.getNameSet());
                return new LCOMReturnType(newMap, null);
            }
        }

        // At the statement level we could be aggregating other statements or just names
        if (this.dataType == LCOMReturnEnum.STATEMENT_LEVEL) {
            Set<VariableUse> newSet = this.nameSet != null ? new HashSet<>(this.nameSet) : new HashSet<>();

            if (that.getDataType() == LCOMReturnEnum.STATEMENT_LEVEL) {
                newSet.addAll(that.getNameSet());
                return new LCOMReturnType(newSet);
            }

            if (that.getDataType() == LCOMReturnEnum.NAME_LEVEL) {
                newSet.addAll(that.getNameSet());
                return new LCOMReturnType(newSet);
            }
        }

        //At the name level we are aggregating with another name to produce a statement level result
        if (this.dataType == LCOMReturnEnum.NAME_LEVEL) {
            Set<VariableUse> newSet = this.nameSet != null ? new HashSet<>(this.nameSet) : new HashSet<>();

            if (that.getDataType() == LCOMReturnEnum.NAME_LEVEL) {
                newSet.addAll(that.getNameSet());
                return new LCOMReturnType(newSet);
            }
        }


        return null;
    }

    public static Set<String> convertVarUseSetToStrings(Set<VariableUse> s) {
        Set<String> output = new HashSet<>();
        for(VariableUse vu:  s) {
            output.add(vu.getVarName());
        }

        return output;
    }


}
