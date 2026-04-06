package org.ohdsi.webapi.ircalc.converter;

import org.ohdsi.webapi.converter.BaseConversionServiceAwareConverter;
import org.ohdsi.webapi.ircalc.IncidenceRateAnalysis;
import org.ohdsi.webapi.versioning.domain.CohortVersion;
import org.ohdsi.webapi.versioning.domain.IRVersion;
import org.springframework.stereotype.Component;

@Component
public class IRAnalysisToIRVersionConverter
        extends BaseConversionServiceAwareConverter<IncidenceRateAnalysis, IRVersion> {
    @Override
    public IRVersion convert(IncidenceRateAnalysis source) {
        IRVersion target = new IRVersion();
        target.setAssetId(source.getId());
        target.setDescription(source.getDescription());
        target.setAssetJson(source.getDetails().getExpression());

        return target;
    }
}
