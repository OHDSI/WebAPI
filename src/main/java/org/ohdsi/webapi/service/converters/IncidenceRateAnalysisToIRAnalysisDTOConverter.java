package org.ohdsi.webapi.service.converters;

import org.ohdsi.webapi.ircalc.IncidenceRateAnalysis;
import org.ohdsi.webapi.service.dto.IRAnalysisDTO;
import org.springframework.stereotype.Component;

@Component
public class IncidenceRateAnalysisToIRAnalysisDTOConverter extends IncidenceRateAnalysisToIRAnalysisShortDTOConverter<IRAnalysisDTO> {
    @Override
    protected IRAnalysisDTO createResultObject() {
        return new IRAnalysisDTO();
    }

    @Override
    protected void doConvert(IncidenceRateAnalysis source, IRAnalysisDTO target) {
        super.doConvert(source, target);
        target.setExpression(source.getDetails() != null ? source.getDetails().getExpression() : null);
    }
}
