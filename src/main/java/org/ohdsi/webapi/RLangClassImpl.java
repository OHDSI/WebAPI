package org.ohdsi.webapi;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.ohdsi.analysis.RLangClass;

/**
 *
 * @author asena5
 */
public abstract class RLangClassImpl implements RLangClass {

    /**
     *
     */
    protected String attrClass = "args";

  /**
   * Get attrClass
   * @return attrClass
   **/
  @JsonProperty("attr_class")
  public String getAttrClass() {
    return attrClass;
  }

    /**
     *
     * @param attrClass
     */
    public void setAttrClass(String attrClass) {
    this.attrClass = attrClass;
  }  
}
