package org.ohdsi.webapi.pathway.converter;

import com.odysseusinc.arachne.commons.utils.ConverterUtils;
import org.ohdsi.webapi.pathway.domain.PathwayAnalysisEntity;
import org.ohdsi.webapi.pathway.dto.PathwayAnalysisDTO;
import org.ohdsi.webapi.pathway.dto.PathwayCohortDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class PathwayAnalysisToPathwayAnalysisDTOConverter extends BasePathwayAnalysisToPathwayAnalysisDTOConverter<PathwayAnalysisDTO> {

    @Autowired
    private ConverterUtils converterUtils;

    @Override
    public PathwayAnalysisDTO convert(PathwayAnalysisEntity pathwayAnalysis) {

        PathwayAnalysisDTO dto = super.convert(pathwayAnalysis);

        dto.setEventCohorts(converterUtils.convertList(new ArrayList<>(pathwayAnalysis.getEventCohorts()), PathwayCohortDTO.class));
        dto.setTargetCohorts(converterUtils.convertList(new ArrayList<>(pathwayAnalysis.getTargetCohorts()), PathwayCohortDTO.class));

        return dto;
    }

    @Override
    protected PathwayAnalysisDTO getTargetObject() {

        return new PathwayAnalysisDTO();
    }
}
