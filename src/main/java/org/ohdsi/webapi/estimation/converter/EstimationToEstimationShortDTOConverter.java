package org.ohdsi.webapi.estimation.converter;

import org.ohdsi.webapi.converter.BaseConversionServiceAwareConverter;
import org.ohdsi.webapi.estimation.Estimation;
import org.ohdsi.webapi.estimation.dto.EstimationShortDTO;
import org.ohdsi.webapi.user.dto.UserDTO;
import org.springframework.stereotype.Component;

@Component
public class EstimationToEstimationShortDTOConverter extends BaseConversionServiceAwareConverter<Estimation, EstimationShortDTO> {

    @Override
    public EstimationShortDTO convert(Estimation source) {

        EstimationShortDTO result = new EstimationShortDTO();
        result.setId(source.getId());
        result.setName(source.getName());
        result.setDescription(source.getDescription());
        result.setType(source.getType());
        result.setCreatedBy(conversionService.convert(source.getCreatedBy(), UserDTO.class));
        result.setCreatedDate(source.getCreatedDate());
        result.setModifiedBy(conversionService.convert(source.getModifiedBy(), UserDTO.class));
        result.setModifiedDate(source.getModifiedDate());
        return result;
    }
}
