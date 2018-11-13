package org.ohdsi.webapi.estimation.specification;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

public class NegativeControlOutcomeCohortExpression {
  @JsonProperty("occurrenceType")
  private String occurrenceType = null;

  @JsonProperty("detectOnDescendants")
  private Boolean detectOnDescendants = null;

  @JsonProperty("domains")
  private List<String> domains = null;

  public NegativeControlOutcomeCohortExpression occurrenceType(String occurrenceType) {
    this.occurrenceType = occurrenceType;
    return this;
  }

  /**
   * The type of occurrence of the event when selecting from the domain. The options are \&quot;All\&quot; or \&quot;First\&quot; 
   * @return occurrenceType
   **/
  @JsonProperty("occurrenceType")
  public String getOccurrenceType() {
    return occurrenceType;
  }

  public void setOccurrenceType(String occurrenceType) {
    this.occurrenceType = occurrenceType;
  }

  public NegativeControlOutcomeCohortExpression detectOnDescendants(Boolean detectOnDescendants) {
    this.detectOnDescendants = detectOnDescendants;
    return this;
  }

  /**
   * When true, desendant concepts for the conceptId will be used to detect the exposure/outcome and roll up the occurrence to the conceptId 
   * @return detectOnDescendants
   **/
  @JsonProperty("detectOnDescendants")
  public Boolean isisDetectOnDescendants() {
    return detectOnDescendants;
  }

  public void setDetectOnDescendants(Boolean detectOnDescendants) {
    this.detectOnDescendants = detectOnDescendants;
  }

  public NegativeControlOutcomeCohortExpression domains(List<String> domains) {
    this.domains = domains;
    return this;
  }

  public NegativeControlOutcomeCohortExpression addDomainsItem(String domainsItem) {
    if (this.domains == null) {
      this.domains = new ArrayList<String>();
    }
    this.domains.add(domainsItem);
    return this;
  }

  /**
   * Specifies to the domains to use when evaluating negative control events.  
   * @return domains
   **/
  @JsonProperty("domains")
  public List<String> getDomains() {
    return domains;
  }

  public void setDomains(List<String> domains) {
    this.domains = domains;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    NegativeControlOutcomeCohortExpression negativeControlOutcomeCohortExpression = (NegativeControlOutcomeCohortExpression) o;
    return Objects.equals(this.occurrenceType, negativeControlOutcomeCohortExpression.occurrenceType) &&
        Objects.equals(this.detectOnDescendants, negativeControlOutcomeCohortExpression.detectOnDescendants) &&
        Objects.equals(this.domains, negativeControlOutcomeCohortExpression.domains);
  }

  @Override
  public int hashCode() {
    return Objects.hash(occurrenceType, detectOnDescendants, domains);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class NegativeControlOutcomeCohortExpression {\n");
    
    sb.append("    occurrenceType: ").append(toIndentedString(occurrenceType)).append("\n");
    sb.append("    detectOnDescendants: ").append(toIndentedString(detectOnDescendants)).append("\n");
    sb.append("    domains: ").append(toIndentedString(domains)).append("\n");
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
