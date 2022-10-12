import com.github.javaparser.ast.body.MethodDeclaration;

import java.util.HashSet;
import java.util.Set;

public class LCOMReturnType {
  private final LCOMReturnEnum dataType;

  public enum LCOMReturnEnum {
    NAME_LEVEL,
    STATEMENT_LEVEL,
    METHOD_LEVEL,
    CLASS_LEVEL
  }

  public LCOMReturnEnum getDataType() {
    return this.dataType;
  }

  private String name;

  //Name level
  //String
  public LCOMReturnType(String name) {
    this.name = name;
    this.dataType = LCOMReturnEnum.NAME_LEVEL;
  }

  public String getName() {
    return this.name;
  }

  private Set<String> nameSet;

  //Statement level
  //Set<String>
  public LCOMReturnType(Set<String> nameSet) {
    this.nameSet = nameSet;
    this.dataType = LCOMReturnEnum.STATEMENT_LEVEL;
  }

  public Set<String> getNameSet() {
    return this.nameSet;
  }

  private MethodDeclaration md;

  //Method level
  //MethodDeclaration and Set<String>
  public LCOMReturnType(MethodDeclaration md, Set<String> nameSet) {
    this.md = md;
    this.nameSet = nameSet;
    this.dataType = LCOMReturnEnum.METHOD_LEVEL;
  }

  public MethodDeclaration getMethodDeclaration() {
    return this.md;
  }

  private ILCOMMap ilm;
  private Set<String> fieldNameSet;

  //Class level
  //ILCOMMap and Set<String> (field names)
  public LCOMReturnType(ILCOMMap ilm, Set<String> fieldNameSet) {
    this.ilm = ilm;
    this.fieldNameSet = fieldNameSet;
    this.dataType = LCOMReturnEnum.CLASS_LEVEL;
  }

  public ILCOMMap getLCOMMap() {
    return ilm;
  }

  public Set<String> getFieldNameSet() {
    return this.fieldNameSet;
  }

  public LCOMReturnType aggregate(LCOMReturnType that) {
    //At the class level we will be aggregating (inner) classes and methods
    if(this.dataType == LCOMReturnEnum.CLASS_LEVEL) {
      if(that.getDataType() == LCOMReturnEnum.CLASS_LEVEL) {
        ILCOMMap newMap = new LCOMMap(this.getLCOMMap());
        newMap.putAll(that.getLCOMMap());

        Set<String> newSet = new HashSet<>(this.fieldNameSet);
        newSet.addAll(that.getFieldNameSet());

        return new LCOMReturnType(newMap, newSet);
      }

      if(that.getDataType() == LCOMReturnEnum.METHOD_LEVEL) {
        ILCOMMap newMap = new LCOMMap(this.getLCOMMap());
        newMap.put(that.getMethodDeclaration(), that.getNameSet());

        return new LCOMReturnType(newMap, this.fieldNameSet);
      }

      return null;
    }

    //At the method level we will be aggregating statements only
    if(this.dataType == LCOMReturnEnum.METHOD_LEVEL) {
      Set<String> newSet = new HashSet<>(this.nameSet);

      if(that.getDataType() == LCOMReturnEnum.STATEMENT_LEVEL) {
        newSet.addAll(that.getNameSet());
        return new LCOMReturnType(this.md, newSet);
      }
    }

    //At the statement level we could be aggregating other statements or just names
    if(this.dataType == LCOMReturnEnum.STATEMENT_LEVEL) {
      Set<String> newSet = new HashSet<>(this.nameSet);

      if(that.getDataType() == LCOMReturnEnum.STATEMENT_LEVEL) {
        newSet.addAll(that.getNameSet());
        return new LCOMReturnType(newSet);
      }

      if(that.getDataType() == LCOMReturnEnum.NAME_LEVEL) {
        newSet.add(that.getName());
        return new LCOMReturnType(newSet);
      }
    }

    return null;
  }


  public void aggregate(Set<String> s) {

  }


}
