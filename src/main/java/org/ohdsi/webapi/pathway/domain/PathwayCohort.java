package org.ohdsi.webapi.pathway.domain;

import org.ohdsi.webapi.cohortdefinition.CohortDefinition;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.SequenceGenerator;
import java.util.Objects;

@MappedSuperclass
public abstract class PathwayCohort {

    @Id
    @SequenceGenerator(name = "pathway_cohort_pk_sequence", sequenceName = "pathway_cohort_sequence", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pathway_cohort_pk_sequence")
    protected Integer id;

    @Column
    protected String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cohort_definition_id")
    protected CohortDefinition cohortDefinition;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pathway_analysis_id")
    protected PathwayAnalysisEntity pathwayAnalysis;

    @Override
    public int hashCode() {

        return Objects.hash(this.getCohortDefinition().getId());
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }

        if (!(obj instanceof PathwayCohort)) {
            return false;
        }

        final PathwayCohort compare = (PathwayCohort) obj;
        return Objects.equals(getCohortDefinition().getId(), compare.getCohortDefinition().getId());
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
