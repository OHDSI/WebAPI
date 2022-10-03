package org.ohdsi.webapi.pathway.converter;

import org.apache.commons.lang3.StringUtils;
import org.ohdsi.webapi.pathway.domain.PathwayAnalysisEntity;
import org.ohdsi.webapi.pathway.dto.BasePathwayAnalysisDTO;
import org.ohdsi.webapi.service.converters.BaseCommonEntityExtToDTOExtConverter;
import org.ohdsi.webapi.tag.dto.TagDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class BasePathwayAnalysisToPathwayAnalysisDTOConverter<T extends BasePathwayAnalysisDTO>
        extends BaseCommonEntityExtToDTOExtConverter<PathwayAnalysisEntity, T> {

    @Autowired
    protected ConversionService conversionService;

    @Override
    public void doConvert(PathwayAnalysisEntity source, T target) {
        target.setId(source.getId());
        target.setName(StringUtils.trim(source.getName()));
        target.setCombinationWindow(source.getCombinationWindow());
        target.setMinSegmentLength(source.getMinSegmentLength());
        target.setMinCellCount(source.getMinCellCount());
        target.setDescription(source.getDescription());
        target.setMaxDepth(source.getMaxDepth());
        target.setAllowRepeats(source.isAllowRepeats());
        target.setHashCode(source.getHashCode());
    }
}
