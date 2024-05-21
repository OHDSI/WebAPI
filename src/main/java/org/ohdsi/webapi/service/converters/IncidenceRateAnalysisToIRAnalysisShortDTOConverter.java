package org.ohdsi.webapi.service.converters;

import org.apache.commons.lang3.StringUtils;
import org.ohdsi.webapi.ircalc.IncidenceRateAnalysis;
import org.ohdsi.webapi.service.dto.IRAnalysisShortDTO;
import org.springframework.stereotype.Component;

@Component
public class IncidenceRateAnalysisToIRAnalysisShortDTOConverter<T extends IRAnalysisShortDTO> extends BaseCommonEntityExtToDTOExtConverter<IncidenceRateAnalysis, T> {
    @Override
    protected T createResultObject() {
        return (T) new IRAnalysisShortDTO();
    }

    @Override
    protected void doConvert(IncidenceRateAnalysis source, T target) {
        target.setId(source.getId());
        target.setName(StringUtils.trim(source.getName()));
        target.setDescription(source.getDescription());
    }
}
