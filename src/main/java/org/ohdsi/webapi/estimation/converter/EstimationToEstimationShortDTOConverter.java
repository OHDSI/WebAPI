package org.ohdsi.webapi.estimation.converter;

import org.ohdsi.webapi.converter.BaseConversionServiceAwareConverter;
import org.ohdsi.webapi.estimation.Estimation;
import org.ohdsi.webapi.estimation.dto.EstimationShortDTO;
import org.ohdsi.webapi.util.ConversionUtils;
import org.springframework.stereotype.Component;

@Component
public class EstimationToEstimationShortDTOConverter <T extends EstimationShortDTO> extends BaseConversionServiceAwareConverter<Estimation, T> {

    @Override
    protected T createResultObject() {

        return (T) new EstimationShortDTO();
    }
    
    @Override
    public T convert(Estimation source) {

        T result = createResultObject(source);
        ConversionUtils.convertMetadata(conversionService, source, result);
        result.setId(source.getId());
        result.setName(source.getName());
        result.setDescription(source.getDescription());
        result.setType(source.getType());
        return result;
    }
}
