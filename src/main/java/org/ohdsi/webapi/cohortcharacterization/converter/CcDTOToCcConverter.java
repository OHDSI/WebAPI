package org.ohdsi.webapi.cohortcharacterization.converter;

import com.odysseusinc.arachne.commons.utils.ConverterUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.ohdsi.standardized_analysis_api.cohortcharacterization.design.StandardFeatureAnalysisType;
import org.ohdsi.webapi.cohortcharacterization.domain.CcParamEntity;
import org.ohdsi.webapi.cohortcharacterization.dto.CcParameterDTO;
import org.ohdsi.webapi.cohortcharacterization.dto.CohortCharacterizationDTO;
import org.ohdsi.webapi.cohortcharacterization.domain.CohortCharacterizationEntity;
import org.ohdsi.webapi.cohortdefinition.CohortDefinition;
import org.ohdsi.webapi.feanalysis.dto.FeAnalysisDTO;
import org.ohdsi.webapi.feanalysis.domain.FeAnalysisEntity;
import org.ohdsi.webapi.feanalysis.domain.FeAnalysisWithCriteriaEntity;
import org.ohdsi.webapi.feanalysis.domain.FeAnalysisWithStringEntity;
import org.ohdsi.webapi.shiro.Entities.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CcDTOToCcConverter extends CcCreateDTOToCcConverter<CohortCharacterizationDTO> {
    
    @Autowired
    private ConverterUtils converterUtils;
    
    @Override
    public CohortCharacterizationEntity convert(final CohortCharacterizationDTO source) {
        final CohortCharacterizationEntity cohortCharacterization = super.convert(source);

        if (source.getCreatedBy() != null) {
            final UserEntity createdBy = new UserEntity();
            createdBy.setId(source.getCreatedBy().getId());
            cohortCharacterization.setCreatedBy(createdBy);
        }
        
        cohortCharacterization.setCreatedAt(source.getCreatedAt());
        
        cohortCharacterization.setId(source.getId());

        if (!CollectionUtils.isEmpty(source.getCohorts())) {
            final List<CohortDefinition> convertedCohortDefinitions = converterUtils.convertList(new ArrayList<>(source.getCohorts()), CohortDefinition.class);
            cohortCharacterization.setCohortDefinitions(convertedCohortDefinitions);
        }

        final Set<FeAnalysisEntity> convertedFeatureAnalyses = source.getFeatureAnalyses().stream().map(this::convertFeAnalysisAccordingToType).collect(Collectors.toSet());
        cohortCharacterization.setFeatureAnalyses(convertedFeatureAnalyses);

        final Set<CcParamEntity> convertedParameters = source.getParameters().stream().map(this::convertParameter).collect(Collectors.toSet());
        cohortCharacterization.setParameters(convertedParameters);
        
        return cohortCharacterization;
    }
    
    private CcParamEntity convertParameter(final CcParameterDTO dto) {
        return conversionService.convert(dto, CcParamEntity.class);
    }
    
    private FeAnalysisEntity convertFeAnalysisAccordingToType(final FeAnalysisDTO dto) {
        if (dto.getType() == null) {
            return conversionService.convert(dto, FeAnalysisEntity.class);
        } else if (dto.getType() == StandardFeatureAnalysisType.CRITERIA_SET) {
            return conversionService.convert(dto, FeAnalysisWithCriteriaEntity.class);
        } else {
            return conversionService.convert(dto, FeAnalysisWithStringEntity.class);
        }
    }
}
