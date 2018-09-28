package org.ohdsi.webapi.feanalysis.converter;

import org.ohdsi.webapi.feanalysis.domain.FeAnalysisEntity;
import org.ohdsi.webapi.feanalysis.domain.FeAnalysisWithCriteriaEntity;
import org.ohdsi.webapi.feanalysis.dto.FeAnalysisCriteriaDTO;
import org.ohdsi.webapi.feanalysis.dto.FeAnalysisDTO;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class FeAnalysisEntityToFeAnalysisDTOConverter extends BaseFeAnalysisEntityToFeAnalysisDTOConverter<FeAnalysisDTO> {
    
    @Override
    public FeAnalysisDTO convert(final FeAnalysisEntity source) {
        final FeAnalysisDTO dto = super.convert(source);
        dto.setDesign(convertDesignToJson(source));
        return dto;
    }

    @Override
    protected FeAnalysisDTO createResultObject() {
        return new FeAnalysisDTO();
    }

    private Object convertDesignToJson(final FeAnalysisEntity source) {
        switch (source.getType()) {
            case CRITERIA_SET:
                return ((FeAnalysisWithCriteriaEntity) source).getDesign()
                        .stream()
                        .map(c -> new FeAnalysisCriteriaDTO(c.getName(), c.getExpression()))
                        .collect(Collectors.toList());
            default:
                return source.getDesign();
        }
    }
}
