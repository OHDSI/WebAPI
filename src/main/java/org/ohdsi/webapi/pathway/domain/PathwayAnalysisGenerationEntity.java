package org.ohdsi.webapi.pathway.domain;

import org.ohdsi.webapi.common.generation.AnalysisGenerationInfo;
import org.ohdsi.webapi.common.generation.CommonGeneration;
import org.ohdsi.webapi.pathway.converter.SerializedPathwayAnalysisToPathwayAnalysisConverter;
import org.ohdsi.webapi.shiro.Entities.UserEntity;

import javax.persistence.Embedded;
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

    @Embedded
    private AnalysisGenerationInfo info;

    public PathwayAnalysisEntity getPathwayAnalysis() {

        return pathwayAnalysis;
    }

    public PathwayAnalysisEntity getDesign() {
        return new SerializedPathwayAnalysisToPathwayAnalysisConverter().convertToEntityAttribute(info.getDesign());
    }

    public Integer getHashCode() {
        return this.getDesign().hashCode();
    }

    public UserEntity getCreatedBy() {

        return this.info.getCreatedBy();
    }
}
