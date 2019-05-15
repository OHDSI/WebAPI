package org.ohdsi.webapi.pathway.converter;

import org.ohdsi.webapi.converter.BaseConversionServiceAwareConverter;
import org.ohdsi.webapi.pathway.domain.PathwayAnalysisEntity;
import org.ohdsi.webapi.pathway.dto.BasePathwayAnalysisDTO;

public abstract class BasePathwayAnalysisDTOToPathwayAnalysisConverter<T extends BasePathwayAnalysisDTO> extends BaseConversionServiceAwareConverter<T, PathwayAnalysisEntity> {

    @Override
    public PathwayAnalysisEntity convert(T source) {

        PathwayAnalysisEntity pathwayAnalysis = new PathwayAnalysisEntity();

        pathwayAnalysis.setId(source.getId());
        pathwayAnalysis.setName(source.getName());
        pathwayAnalysis.setCombinationWindow(source.getCombinationWindow());
				pathwayAnalysis.setMinSegmentLength(source.getMinSegmentLength());
        pathwayAnalysis.setMinCellCount(source.getMinCellCount());
        pathwayAnalysis.setMaxDepth(source.getMaxDepth());
        pathwayAnalysis.setAllowRepeats(source.isAllowRepeats());

        return pathwayAnalysis;
    }
}
