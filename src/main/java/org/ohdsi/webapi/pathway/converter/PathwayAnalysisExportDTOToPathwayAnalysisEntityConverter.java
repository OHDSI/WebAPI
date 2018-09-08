package org.ohdsi.webapi.pathway.converter;

import com.odysseusinc.arachne.commons.utils.ConverterUtils;
import org.ohdsi.webapi.pathway.domain.PathwayAnalysisEntity;
import org.ohdsi.webapi.pathway.dto.PathwayAnalysisDTO;
import org.ohdsi.webapi.pathway.dto.PathwayAnalysisExportDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PathwayAnalysisExportDTOToPathwayAnalysisEntityConverter extends BasePathwayAnalysisDTOToPathwayAnalysisConverter<PathwayAnalysisExportDTO> {

    @Autowired
    private ConverterUtils converterUtils;

    public PathwayAnalysisEntity convert(PathwayAnalysisExportDTO source) {

        PathwayAnalysisEntity result = super.convert(source);

        // TODO:

//        result.setEventCohorts(converterUtils.convertList(source.getEventCohorts(), PathwayEventCohort.class));
//        result.setTargetCohorts(converterUtils.convertList(source.getEventCohorts(), PathwayTargetCohort.class));

        return result;
    }
}
