package org.ohdsi.webapi.pathway.domain;

import org.ohdsi.webapi.cohortdefinition.CohortDefinition;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.SequenceGenerator;

@MappedSuperclass
public abstract class PathwayCohort {

    @Id
    @SequenceGenerator(name = "pathway_cohorts_pk_sequence", sequenceName = "pathway_cohorts_sequence", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pathway_cohorts_pk_sequence")
    protected Integer id;

    @Column
    protected String name;

    @ManyToOne
    @JoinColumn(name = "cohort_definition_id")
    protected CohortDefinition cohortDefinition;

    @ManyToOne
    @JoinColumn(name = "pathway_analysis_id")
    protected PathwayAnalysisEntity pathwayAnalysis;

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

    public CohortDefinition getCohortDefinition() {

        return cohortDefinition;
    }

    public void setCohortDefinition(CohortDefinition cohortDefinition) {

        this.cohortDefinition = cohortDefinition;
    }

    public PathwayAnalysisEntity getPathwayAnalysis() {

        return pathwayAnalysis;
    }

    public void setPathwayAnalysis(PathwayAnalysisEntity pathwayAnalysis) {

        this.pathwayAnalysis = pathwayAnalysis;
    }
}
