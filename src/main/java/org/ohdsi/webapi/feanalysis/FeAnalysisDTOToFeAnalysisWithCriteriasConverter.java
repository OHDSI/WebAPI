package org.ohdsi.webapi.feanalysis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class FeAnalysisDTOToFeAnalysisWithCriteriasConverter extends BaseFeAnalysisDTOToFeAnalysisConverter<FeAnalysisWithCriteriaEntity> {
    
    private ObjectMapper objectMapper = new ObjectMapper();
    
    @PostConstruct
    private void init() {
        objectMapper.disable(
                MapperFeature.AUTO_DETECT_CREATORS,
                MapperFeature.AUTO_DETECT_GETTERS,
                MapperFeature.AUTO_DETECT_IS_GETTERS
        );
    }
    
    @Override
    public FeAnalysisWithCriteriaEntity convert(final FeAnalysisDTO source) {
        final FeAnalysisWithCriteriaEntity baseEntity = super.convert(source);
        baseEntity.setDesign(buildCriteriaList(source.getDesign()));
        return baseEntity;
    }

    private List<FeAnalysisCriteriaEntity> buildCriteriaList(final Object design) {
        final List<FeAnalysisCriteriaEntity> result = new ArrayList<>();
        if (!(design instanceof List<?>)) {
            throw new IllegalArgumentException("Design: " + design.toString() + " cannot be converted to Criteria List");
        } else {
            for (final Object criteria : (List<?>) design) {
                if (!(criteria instanceof FeAnalysisCriteriaDTO)) {
                    throw new IllegalArgumentException("Object " + criteria.toString() + " cannot be converted to Criteria");
                } else {
                    final FeAnalysisCriteriaDTO convertedCriteria = (FeAnalysisCriteriaDTO) criteria;
                    final FeAnalysisCriteriaEntity criteriaEntity = new FeAnalysisCriteriaEntity();
                    criteriaEntity.setExpressionString(convertExpressionToString(convertedCriteria));
                    criteriaEntity.setName(convertedCriteria.getName());
                    result.add(criteriaEntity);
                }
            }
        }
        return result;
    }

    private String convertExpressionToString(final FeAnalysisCriteriaDTO convertedCriteria) {
        try {
            return objectMapper.writeValueAsString(convertedCriteria.getExpression());
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
    }


    @Override
    protected FeAnalysisWithCriteriaEntity createResultObject() {
        return new FeAnalysisWithCriteriaEntity();
    }
}
