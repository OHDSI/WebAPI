package org.ohdsi.webapi.service.converters;

import org.ohdsi.webapi.ircalc.IncidenceRateAnalysis;
import org.ohdsi.webapi.service.dto.IRAnalysisDTO;
import org.ohdsi.webapi.util.UserUtils;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.stereotype.Component;

@Component
public class IncidenceRateAnalysisToIRAnalysisDTOConverter extends BaseCommonEntityToDTOConverter<IncidenceRateAnalysis, IRAnalysisDTO> {

    public IncidenceRateAnalysisToIRAnalysisDTOConverter(GenericConversionService conversionService) {

        conversionService.addConverter(this);
    }

    @Override
    protected IRAnalysisDTO newTarget() {
        return new IRAnalysisDTO();
    }

    @Override
    protected void doConvert(IRAnalysisDTO target, IncidenceRateAnalysis source) {
        target.setId(source.getId());
        target.setName(source.getName());
        target.setDescription(source.getDescription());
        target.setCreatedBy(UserUtils.nullSafeLogin(source.getCreatedBy()));
        target.setCreatedDate(source.getCreatedDate());
        target.setModifiedBy(UserUtils.nullSafeLogin(source.getModifiedBy()));
        target.setModifiedDate(source.getModifiedDate());
        target.setExpression(source.getDetails() != null ? source.getDetails().getExpression() : null);
    }
}
