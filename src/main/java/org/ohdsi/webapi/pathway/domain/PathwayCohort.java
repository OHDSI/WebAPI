package org.ohdsi.webapi.pathway.domain;

import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.ohdsi.webapi.cohortdefinition.CohortDefinition;

@MappedSuperclass
public abstract class PathwayCohort {

    @Id
    @GenericGenerator(
        name = "pathway_cohort_generator",
        strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
        parameters = {
            @Parameter(name = "sequence_name", value = "pathway_cohort_sequence"),
            @Parameter(name = "increment_size", value = "1")
        }
    )
    @GeneratedValue(generator = "pathway_cohort_generator")
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
