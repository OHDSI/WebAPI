package org.ohdsi.webapi.feanalysis.domain;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.ohdsi.analysis.TableJoin;
import org.ohdsi.analysis.WithId;
import org.ohdsi.analysis.cohortcharacterization.design.AggregateFunction;
import org.ohdsi.analysis.cohortcharacterization.design.FeatureAnalysisAggregate;
import org.ohdsi.analysis.cohortcharacterization.design.StandardFeatureAnalysisDomain;
import org.ohdsi.circe.cohortdefinition.builders.CriteriaColumn;
import org.ohdsi.webapi.common.orm.EnumListType;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "fe_analysis_aggregate")
@TypeDef(typeClass = EnumListType.class, name = "enum-list")
public class FeAnalysisAggregateEntity implements FeatureAnalysisAggregate, WithId<Integer> {

  @Id
  @GenericGenerator(
          name = "fe_aggregate_generator",
          strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
          parameters = {
                  @Parameter(name = "sequence_name", value = "fe_aggregate_sequence"),
                  @Parameter(name = "increment_size", value = "1")
          }
  )
  @GeneratedValue(generator = "fe_aggregate_generator")
  private Integer id;

  @Column
  private String name;

  @Column
  @Enumerated(value = EnumType.STRING)
  private StandardFeatureAnalysisDomain domain;

  @Column(name = "agg_function")
  @Enumerated(value = EnumType.STRING)
  private AggregateFunction function;

  @Column
  private String expression;

  @Column(name = "join_table")
  private String joinTable;

  @Column(name = "join_type")
  @Enumerated(EnumType.STRING)
  private TableJoin joinType;

  @Column(name = "join_condition")
  private String joinCondition;

  @Column(name = "is_default")
  private boolean isDefault;

  @Column(name = "missing_means_zero")
  private boolean isMissingMeansZero;

	@Column(name = "criteria_columns")
  @Type(type = "enum-list", parameters = {
          @Parameter(name = "enumClass", value = "org.ohdsi.circe.cohortdefinition.builders.CriteriaColumn")
  })
  private List<CriteriaColumn> columns;

  @Override
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

  @Override
  public List<CriteriaColumn> getAdditionalColumns() {

    return columns;
  }

  public void setCriteriaColumns(List<CriteriaColumn> columns) {
    this.columns = columns;
  }

  public void setFunction(AggregateFunction function) {

    this.function = function;
  }

  public String getExpression() {

    return expression;
  }

  @Override
  public boolean hasQuery() {

    return StringUtils.isNotBlank(this.joinTable);
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

  public boolean isDefault() {
    return isDefault;
  }

  public void setDefault(boolean aDefault) {
    isDefault = aDefault;
  }

  public boolean isMissingMeansZero() {
    return isMissingMeansZero;
  }

  public void setMissingMeansZero(boolean aDefault) {
    isMissingMeansZero = aDefault;
  }
}
