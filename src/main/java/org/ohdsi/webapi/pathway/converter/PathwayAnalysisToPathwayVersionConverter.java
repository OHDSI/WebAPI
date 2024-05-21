package org.ohdsi.webapi.pathway.converter;

import org.ohdsi.analysis.Utils;
import org.ohdsi.analysis.pathway.design.PathwayAnalysis;
import org.ohdsi.webapi.converter.BaseConversionServiceAwareConverter;
import org.ohdsi.webapi.ircalc.IncidenceRateAnalysis;
import org.ohdsi.webapi.pathway.domain.PathwayAnalysisEntity;
import org.ohdsi.webapi.pathway.dto.PathwayAnalysisExportDTO;
import org.ohdsi.webapi.versioning.domain.IRVersion;
import org.ohdsi.webapi.versioning.domain.PathwayVersion;
import org.springframework.stereotype.Component;

@Component
public class PathwayAnalysisToPathwayVersionConverter
        extends BaseConversionServiceAwareConverter<PathwayAnalysisEntity, PathwayVersion> {
    @Override
    public PathwayVersion convert(PathwayAnalysisEntity source) {
        PathwayAnalysisExportDTO exportDTO = conversionService.convert(source, PathwayAnalysisExportDTO.class);
        String data = Utils.serialize(exportDTO);

        PathwayVersion target = new PathwayVersion();
        target.setAssetId(source.getId());
        target.setAssetJson(data);

        return target;
    }
}
