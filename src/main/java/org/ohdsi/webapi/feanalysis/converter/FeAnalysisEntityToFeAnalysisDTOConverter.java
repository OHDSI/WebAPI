package org.ohdsi.webapi.feanalysis.converter;

import org.ohdsi.webapi.feanalysis.domain.FeAnalysisEntity;
import org.ohdsi.webapi.feanalysis.domain.FeAnalysisWithCriteriaEntity;
import org.ohdsi.webapi.feanalysis.dto.FeAnalysisCriteriaDTO;
import org.ohdsi.webapi.feanalysis.dto.FeAnalysisDTO;
import org.ohdsi.webapi.feanalysis.dto.FeAnalysisWithConceptSetDTO;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

import static org.ohdsi.analysis.cohortcharacterization.design.StandardFeatureAnalysisType.CRITERIA_SET;

@Component
public class FeAnalysisEntityToFeAnalysisDTOConverter extends BaseFeAnalysisEntityToFeAnalysisDTOConverter<FeAnalysisDTO> {
    
    @Override
    public FeAnalysisDTO convert(final FeAnalysisEntity source) {
        final FeAnalysisDTO dto = super.convert(source);
        dto.setDesign(convertDesignToJson(source));
        if (CRITERIA_SET.equals(source.getType())){
            FeAnalysisWithConceptSetDTO dtoWithConceptSet = (FeAnalysisWithConceptSetDTO) dto;
            FeAnalysisWithCriteriaEntity sourceWithCriteria = (FeAnalysisWithCriteriaEntity) source;
            dtoWithConceptSet.setConceptSets(sourceWithCriteria.getConceptSets());
        }
        return dto;
    }

    @Override
    protected FeAnalysisDTO createResultObject(FeAnalysisEntity feAnalysisEntity) {
        switch (feAnalysisEntity.getType()){
            case CRITERIA_SET:
              return new FeAnalysisWithConceptSetDTO();
            default:
              return new FeAnalysisDTO();
        }
    }

    private Object convertDesignToJson(final FeAnalysisEntity source) {
        switch (source.getType()) {
            case CRITERIA_SET:
                return ((FeAnalysisWithCriteriaEntity) source).getDesign()
                        .stream()
                        .map(c -> new FeAnalysisCriteriaDTO(c.getId(), c.getName(), c.getExpression()))
                        .collect(Collectors.toList());
            default:
                return source.getDesign();
        }
    }
}
