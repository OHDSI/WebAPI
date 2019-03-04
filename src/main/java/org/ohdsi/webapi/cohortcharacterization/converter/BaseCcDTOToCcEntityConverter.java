package org.ohdsi.webapi.cohortcharacterization.converter;

import com.odysseusinc.arachne.commons.utils.ConverterUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.ohdsi.analysis.CohortMetadata;
import org.ohdsi.analysis.Utils;
import org.ohdsi.webapi.cohortcharacterization.domain.CcStrataConceptSetEntity;
import org.ohdsi.webapi.cohortcharacterization.domain.CcParamEntity;
import org.ohdsi.webapi.cohortcharacterization.domain.CcStrataEntity;
import org.ohdsi.webapi.cohortcharacterization.domain.CohortCharacterizationEntity;
import org.ohdsi.webapi.cohortcharacterization.dto.BaseCcDTO;
import org.ohdsi.webapi.cohortdefinition.CohortDefinition;
import org.ohdsi.webapi.converter.BaseConversionServiceAwareConverter;
import org.ohdsi.webapi.feanalysis.domain.FeAnalysisEntity;
import org.ohdsi.webapi.feanalysis.dto.FeAnalysisShortDTO;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;
import java.util.TreeSet;

public abstract class BaseCcDTOToCcEntityConverter<T extends BaseCcDTO<? extends CohortMetadata, ? extends FeAnalysisShortDTO>>
        extends BaseConversionServiceAwareConverter<T, CohortCharacterizationEntity> {

  @Autowired
  private ConverterUtils converterUtils;

  @Override
  public CohortCharacterizationEntity convert(T source) {

    final CohortCharacterizationEntity cohortCharacterization = new CohortCharacterizationEntity();

    cohortCharacterization.setName(source.getName());
    cohortCharacterization.setStratifiedBy(source.getStratifiedBy());
    cohortCharacterization.setStrataOnly(source.getStrataOnly());

    cohortCharacterization.setId(source.getId());

    Set<CohortDefinition> convertedCohorts = new TreeSet<>((o1, o2) -> ObjectUtils.compare(o1.getId(), o2.getId()));
    convertedCohorts.addAll(converterUtils.convertSet(source.getCohorts(), CohortDefinition.class));
    cohortCharacterization.setCohortDefinitions(convertedCohorts);

    Set<FeAnalysisEntity> convertedFeatureAnalyses = new TreeSet<>((o1, o2) -> ObjectUtils.compare(o1.getId(), o2.getId()));
    convertedFeatureAnalyses.addAll(converterUtils.convertSet(source.getFeatureAnalyses(), FeAnalysisEntity.class));
    cohortCharacterization.setFeatureAnalyses(convertedFeatureAnalyses);

    Set<CcParamEntity> convertedParameters = new TreeSet<>((o1, o2) -> ObjectUtils.compare(o1.getName(), o2.getName()));
    convertedParameters.addAll(converterUtils.convertSet(source.getParameters(), CcParamEntity.class));
    cohortCharacterization.setParameters(convertedParameters);

    Set<CcStrataEntity> convertedStratas = new TreeSet<>((o1, o2) -> ObjectUtils.compare(
            o1.getName() + o1.getId() + o1.getExpressionString(), o2.getName() + o2.getId() + o2.getExpressionString()));
    convertedStratas.addAll(converterUtils.convertSet(source.getStratas(), CcStrataEntity.class));
    cohortCharacterization.setStratas(convertedStratas);

    CcStrataConceptSetEntity conceptSetEntity = new CcStrataConceptSetEntity();
    conceptSetEntity.setCohortCharacterization(cohortCharacterization);
    conceptSetEntity.setRawExpression(Utils.serialize(source.getStrataConceptSets()));
    cohortCharacterization.setConceptSetEntity(conceptSetEntity);

    return cohortCharacterization;
  }


}
