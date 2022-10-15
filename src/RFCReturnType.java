import java.util.HashSet;
import java.util.Set;

public class RFCReturnType {
    private final Set<String> remoteMethodNames;

    private int numberOfOwnMethods;

    public static RFCReturnType empty() {
        return new RFCReturnType(new HashSet<>());
    }

    public RFCReturnType(Set<String> remoteMethodNames) {
        this.remoteMethodNames = remoteMethodNames;
        this.numberOfOwnMethods = 0;
    }

    public boolean isNonEmpty() {
        return !(this.remoteMethodNames.size() == 0 && this.numberOfOwnMethods == 0);
    }

    public Set<String> getRemoteMethodNames() {
        return this.remoteMethodNames;
    }

    public int getNumberOfOwnMethods() {
        return this.numberOfOwnMethods;
    }

    public void setNumberOfOwnMethods(int numberOfOwnMethods) {
        this.numberOfOwnMethods = numberOfOwnMethods;
    }

    public RFCReturnType aggregate(RFCReturnType that) {
        Set<String> combined = new HashSet<>();
        combined.addAll(this.remoteMethodNames);
        combined.addAll(that.getRemoteMethodNames());

        RFCReturnType toReturn = new RFCReturnType(combined);
        toReturn.setNumberOfOwnMethods(this.numberOfOwnMethods + that.getNumberOfOwnMethods());
        return toReturn;
    }

    public void addSingleMethodName(String methodName) {
        this.remoteMethodNames.add(methodName);
    }



    public int calculateRfc() {
        return this.numberOfOwnMethods + this.remoteMethodNames.size();
    }

}
