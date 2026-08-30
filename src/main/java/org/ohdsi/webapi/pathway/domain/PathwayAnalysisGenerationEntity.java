package org.ohdsi.webapi.pathway.domain;

import org.ohdsi.webapi.common.generation.CommonGeneration;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

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
