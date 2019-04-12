package org.ohdsi.webapi.service.converters;

import org.ohdsi.webapi.ircalc.IncidenceRateAnalysis;
import org.ohdsi.webapi.service.dto.IRAnalysisShortDTO;
import org.ohdsi.webapi.util.UserUtils;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.stereotype.Component;

@Component
public class IncidenceRateAnalysisToIRAnalysisShortDTOConverter extends BaseCommonEntityToDTOConverter<IncidenceRateAnalysis, IRAnalysisShortDTO>{
    public IncidenceRateAnalysisToIRAnalysisShortDTOConverter(GenericConversionService conversionService) {

        conversionService.addConverter(this);
    }

    @Override
    protected IRAnalysisShortDTO newTarget() {
        return new IRAnalysisShortDTO();
    }

    @Override
    protected void doConvert(IRAnalysisShortDTO target, IncidenceRateAnalysis source) {
        target.setId(source.getId());
        target.setName(source.getName());
        target.setDescription(source.getDescription());
        target.setCreatedBy(UserUtils.nullSafeLogin(source.getCreatedBy()));
        target.setCreatedDate(source.getCreatedDate());
        target.setModifiedBy(UserUtils.nullSafeLogin(source.getModifiedBy()));
        target.setModifiedDate(source.getModifiedDate());
    }
}
