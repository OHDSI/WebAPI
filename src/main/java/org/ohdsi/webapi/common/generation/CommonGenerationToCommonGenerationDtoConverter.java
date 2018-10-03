package org.ohdsi.webapi.common.generation;

import org.ohdsi.webapi.converter.BaseConversionServiceAwareConverter;
import org.springframework.stereotype.Component;

@Component
public class CommonGenerationToCommonGenerationDtoConverter extends BaseConversionServiceAwareConverter<CommonGeneration, CommonGenerationDTO> {

    @Override
    public CommonGenerationDTO convert(final CommonGeneration source) {
        final CommonGenerationDTO resultObject = new CommonGenerationDTO();
        
        resultObject.setId(source.getId());
        resultObject.setStatus(source.getStatus());
        resultObject.setSourceKey(source.getSource().getSourceKey());
        resultObject.setHashCode(source.getHashCode());
        resultObject.setStartTime(source.getStartTime());
        resultObject.setEndTime(source.getEndTime());
        
        return resultObject;
    }
}
