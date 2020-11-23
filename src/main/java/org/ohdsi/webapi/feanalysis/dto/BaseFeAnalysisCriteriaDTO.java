package org.ohdsi.webapi.feanalysis.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "criteriaType", defaultImpl = FeAnalysisCriteriaDTO.class)
@JsonSubTypes({
        @JsonSubTypes.Type(value = FeAnalysisCriteriaDTO.class, name = "CriteriaGroup"),
        @JsonSubTypes.Type(value = FeAnalysisWindowedCriteriaDTO.class, name = "WindowedCriteria"),
        @JsonSubTypes.Type(value = FeAnalysisDemographicCriteriaDTO.class, name = "DemographicCriteria")
})
public abstract class BaseFeAnalysisCriteriaDTO {
  @JsonProperty("id")
  private Long id;
  @JsonProperty("name")
  private String name;

  @JsonProperty("aggregate")
  private FeAnalysisAggregateDTO aggregate;

  public BaseFeAnalysisCriteriaDTO() {
  }

  public BaseFeAnalysisCriteriaDTO(Long id, String name) {
    this.id = id;
    this.name = name;
  }

  public Long getId() {
      return id;
  }

  public void setId(Long id) {
      this.id = id;
  }

  public String getName() {
      return name;
  }

  public void setName(final String name) {
      this.name = name;
  }

  public FeAnalysisAggregateDTO getAggregate() {
    return aggregate;
  }

  public void setAggregate(FeAnalysisAggregateDTO aggregate) {
    this.aggregate = aggregate;
  }
}
