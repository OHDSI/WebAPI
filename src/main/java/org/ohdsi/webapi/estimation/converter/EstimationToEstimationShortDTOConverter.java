package org.ohdsi.webapi.estimation.converter;

import org.apache.commons.lang.StringUtils;
import org.ohdsi.webapi.estimation.Estimation;
import org.ohdsi.webapi.estimation.dto.EstimationShortDTO;
import org.ohdsi.webapi.service.converters.BaseCommonEntityToDTOConverter;
import org.springframework.stereotype.Component;

@Component
public class EstimationToEstimationShortDTOConverter <T extends EstimationShortDTO>
        extends BaseCommonEntityToDTOConverter<Estimation, T> {

    @Override
    protected T createResultObject() {
        return (T) new EstimationShortDTO();
    }
    
    @Override
    public void doConvert(Estimation source, T target) {
        target.setId(source.getId());
        target.setName(StringUtils.trim(source.getName()));
        target.setDescription(source.getDescription());
        target.setType(source.getType());
    }
}
