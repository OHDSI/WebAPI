package org.ohdsi.webapi.pathway.domain;

import org.ohdsi.webapi.common.generation.CommonGeneration;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "pathway_analysis_generation")
public class PathwayAnalysisGenerationEntity extends CommonGeneration {

    @ManyToOne(targetEntity = PathwayAnalysisEntity.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "pathway_analysis_id")
    private PathwayAnalysisEntity pathwayAnalysis;

    public PathwayAnalysisEntity getPathwayAnalysis() {

        return pathwayAnalysis;
    }
}
