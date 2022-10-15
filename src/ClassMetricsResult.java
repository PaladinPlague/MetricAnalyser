import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;

public class ClassMetricsResult {
    private final ClassOrInterfaceDeclaration coid;
    private int wmc;
    private int wmc_c;
    private int rfc;
    private int cbo;
    private int lcom;

    public ClassMetricsResult(ClassOrInterfaceDeclaration coid) {
        this.coid = coid;
        this.wmc = 0;
        this.wmc_c = 0;
        this.rfc = 0;
        this.cbo = 0;
        this.lcom = 0;
    }

    public ClassOrInterfaceDeclaration getCoid() {
        return this.coid;
    }

    public int getWmc() {
        return this.wmc;
    }

    public int getWmc_c() {
        return this.wmc_c;
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

    public void setWmc(int wmc) {
        this.wmc = wmc;
    }

    public void setWmc_c(int wmc_c) {
        this.wmc_c = wmc_c;
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
        return coid.getNameAsString() + "," + wmc + "," + wmc_c + "," + rfc + "," + cbo + "," + lcom + "\n";
    }
}
