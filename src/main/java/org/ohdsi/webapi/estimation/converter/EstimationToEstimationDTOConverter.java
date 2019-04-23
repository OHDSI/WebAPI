package org.ohdsi.webapi.estimation.converter;

import org.ohdsi.webapi.converter.BaseConversionServiceAwareConverter;
import org.ohdsi.webapi.estimation.Estimation;
import org.ohdsi.webapi.estimation.dto.EstimationDTO;
import org.ohdsi.webapi.user.dto.UserDTO;
import org.springframework.stereotype.Component;

@Component
public class EstimationToEstimationDTOConverter extends BaseConversionServiceAwareConverter<Estimation, EstimationDTO> {

    @Override
    public EstimationDTO convert(Estimation source) {

        EstimationDTO result = new EstimationDTO();
        result.setId(source.getId());
        result.setName(source.getName());
        result.setDescription(source.getDescription());
        result.setSpecification(source.getSpecification());
        result.setType(source.getType());
        result.setCreatedBy(conversionService.convert(source.getCreatedBy(), UserDTO.class));
        result.setCreatedDate(source.getCreatedDate());
        result.setModifiedBy(conversionService.convert(source.getModifiedBy(), UserDTO.class));
        result.setModifiedDate(source.getModifiedDate());
        return result;
    }
}
