package org.ohdsi.webapi.cohortcharacterization.converter;

import com.odysseusinc.arachne.commons.utils.ConverterUtils;
import org.apache.commons.collections.CollectionUtils;
import org.ohdsi.analysis.CohortMetadata;
import org.ohdsi.analysis.Utils;
import org.ohdsi.analysis.cohortcharacterization.design.StandardFeatureAnalysisType;
import org.ohdsi.webapi.cohortcharacterization.domain.CcConceptSetEntity;
import org.ohdsi.webapi.cohortcharacterization.domain.CcParamEntity;
import org.ohdsi.webapi.cohortcharacterization.domain.CcStrataEntity;
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
    cohortCharacterization.setStratifiedBy(source.getStratifiedBy());

    cohortCharacterization.setId(source.getId());

    final Set<CohortDefinition> convertedCohorts = converterUtils.convertSet(source.getCohorts(), CohortDefinition.class);
    cohortCharacterization.setCohortDefinitions(convertedCohorts);

    final Set<FeAnalysisEntity> convertedFeatureAnalyses = converterUtils.convertSet(source.getFeatureAnalyses(), FeAnalysisEntity.class);
    cohortCharacterization.setFeatureAnalyses(convertedFeatureAnalyses);

    final Set<CcParamEntity> convertedParameters = converterUtils.convertSet(source.getParameters(), CcParamEntity.class);
    cohortCharacterization.setParameters(convertedParameters);

    final Set<CcStrataEntity> convertedStratas = converterUtils.convertSet(source.getStratas(), CcStrataEntity.class);
    cohortCharacterization.setStratas(convertedStratas);

    CcConceptSetEntity conceptSetEntity = new CcConceptSetEntity();
    conceptSetEntity.setCohortCharacterization(cohortCharacterization);
    conceptSetEntity.setRawExpression(Utils.serialize(source.getConceptSets()));
    cohortCharacterization.setConceptSetEntity(conceptSetEntity);

    return cohortCharacterization;
  }


}
