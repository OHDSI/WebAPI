package org.ohdsi.webapi.pathway.converter;

import com.odysseusinc.arachne.commons.utils.ConverterUtils;
import org.ohdsi.webapi.pathway.domain.PathwayAnalysisEntity;
import org.ohdsi.webapi.pathway.domain.PathwayEventCohort;
import org.ohdsi.webapi.pathway.domain.PathwayTargetCohort;
import org.ohdsi.webapi.pathway.dto.PathwayAnalysisExportDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;

@Component
public class PathwayAnalysisExportDTOToPathwayAnalysisEntityConverter extends
        BasePathwayAnalysisDTOToPathwayAnalysisConverter<PathwayAnalysisExportDTO, PathwayAnalysisEntity> {

    @Autowired
    private ConverterUtils converterUtils;

    @Override
    public void doConvert(PathwayAnalysisExportDTO source, PathwayAnalysisEntity target) {
        super.doConvert(source, target);
        target.setEventCohorts(new HashSet<>(converterUtils.convertList(source.getEventCohorts(), PathwayEventCohort.class)));
        target.setTargetCohorts(new HashSet<>(converterUtils.convertList(source.getTargetCohorts(), PathwayTargetCohort.class)));
    }

    @Override
    protected PathwayAnalysisEntity createResultObject() {
        return new PathwayAnalysisEntity();
    }
}
