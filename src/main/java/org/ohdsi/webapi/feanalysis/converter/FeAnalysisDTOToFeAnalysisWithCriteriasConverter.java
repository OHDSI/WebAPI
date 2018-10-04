package org.ohdsi.webapi.feanalysis.converter;

import org.ohdsi.analysis.Utils;
import org.ohdsi.webapi.feanalysis.domain.FeAnalysisCriteriaEntity;
import org.ohdsi.webapi.feanalysis.domain.FeAnalysisWithCriteriaEntity;
import org.ohdsi.webapi.feanalysis.dto.FeAnalysisCriteriaDTO;
import org.ohdsi.webapi.feanalysis.dto.FeAnalysisDTO;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

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
                    criteriaEntity.setExpressionString(Utils.serialize(typifiedCriteria.getExpression()));
                    criteriaEntity.setConceptsetsString(Utils.serialize(typifiedCriteria.getConceptSets()));
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
