package org.ohdsi.webapi.pathway.converter;

import com.odysseusinc.arachne.commons.utils.ConverterUtils;
import org.ohdsi.webapi.pathway.domain.PathwayAnalysisEntity;
import org.ohdsi.webapi.pathway.dto.PathwayAnalysisExportDTO;
import org.ohdsi.webapi.pathway.dto.PathwayCohortExportDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class PathwayAnalysisToPathwayAnalysisExportDTOConverter extends BasePathwayAnalysisToPathwayAnalysisDTOConverter<PathwayAnalysisExportDTO> {

    @Autowired
    private ConverterUtils converterUtils;

    @Override
    public void doConvert(PathwayAnalysisEntity pathwayAnalysis, PathwayAnalysisExportDTO target) {
        super.doConvert(pathwayAnalysis, target);
        target.setEventCohorts(converterUtils.convertList(new ArrayList<>(pathwayAnalysis.getEventCohorts()), PathwayCohortExportDTO.class));
        target.setTargetCohorts(converterUtils.convertList(new ArrayList<>(pathwayAnalysis.getTargetCohorts()), PathwayCohortExportDTO.class));
    }

    @Override
    protected PathwayAnalysisExportDTO createResultObject() {

        return new PathwayAnalysisExportDTO();
    }
}
