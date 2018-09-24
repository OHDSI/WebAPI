package org.ohdsi.webapi.pathway.domain;

import org.ohdsi.webapi.common.generation.CommonGeneration;
import org.ohdsi.webapi.pathway.converter.SerializedPathwayAnalysisToPathwayAnalysisConverter;

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

    public PathwayAnalysisEntity getDesign() {
        return new SerializedPathwayAnalysisToPathwayAnalysisConverter().convertToEntityAttribute(this.info.getDesign());
    }
}
