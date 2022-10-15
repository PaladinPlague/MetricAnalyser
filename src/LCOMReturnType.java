import java.util.*;

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

    private Set<String> localVars;

    public Set<String> getLocalVars() {
        return localVars;
    }

    public void setLocalVars(Set<String> localVars) {
        this.localVars = localVars;
    }

    public void resetLocalVars() {
        setLocalVars(null);
    }

    //Method level
    //MethodDeclaration and Set<String>
    public LCOMReturnType(Set<VariableUse> nameSet, boolean isMethodLevel) {
        if(isMethodLevel) {
            this.nameSet = nameSet;
            this.dataType = LCOMReturnEnum.METHOD_LEVEL;
        } else {
            this.nameSet = nameSet;
            this.dataType = LCOMReturnEnum.STATEMENT_LEVEL;
        }

    }

    private List<Set<VariableUse>> totalVariableUses;
    private Set<String> fieldNameSet;

    //Class level
    //ILCOMMap and Set<String> (field names)
    public LCOMReturnType(List<Set<VariableUse>> totalVariableUses, Set<String> fieldNameSet) {
        this.totalVariableUses = totalVariableUses;
        this.fieldNameSet = fieldNameSet;
        this.dataType = LCOMReturnEnum.CLASS_LEVEL;
    }

    public List<Set<VariableUse>> getTotalVariableUses() {
        return totalVariableUses;
    }

    public Set<String> getFieldNameSet() {
        return this.fieldNameSet;
    }

    public LCOMReturnType aggregate(LCOMReturnType that) {
        LCOMReturnType emptyResult = aggregateEmpty(that);
        if(emptyResult != null) {
            return emptyResult;
        }

        // At the class level we will be aggregating (inner) classes and methods
        if (this.dataType == LCOMReturnEnum.CLASS_LEVEL) {
            if (that.getDataType() == LCOMReturnEnum.CLASS_LEVEL) {
                List<Set<VariableUse>> newTotalVariableUses = new ArrayList<>(this.getTotalVariableUses());
                newTotalVariableUses.addAll(that.getTotalVariableUses());

                Set<String> newSet = new HashSet<>(this.fieldNameSet);
                newSet.addAll(that.getFieldNameSet());

                return new LCOMReturnType(newTotalVariableUses, newSet);
            }

            if (that.getDataType() == LCOMReturnEnum.METHOD_LEVEL) {
                List<Set<VariableUse>> newTotalVariableUses = new ArrayList<>(this.getTotalVariableUses());
                newTotalVariableUses.add(that.getNameSet());

                return new LCOMReturnType(newTotalVariableUses, this.fieldNameSet);
            }
        }

        // At the method level we will be aggregating with other methods to produce a pseudo-class level
        // return type which is just an aggregation of methods without any fields,
        // this can be used to construct the actual class level type when we are at the class level
        if (this.dataType == LCOMReturnEnum.METHOD_LEVEL) {
            List<Set<VariableUse>> newTotalVariableUses = new ArrayList<>(new HashSet<>());
            newTotalVariableUses.add(this.nameSet);


            if (that.getDataType() == LCOMReturnEnum.METHOD_LEVEL) {
                newTotalVariableUses.add(that.getNameSet());
                return new LCOMReturnType(newTotalVariableUses, new HashSet<>());
            }
        }

        // At the statement level we could be aggregating other statements or just names
        if (this.dataType == LCOMReturnEnum.STATEMENT_LEVEL) {
            Set<VariableUse> newSet = this.nameSet != null ? new HashSet<>(this.nameSet) : new HashSet<>();

            if (that.getDataType() == LCOMReturnEnum.STATEMENT_LEVEL) {
                newSet.addAll(VariableUse.removeVarsFromSet(this.getLocalVars(), that.getNameSet()));
                LCOMReturnType toReturn = new LCOMReturnType(newSet, false);
                toReturn.setLocalVars(this.getLocalVars());
                return toReturn;
            }

            if (that.getDataType() == LCOMReturnEnum.NAME_LEVEL) {
                newSet.addAll(VariableUse.removeVarsFromSet(this.getLocalVars(), that.getNameSet()));
                LCOMReturnType toReturn = new LCOMReturnType(newSet, false);
                toReturn.setLocalVars(this.getLocalVars());
                return toReturn;
            }
        }

        //At the name level we are aggregating with another name, or a statement to produce a statement level result
        if (this.dataType == LCOMReturnEnum.NAME_LEVEL) {
            Set<VariableUse> newSet = this.nameSet != null ? new HashSet<>(this.nameSet) : new HashSet<>();

            if (that.getDataType() == LCOMReturnEnum.NAME_LEVEL) {
                newSet.addAll(that.getNameSet());
                return new LCOMReturnType(newSet, false);
            }
        }

        return null;
    }

    private LCOMReturnType aggregateEmpty(LCOMReturnType that) {
        if(this.isEmpty) {
            // always convert name level to statement level on aggregation with anything, even empty return type
            if(that.getDataType() == LCOMReturnEnum.NAME_LEVEL) {
                return new LCOMReturnType(that.getNameSet(), false);
            }

            // always convert a method level to a class level on aggregation
            if(that.getDataType() == LCOMReturnEnum.METHOD_LEVEL) {
                List<Set<VariableUse>> newTotalVariableUses = new ArrayList<>();
                newTotalVariableUses.add(that.getNameSet());
                return new LCOMReturnType(newTotalVariableUses, new HashSet<>());
            }

            return that;
        }

        if(that.isEmpty) {
            // always convert name level to statement level on aggregation with anything, even empty return type
            if(this.dataType == LCOMReturnEnum.NAME_LEVEL) {
                return new LCOMReturnType(this.getNameSet(), false);
            }

            return this;
        }

        return null;
    }

    public LCOMReturnType reduceListToFields() {
        if(this.isEmpty || this.dataType != LCOMReturnEnum.CLASS_LEVEL) {
            throw new IllegalStateException();
        }

        if(this.fieldNameSet.size() == 0) {
            throw new UnsupportedOperationException("Not sure yet what to do with classes with no fields");
        }

        List<Set<VariableUse>> reducedList = new ArrayList<>();

        for(Set<VariableUse> sv : totalVariableUses) {
            reducedList.add(VariableUse.keepVarsFromSet(fieldNameSet,sv));
        }

        return new LCOMReturnType(reducedList, fieldNameSet);
    }

    public int calculateLCOM() {
        if(this.isEmpty || this.dataType != LCOMReturnEnum.CLASS_LEVEL) {
            throw new IllegalStateException();
        }

        int LCOM = 0;

        List<Set<String>> allVarsForMethods = new ArrayList<>();

        for(Set<VariableUse> vuSet : this.totalVariableUses) {
            Set<String> stringSet = VariableUse.convertVarUseSetToStrings(vuSet);

            allVarsForMethods.add(stringSet);
        }

        for(int i = 0; i<allVarsForMethods.size(); i++) {
            Set<String> leftInPair = allVarsForMethods.get(i);

            // only need to iterate through methods after the current one
            for(int j=i+1; j< allVarsForMethods.size(); j++) {
                Set<String> rightInPair = allVarsForMethods.get(j);

                if(leftInPair.size() == 0 && rightInPair.size() == 0) {
                    // two methods which both use no variables are cohesive
                    LCOM -= 1;
                } else {
                    // Set intersection
                    Set<String> sharedVars = new HashSet<>(leftInPair);
                    sharedVars.retainAll(rightInPair);

                    LCOM += sharedVars.size() == 0 ? 1 : -1 ;
                }
            }
        }

        // Return LCOM, or 0 if it was negative
        return Math.max(LCOM, 0);
    }
}
