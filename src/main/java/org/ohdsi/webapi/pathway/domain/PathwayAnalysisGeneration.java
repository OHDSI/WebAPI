package org.ohdsi.webapi.pathway.domain;

import org.ohdsi.webapi.common.CommonGeneration;
import org.ohdsi.webapi.pathway.converter.SerializedPathwayAnalysisToPathwayAnalysisConverter;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "pathway_analysis_generations")
public class PathwayAnalysisGeneration extends CommonGeneration {

    @ManyToOne(targetEntity = PathwayAnalysisEntity.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "pathway_analysis_id")
    private PathwayAnalysisEntity pathwayAnalysis;

    @Column(name = "design", updatable= false)
    @Convert(converter = SerializedPathwayAnalysisToPathwayAnalysisConverter.class)
    private PathwayAnalysisEntity design;

    public PathwayAnalysisEntity getPathwayAnalysis() {

        return pathwayAnalysis;
    }

    public void setPathwayAnalysis(PathwayAnalysisEntity pathwayAnalysis) {

        this.pathwayAnalysis = pathwayAnalysis;
    }

    public PathwayAnalysisEntity getDesign() {

        return design;
    }

    public void setDesign(PathwayAnalysisEntity design) {

        this.design = design;
    }
}
