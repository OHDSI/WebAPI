package org.ohdsi.webapi.feanalysis.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.Type;
import org.ohdsi.analysis.Utils;
import org.ohdsi.circe.cohortdefinition.CriteriaGroup;
import org.ohdsi.analysis.cohortcharacterization.design.CriteriaFeature;

@Entity
@Table(name = "fe_analysis_criteria")
public class FeAnalysisCriteriaEntity implements CriteriaFeature {
    
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

    @Override
    public CriteriaGroup getExpression() {
        return getCriteriaGroup();
    }
    
    private CriteriaGroup getCriteriaGroup() {
        return Utils.deserialize(this.expressionString, CriteriaGroup.class);
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
