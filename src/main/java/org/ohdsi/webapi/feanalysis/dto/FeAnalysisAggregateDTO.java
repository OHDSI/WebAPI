package org.ohdsi.webapi.feanalysis.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.ohdsi.analysis.cohortcharacterization.design.AggregateFunction;
import org.ohdsi.analysis.cohortcharacterization.design.StandardFeatureAnalysisDomain;

public class FeAnalysisAggregateDTO {

  @JsonProperty("id")
  private Integer id;
  @JsonProperty("name")
  private String name;
  @JsonProperty("domain")
  private StandardFeatureAnalysisDomain domain;
  @JsonProperty("function")
  private AggregateFunction function;
  @JsonProperty("expression")
  private String expression;
  @JsonProperty("query")
  private String query;
  @JsonProperty("isDefault")
  private boolean isDefault;

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public StandardFeatureAnalysisDomain getDomain() {
    return domain;
  }

  public void setDomain(StandardFeatureAnalysisDomain domain) {
    this.domain = domain;
  }

  public AggregateFunction getFunction() {
    return function;
  }

  public void setFunction(AggregateFunction function) {
    this.function = function;
  }

  public String getExpression() {
    return expression;
  }

  public void setExpression(String expression) {
    this.expression = expression;
  }

  public String getQuery() {
    return query;
  }

  public void setQuery(String query) {
    this.query = query;
  }

  public boolean isDefault() {
    return isDefault;
  }

  public void setDefault(boolean aDefault) {
    isDefault = aDefault;
  }
}
