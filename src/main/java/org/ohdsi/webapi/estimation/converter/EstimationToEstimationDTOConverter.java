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
    public void doConvert(Estimation source, EstimationDTO target) {
        super.doConvert(source, target);
        target.setSpecification(source.getSpecification());
    }
}
