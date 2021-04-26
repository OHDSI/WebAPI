package org.ohdsi.webapi.pathway.converter;

import org.apache.commons.lang3.StringUtils;
import org.ohdsi.webapi.pathway.domain.PathwayAnalysisEntity;
import org.ohdsi.webapi.pathway.dto.BasePathwayAnalysisDTO;
import org.ohdsi.webapi.service.converters.BaseCommonDTOExtToEntityExtConverter;

public abstract class BasePathwayAnalysisDTOToPathwayAnalysisConverter<S extends BasePathwayAnalysisDTO, T extends PathwayAnalysisEntity> extends
        BaseCommonDTOExtToEntityExtConverter<S, T> {

    @Override
    protected void doConvert(S source, T target) {
        source.setId(source.getId());
        source.setName(StringUtils.trim(source.getName()));
        source.setCombinationWindow(source.getCombinationWindow());
        source.setMinSegmentLength(source.getMinSegmentLength());
        source.setMinCellCount(source.getMinCellCount());
        source.setMaxDepth(source.getMaxDepth());
        source.setAllowRepeats(source.isAllowRepeats());
    }
}
