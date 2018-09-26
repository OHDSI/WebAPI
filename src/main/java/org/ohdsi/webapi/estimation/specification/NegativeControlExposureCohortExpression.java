package org.ohdsi.webapi.estimation.specification;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Objects;

/**
 * The expression that defines the criteria for inclusion and duration of time for cohorts intended for use as negative control exposures. This model is still under desgin and is a placholder for now. 
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class NegativeControlExposureCohortExpression {
  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    return Objects.hash();
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class NegativeControlExposureCohortExpression {\n");
    
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
