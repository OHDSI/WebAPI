package org.ohdsi.webapi.feanalysis.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import javax.persistence.*;

import org.hibernate.annotations.DiscriminatorOptions;
import org.hibernate.annotations.Type;
import org.ohdsi.analysis.Utils;
import org.ohdsi.circe.cohortdefinition.CriteriaGroup;
import org.ohdsi.analysis.cohortcharacterization.design.CriteriaFeature;

@Entity
@Table(name = "fe_analysis_criteria")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "criteria_type")
@DiscriminatorOptions(force = false)
public abstract class FeAnalysisCriteriaEntity {
    
    @Id
    @SequenceGenerator(name = "fe_analysis_criteria_pk_sequence", sequenceName = "fe_analysis_criteria_sequence", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "fe_analysis_criteria_pk_sequence")
    private Long id;

    @Column
    private String name;
    
    @Lob
    @Column(name = "expression")
    @Type(type = "org.hibernate.type.TextType")
    private String expressionString;

    @ManyToOne(optional = false, targetEntity = FeAnalysisWithCriteriaEntity.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "fe_analysis_id")
    private FeAnalysisWithCriteriaEntity featureAnalysis;

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

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
}
