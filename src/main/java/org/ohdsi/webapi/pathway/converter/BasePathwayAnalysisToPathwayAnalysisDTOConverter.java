package org.ohdsi.webapi.pathway.converter;

import org.apache.commons.lang3.StringUtils;
import org.ohdsi.webapi.pathway.domain.PathwayAnalysisEntity;
import org.ohdsi.webapi.pathway.dto.BasePathwayAnalysisDTO;
import org.ohdsi.webapi.service.converters.BaseCommonEntityToDTOConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;

public abstract class BasePathwayAnalysisToPathwayAnalysisDTOConverter<T extends BasePathwayAnalysisDTO>
        extends BaseCommonEntityToDTOConverter<PathwayAnalysisEntity, T> {

    @Autowired
    protected ConversionService conversionService;

    @Override
    public void doConvert(PathwayAnalysisEntity source, T target) {
        target.setId(source.getId());
        target.setName(StringUtils.trim(source.getName()));
        target.setCombinationWindow(source.getCombinationWindow());
        target.setMinSegmentLength(source.getMinSegmentLength());
        target.setMinCellCount(source.getMinCellCount());
        target.setMaxDepth(source.getMaxDepth());
        target.setAllowRepeats(source.isAllowRepeats());
        target.setHashCode(source.getHashCode());
    }
}
