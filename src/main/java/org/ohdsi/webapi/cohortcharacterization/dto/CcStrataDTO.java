package org.ohdsi.webapi.cohortcharacterization.dto;

import org.ohdsi.analysis.cohortcharacterization.design.CohortCharacterizationStrata;
import org.ohdsi.circe.cohortdefinition.CriteriaGroup;

public class CcStrataDTO implements CohortCharacterizationStrata {

  private Long id;
  private String name;
  private CriteriaGroup criteria;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public CriteriaGroup getCriteria() {
    return criteria;
  }

  public void setCriteria(CriteriaGroup criteria) {
    this.criteria = criteria;
  }
}
