package org.ohdsi.webapi.feanalysis.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.ArrayList;
import java.util.List;

import org.ohdsi.analysis.Utils;
import org.ohdsi.webapi.feanalysis.dto.FeAnalysisCriteriaDTO;
import org.ohdsi.webapi.feanalysis.domain.FeAnalysisCriteriaEntity;
import org.ohdsi.webapi.feanalysis.dto.FeAnalysisDTO;
import org.ohdsi.webapi.feanalysis.domain.FeAnalysisWithCriteriaEntity;
import org.springframework.stereotype.Component;

@Component
public class FeAnalysisDTOToFeAnalysisWithCriteriasConverter extends BaseFeAnalysisDTOToFeAnalysisConverter<FeAnalysisDTO, FeAnalysisWithCriteriaEntity> {
    
    @Override
    public FeAnalysisWithCriteriaEntity convert(final FeAnalysisDTO source) {
        final FeAnalysisWithCriteriaEntity baseEntity = super.convert(source);
        baseEntity.setDesign(buildCriteriaList(source.getDesign()));
        baseEntity.getDesign().forEach(c -> c.setFeatureAnalysis(baseEntity));
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
                    final FeAnalysisCriteriaDTO typifiedCriteria = (FeAnalysisCriteriaDTO) criteria;
                    final FeAnalysisCriteriaEntity criteriaEntity = new FeAnalysisCriteriaEntity();
                    try {
                        criteriaEntity.setExpressionString(Utils.serialize(typifiedCriteria.getExpression()));
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                    criteriaEntity.setId(typifiedCriteria.getId());
                    criteriaEntity.setName(typifiedCriteria.getName());
                    result.add(criteriaEntity);
                }
            }
        }
        return result;
    }


    @Override
    protected FeAnalysisWithCriteriaEntity createResultObject() {
        return new FeAnalysisWithCriteriaEntity();
    }
}
