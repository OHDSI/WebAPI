package org.ohdsi.webapi.feanalysis.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.ohdsi.analysis.TableJoin;
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
  @JsonProperty("joinTable")
  private String joinTable;
  @JsonProperty("joinType")
  private TableJoin joinType;
  @JsonProperty("joinCondition")
  private String joinCondition;
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

  public String getJoinTable() {
    return joinTable;
  }

  public void setJoinTable(String joinTable) {
    this.joinTable = joinTable;
  }

  public boolean isDefault() {
    return isDefault;
  }

  public void setDefault(boolean aDefault) {
    isDefault = aDefault;
  }

  public TableJoin getJoinType() {
    return joinType;
  }

  public void setJoinType(TableJoin joinType) {
    this.joinType = joinType;
  }

  public String getJoinCondition() {
    return joinCondition;
  }

  public void setJoinCondition(String joinCondition) {
    this.joinCondition = joinCondition;
  }
}
