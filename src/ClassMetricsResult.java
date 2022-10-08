import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;

public class ClassMetricsResult {
    private final ClassOrInterfaceDeclaration coid;
    private int wcm;
    private int rfc;
    private int cbo;
    private int lcom;

    public ClassMetricsResult(ClassOrInterfaceDeclaration coid) {
        this.coid = coid;
        this.wcm = 0;
        this.rfc = 0;
        this.cbo = 0;
        this.lcom = 0;
    }

    public ClassOrInterfaceDeclaration getCoid() {
        return this.coid;
    }

    public int getWcm() {
        return this.wcm;
    }

    public int getRfc() {
        return this.rfc;
    }
    public int getCbo() {
        return this.cbo;
    }
    public int getLcom() {
        return this.lcom;
    }

    public void setWcm(int wcm) {
        this.wcm = wcm;
    }

    public void setRfc(int rfc) {
        this.rfc = rfc;
    }
    public void setCbo(int cbo) {
        this.cbo = cbo;
    }
    public void setLcom(int lcom) {
        this.lcom = lcom;
    }

    public String toCSVString() {
        return coid.getNameAsString() + "," + wcm + "," + rfc + "," + cbo + "," + lcom + "\n";
    }
}
