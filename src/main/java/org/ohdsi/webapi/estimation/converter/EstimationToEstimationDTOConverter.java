package org.ohdsi.webapi.estimation.converter;

import org.ohdsi.webapi.estimation.Estimation;
import org.ohdsi.webapi.estimation.dto.EstimationDTO;
import org.springframework.stereotype.Component;

@Component
public class EstimationToEstimationDTOConverter extends EstimationToEstimationShortDTOConverter<EstimationDTO> {

    @Override
    protected EstimationDTO createResultObject() {

        return new EstimationDTO();
    }

    @Override
    public EstimationDTO convert(Estimation source) {

        EstimationDTO result = super.convert(source);        
        result.setSpecification(source.getSpecification());        
        return result;
    }
}
