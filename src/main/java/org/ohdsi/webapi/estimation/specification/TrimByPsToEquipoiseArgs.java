package org.ohdsi.webapi.estimation.specification;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

public class TrimByPsToEquipoiseArgs {
  @JsonProperty("bounds")
  private List<Float> bounds = null;

  @JsonProperty("attr_class")
  private String attrClass = "args";

  public TrimByPsToEquipoiseArgs bounds(List<Float> bounds) {
    this.bounds = bounds;
    return this;
  }

  public TrimByPsToEquipoiseArgs addBoundsItem(Float boundsItem) {
    if (this.bounds == null) {
      this.bounds = new ArrayList<Float>();
    }
    this.bounds.add(boundsItem);
    return this;
  }

  /**
   * The upper and lower bound on the preference score for keeping persons 
   * @return bounds
   **/
  @JsonProperty("bounds")
  public List<Float> getBounds() {
    return bounds;
  }

  public void setBounds(List<Float> bounds) {
    this.bounds = bounds;
  }

  public TrimByPsToEquipoiseArgs attrClass(String attrClass) {
    this.attrClass = attrClass;
    return this;
  }

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


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TrimByPsToEquipoiseArgs trimByPsToEquipoiseArgs = (TrimByPsToEquipoiseArgs) o;
    return Objects.equals(this.bounds, trimByPsToEquipoiseArgs.bounds) &&
        Objects.equals(this.attrClass, trimByPsToEquipoiseArgs.attrClass);
  }

  @Override
  public int hashCode() {
    return Objects.hash(bounds, attrClass);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class TrimByPsToEquipoiseArgs {\n");
    
    sb.append("    bounds: ").append(toIndentedString(bounds)).append("\n");
    sb.append("    attrClass: ").append(toIndentedString(attrClass)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }    
}
