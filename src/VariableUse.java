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
}
