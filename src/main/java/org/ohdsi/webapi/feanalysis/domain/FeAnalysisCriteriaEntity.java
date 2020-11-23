package org.ohdsi.webapi.feanalysis.domain;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import org.hibernate.annotations.DiscriminatorOptions;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.ohdsi.analysis.WithId;

@Entity
@Table(name = "fe_analysis_criteria")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "criteria_type")
@DiscriminatorOptions(force = false)
public abstract class FeAnalysisCriteriaEntity implements WithId<Long> {
    
    @Id
    @GenericGenerator(
        name = "fe_analysis_criteria_generator",
        strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
        parameters = {
            @Parameter(name = "sequence_name", value = "fe_analysis_criteria_sequence"),
            @Parameter(name = "increment_size", value = "1")
        }
    )
    @GeneratedValue(generator = "fe_analysis_criteria_generator")
    private Long id;

    @Column
    private String name;
    
    @Lob
    @Column(name = "expression")
    @Type(type = "org.hibernate.type.TextType")
    private String expressionString;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "fe_aggregate_id")
    private FeAnalysisAggregateEntity aggregate;

    @ManyToOne(optional = false, targetEntity = FeAnalysisWithCriteriaEntity.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "fe_analysis_id")
    private FeAnalysisWithCriteriaEntity featureAnalysis;

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    @Override
    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public FeAnalysisWithCriteriaEntity getFeatureAnalysis() {
        return featureAnalysis;
    }

    public void setFeatureAnalysis(final FeAnalysisWithCriteriaEntity featureAnalysis) {
        this.featureAnalysis = featureAnalysis;
    }

    public String getExpressionString() {
        return expressionString;
    }

    public void setExpressionString(final String expressionString) {
        this.expressionString = expressionString;
    }

    public FeAnalysisAggregateEntity getAggregate() {
        return aggregate;
    }

    public void setAggregate(FeAnalysisAggregateEntity aggregate) {
        this.aggregate = aggregate;
    }
}
