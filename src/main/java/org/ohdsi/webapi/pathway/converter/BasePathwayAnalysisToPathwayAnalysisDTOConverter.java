package org.ohdsi.webapi.pathway.converter;

import org.ohdsi.webapi.user.dto.UserDTO;
import org.ohdsi.webapi.converter.BaseConversionServiceAwareConverter;
import org.ohdsi.webapi.pathway.domain.PathwayAnalysisEntity;
import org.ohdsi.webapi.pathway.dto.BasePathwayAnalysisDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;

public abstract class BasePathwayAnalysisToPathwayAnalysisDTOConverter<T extends BasePathwayAnalysisDTO> extends BaseConversionServiceAwareConverter<PathwayAnalysisEntity, T> {

    @Autowired
    protected ConversionService conversionService;

    @Override
    public T convert(PathwayAnalysisEntity source) {

        T dto = getTargetObject();

        dto.setId(source.getId());
        dto.setName(source.getName());
        dto.setCombinationWindow(source.getCombinationWindow());
				dto.setMinSegmentLength(source.getMinSegmentLength());
        dto.setMinCellCount(source.getMinCellCount());
        dto.setMaxDepth(source.getMaxDepth());
        dto.setAllowRepeats(source.isAllowRepeats());
        dto.setCreatedBy(conversionService.convert(source.getCreatedBy(), UserDTO.class));
        dto.setCreatedDate(source.getCreatedDate());
        dto.setModifiedBy(conversionService.convert(source.getModifiedBy(), UserDTO.class));
        dto.setModifiedDate(source.getModifiedDate());
        dto.setHashCode(source.getHashCode());

        return dto;
    }

    protected abstract T getTargetObject();
}
