package org.ohdsi.webapi.cohortcharacterization.converter;

import com.odysseusinc.arachne.commons.utils.ConverterUtils;
import org.apache.commons.collections.CollectionUtils;
import org.ohdsi.analysis.CohortMetadata;
import org.ohdsi.analysis.cohortcharacterization.design.StandardFeatureAnalysisType;
import org.ohdsi.webapi.cohortcharacterization.domain.CcParamEntity;
import org.ohdsi.webapi.cohortcharacterization.domain.CohortCharacterizationEntity;
import org.ohdsi.webapi.cohortcharacterization.dto.BaseCcDTO;
import org.ohdsi.webapi.cohortcharacterization.dto.CcParameterDTO;
import org.ohdsi.webapi.cohortdefinition.CohortDefinition;
import org.ohdsi.webapi.converter.BaseConversionServiceAwareConverter;
import org.ohdsi.webapi.feanalysis.domain.FeAnalysisEntity;
import org.ohdsi.webapi.feanalysis.domain.FeAnalysisWithCriteriaEntity;
import org.ohdsi.webapi.feanalysis.domain.FeAnalysisWithStringEntity;
import org.ohdsi.webapi.feanalysis.dto.FeAnalysisShortDTO;
import org.ohdsi.webapi.shiro.Entities.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class BaseCcDTOToCcEntityConverter<T extends BaseCcDTO<? extends CohortMetadata, ? extends FeAnalysisShortDTO>>
        extends BaseConversionServiceAwareConverter<T, CohortCharacterizationEntity> {

  @Autowired
  private ConverterUtils converterUtils;

  @Override
  public CohortCharacterizationEntity convert(T source) {

    final CohortCharacterizationEntity cohortCharacterization = new CohortCharacterizationEntity();

    cohortCharacterization.setName(source.getName());

    cohortCharacterization.setId(source.getId());

    if (!CollectionUtils.isEmpty(source.getCohorts())) {
      final List<CohortDefinition> convertedCohortDefinitions = converterUtils.convertList(new ArrayList<>(source.getCohorts()), CohortDefinition.class);
      cohortCharacterization.setCohortDefinitions(convertedCohortDefinitions);
    }

    final Set<FeAnalysisEntity> convertedFeatureAnalyses = source.getFeatureAnalyses().stream().map(fa -> conversionService.convert(fa, FeAnalysisEntity.class)).collect(Collectors.toSet());
    cohortCharacterization.setFeatureAnalyses(convertedFeatureAnalyses);

    final Set<CcParamEntity> convertedParameters = source.getParameters().stream().map(this::convertParameter).collect(Collectors.toSet());
    cohortCharacterization.setParameters(convertedParameters);

    return cohortCharacterization;
  }

  protected CcParamEntity convertParameter(final CcParameterDTO dto) {
    return conversionService.convert(dto, CcParamEntity.class);
  }


}
