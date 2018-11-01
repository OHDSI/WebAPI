package org.ohdsi.webapi;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.ohdsi.analysis.RLangClass;

import java.io.Serializable;

public abstract class RLangClassImpl implements RLangClass, Serializable {
  protected String attrClass = "args";

  /**
   * Get attrClass
   * @return attrClass
   **/
  @JsonProperty("attr_class")
  public String getAttrClass() {
    return attrClass;
  }

  public void setAttrClass(String attrClass) {
    this.attrClass = attrClass;
  }  
}
