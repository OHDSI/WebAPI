package org.ohdsi.webapi.pathway.converter;

import org.apache.commons.lang3.StringUtils;
import org.ohdsi.webapi.pathway.domain.PathwayAnalysisEntity;
import org.ohdsi.webapi.pathway.dto.BasePathwayAnalysisDTO;
import org.ohdsi.webapi.service.converters.BaseCommonDTOExtToEntityExtConverter;

public abstract class BasePathwayAnalysisDTOToPathwayAnalysisConverter<S extends BasePathwayAnalysisDTO, T extends PathwayAnalysisEntity> extends
        BaseCommonDTOExtToEntityExtConverter<S, T> {

    @Override
    protected void doConvert(S source, T target) {
        target.setId(source.getId());
        target.setName(StringUtils.trim(source.getName()));
        target.setCombinationWindow(source.getCombinationWindow());
        target.setMinSegmentLength(source.getMinSegmentLength());
        target.setMinCellCount(source.getMinCellCount());
        target.setDescription(source.getDescription());
        target.setMaxDepth(source.getMaxDepth());
        target.setAllowRepeats(source.isAllowRepeats());
    }
}
