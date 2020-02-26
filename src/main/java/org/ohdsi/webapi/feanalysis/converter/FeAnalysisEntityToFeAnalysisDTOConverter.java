package org.ohdsi.webapi.feanalysis.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.ohdsi.analysis.cohortcharacterization.design.FeatureAnalysisAggregate;
import org.ohdsi.webapi.feanalysis.domain.*;
import org.ohdsi.webapi.feanalysis.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.ohdsi.analysis.cohortcharacterization.design.StandardFeatureAnalysisType.CRITERIA_SET;

@Component
public class FeAnalysisEntityToFeAnalysisDTOConverter extends BaseFeAnalysisEntityToFeAnalysisDTOConverter<FeAnalysisDTO> {

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public FeAnalysisDTO convert(final FeAnalysisEntity source) {
        final FeAnalysisDTO dto = super.convert(source);
        dto.setDesign(convertDesignToJson(source));
        if (CRITERIA_SET.equals(source.getType())){
            FeAnalysisWithConceptSetDTO dtoWithConceptSet = (FeAnalysisWithConceptSetDTO) dto;
            FeAnalysisWithCriteriaEntity<?> sourceWithCriteria = (FeAnalysisWithCriteriaEntity) source;
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
                FeAnalysisWithCriteriaEntity<?> sourceWithCriteria = (FeAnalysisWithCriteriaEntity<?>) source;
                return sourceWithCriteria.getDesign()
                          .stream()
                          .map(this::convertCriteria)
                          .map(c -> (JsonNode)objectMapper.valueToTree(c))
                          .collect(Collectors.toList());
            default:
                return source.getDesign();
        }
    }

    private BaseFeAnalysisCriteriaDTO convertCriteria(FeAnalysisCriteriaEntity criteriaEntity){
        BaseFeAnalysisCriteriaDTO criteriaDTO;
        if (criteriaEntity instanceof FeAnalysisCriteriaGroupEntity) {
            FeAnalysisCriteriaGroupEntity groupEntity = (FeAnalysisCriteriaGroupEntity) criteriaEntity;
            criteriaDTO = new FeAnalysisCriteriaDTO(groupEntity.getId(), groupEntity.getName(), groupEntity.getExpression());
        } else if (criteriaEntity instanceof FeAnalysisWindowedCriteriaEntity) {
            FeAnalysisWindowedCriteriaEntity w = (FeAnalysisWindowedCriteriaEntity) criteriaEntity;
            criteriaDTO = new FeAnalysisWindowedCriteriaDTO(w.getId(), w.getName(), w.getExpression());
        } else if (criteriaEntity instanceof FeAnalysisDemographicCriteriaEntity) {
            FeAnalysisDemographicCriteriaEntity d = (FeAnalysisDemographicCriteriaEntity) criteriaEntity;
            criteriaDTO = new FeAnalysisDemographicCriteriaDTO(d.getId(), d.getName(), d.getExpression());
        } else {
            throw new IllegalArgumentException(String.format("Cannot convert criteria entity, %s is not supported", criteriaEntity));
        }
        criteriaDTO.setAggregate(conversionService.convert(criteriaEntity.getAggregate(), FeAnalysisAggregateDTO.class));
        return criteriaDTO;
    }
}
