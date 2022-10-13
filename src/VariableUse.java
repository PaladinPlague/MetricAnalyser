import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class VariableUse {
    private final String varName;

    private final boolean confirmedFieldAccess;

    public VariableUse(String varName, boolean confirmedFieldAccess) {
        this.varName = varName;
        this.confirmedFieldAccess = confirmedFieldAccess;
    }

    public VariableUse(String varName) {
        this(varName, false);
    }

    public String getVarName() {
        return varName;
    }

    public boolean isConfirmedFieldAccess() {
        return confirmedFieldAccess;
    }

    public boolean equals(Object that) {
        if(that instanceof VariableUse) {
            VariableUse thatTyped =  (VariableUse) that;
            return this.varName.equals(thatTyped.getVarName()) && this.confirmedFieldAccess == thatTyped.isConfirmedFieldAccess();
        }

        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(varName, confirmedFieldAccess);
    }

    public static Set<String> convertVarUseSetToStrings(Set<VariableUse> s) {
        Set<String> output = new HashSet<>();
        for(VariableUse vu:  s) {
            output.add(vu.getVarName());
        }

        return output;
    }

    public static Set<VariableUse> removeVarsFromSet(Set<String> varsToRemove, Set<VariableUse> allVars) {
        //assume passed in Set is by reference of the nameSet - create a copy
        Set<VariableUse> toUse = new HashSet<>(allVars);

        if(varsToRemove == null) {
            return toUse;
        }

        //if it's not a confirmed field access and we have a relevant local var with that name
        //it's a local var use - remove
        toUse.removeIf(vu -> !vu.isConfirmedFieldAccess() && varsToRemove.contains(vu.getVarName()));

        return toUse;
    }

    public static Set<VariableUse> keepVarsFromSet(Set<String> varsToKeep, Set<VariableUse> allVars) {
        Set<VariableUse> toUse = new HashSet<>();

        for(VariableUse vu : allVars) {
            // if the VariableUse is a field
            if(varsToKeep.contains(vu.getVarName())) {
                toUse.add(vu);
            }
        }

        return toUse;
    }
}
