package org.ohdsi.webapi.service.converters;

import org.ohdsi.webapi.ircalc.IncidenceRateAnalysis;
import org.ohdsi.webapi.service.dto.IRAnalysisDTO;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.stereotype.Component;

@Component
public class IncidenceRateAnalysisToIRAnalysisDTOConverter extends IncidenceRateAnalysisToIRAnalysisShortDTOConverter<IRAnalysisDTO> {

    public IncidenceRateAnalysisToIRAnalysisDTOConverter(GenericConversionService conversionService) {
        super(conversionService);
        conversionService.addConverter(this);
    }

    @Override
    protected IRAnalysisDTO newTarget() {
        return new IRAnalysisDTO();
    }

    @Override
    protected void doConvert(IRAnalysisDTO target, IncidenceRateAnalysis source) {
        super.doConvert(target, source);
        target.setExpression(source.getDetails() != null ? source.getDetails().getExpression() : null);
    }
}
