package org.ohdsi.webapi.feanalysis.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import org.ohdsi.analysis.TableJoin;
import org.ohdsi.analysis.cohortcharacterization.design.AggregateFunction;
import org.ohdsi.analysis.cohortcharacterization.design.FeatureAnalysisAggregate;
import org.ohdsi.analysis.cohortcharacterization.design.StandardFeatureAnalysisDomain;
import org.ohdsi.circe.cohortdefinition.builders.CriteriaColumn;

public class FeAnalysisAggregateDTO implements FeatureAnalysisAggregate {

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
  @JsonProperty("missingMeansZero")
  private boolean missingMeansZero;
  @JsonProperty("additionalColumns")
  private List<CriteriaColumn> columns;  

  @Override
  public List<CriteriaColumn> getAdditionalColumns() {

    return columns;
  }

  public void setAdditionalColumns(List<CriteriaColumn> columns) {
    this.columns = columns;
  }  

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

  public boolean isMissingMeansZero() {
    return missingMeansZero;
  }

  public void setMissingMeansZero(boolean missingMeansZero) {
    this.missingMeansZero = missingMeansZero;
  }
  @JsonIgnore
  @Override
  /* this is required by the interface, although not used anywhere */
  public boolean hasQuery() {
    return false;
  }  

}
