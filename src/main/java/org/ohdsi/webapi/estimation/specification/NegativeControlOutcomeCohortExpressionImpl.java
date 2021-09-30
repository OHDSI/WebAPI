package org.ohdsi.webapi.estimation.specification;

import java.util.ArrayList;
import java.util.List;
import org.ohdsi.analysis.estimation.design.NegativeControlOutcomeCohortExpression;

/**
 *
 * @author asena5
 */
public class NegativeControlOutcomeCohortExpressionImpl implements NegativeControlOutcomeCohortExpression {
  private String occurrenceType = null;
  private Boolean detectOnDescendants = null;
  private List<String> domains = null;

  /**
   * The type of occurrence of the event when selecting from the domain. The options are \&quot;All\&quot; or \&quot;First\&quot; 
   * @return occurrenceType
   **/
  @Override
  public String getOccurrenceType() {
    return occurrenceType;
  }

    /**
     *
     * @param occurrenceType
     */
    public void setOccurrenceType(String occurrenceType) {
    this.occurrenceType = occurrenceType;
  }

  /**
   * When true, descendant concepts for the conceptId will be used to detect the exposure/outcome and roll up the occurrence to the conceptId 
   * @return detectOnDescendants
   **/
  @Override
  public Boolean getDetectOnDescendants() {
    return detectOnDescendants;
  }

    /**
     *
     * @param detectOnDescendants
     */
    public void setDetectOnDescendants(Boolean detectOnDescendants) {
    this.detectOnDescendants = detectOnDescendants;
  }

    /**
     *
     * @param domainsItem
     * @return
     */
    public NegativeControlOutcomeCohortExpressionImpl addDomainsItem(String domainsItem) {
    if (this.domains == null) {
      this.domains = new ArrayList<>();
    }
    this.domains.add(domainsItem);
    return this;
  }

  /**
   * Specifies to the domains to use when evaluating negative control events.  
   * @return domains
   **/
  @Override
  public List<String> getDomains() {
    return domains;
  }

    /**
     *
     * @param domains
     */
    public void setDomains(List<String> domains) {
    this.domains = domains;
  }
}
