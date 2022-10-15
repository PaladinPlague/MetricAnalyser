public class WMCCReturnType {
    private int complexity;

    private int compoundStatementsFound;

    public WMCCReturnType(int complexity, int compoundStatementsFound) {
        this.complexity = complexity;
        this.compoundStatementsFound = compoundStatementsFound;
    }

    public WMCCReturnType(int complexity) {
        this(complexity, 0);
    }

    public int getComplexity() {
        return complexity;
    }

    public int getCompoundStatementsFound() {
        return compoundStatementsFound;
    }

    public void confirmIfCondition() {
        complexity += compoundStatementsFound;
        compoundStatementsFound = 0;
    }

    public static WMCCReturnType empty() {
        return new WMCCReturnType(0,0);
    }

    public WMCCReturnType aggregate(WMCCReturnType that) {
        return new WMCCReturnType(this.complexity + that.getComplexity(), this.compoundStatementsFound + that.getCompoundStatementsFound());
    }

    public boolean isNonEmpty() {
        return !(complexity == 0 && compoundStatementsFound == 0);
    }

}
