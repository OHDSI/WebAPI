package org.ohdsi.webapi.feanalysis.domain;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.ohdsi.analysis.WithId;
import org.ohdsi.analysis.cohortcharacterization.design.AggregateFunction;
import org.ohdsi.analysis.cohortcharacterization.design.FeatureAnalysisAggregate;
import org.ohdsi.analysis.cohortcharacterization.design.StandardFeatureAnalysisDomain;

import javax.persistence.*;

@Entity
@Table(name = "fe_analysis_aggregate")
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

  @Column(name = "agg_query")
  private String query;

  @Column(name = "is_default")
  private boolean isDefault;

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

  public void setFunction(AggregateFunction function) {

    this.function = function;
  }

  public String getExpression() {

    return expression;
  }

  @Override
  public boolean hasQuery() {

    return StringUtils.isNotBlank(this.query);
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
