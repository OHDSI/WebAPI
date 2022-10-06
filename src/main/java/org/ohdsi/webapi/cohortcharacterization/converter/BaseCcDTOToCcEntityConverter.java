package org.ohdsi.webapi.cohortcharacterization.converter;

import com.odysseusinc.arachne.commons.utils.ConverterUtils;
import org.apache.commons.lang3.StringUtils;
import org.ohdsi.analysis.CohortMetadata;
import org.ohdsi.analysis.Utils;
import org.ohdsi.analysis.cohortcharacterization.design.CcResultType;
import org.ohdsi.webapi.cohortcharacterization.domain.CcStrataConceptSetEntity;
import org.ohdsi.webapi.cohortcharacterization.domain.CcParamEntity;
import org.ohdsi.webapi.cohortcharacterization.domain.CcStrataEntity;
import org.ohdsi.webapi.cohortcharacterization.domain.CohortCharacterizationEntity;
import org.ohdsi.webapi.cohortcharacterization.dto.BaseCcDTO;
import org.ohdsi.webapi.cohortdefinition.CohortDefinition;
import org.ohdsi.webapi.cohortdefinition.CohortMetadataExt;
import org.ohdsi.webapi.converter.BaseConversionServiceAwareConverter;
import org.ohdsi.webapi.feanalysis.domain.FeAnalysisEntity;
import org.ohdsi.webapi.feanalysis.dto.FeAnalysisShortDTO;
import org.ohdsi.webapi.tag.domain.Tag;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static org.ohdsi.analysis.cohortcharacterization.design.StandardFeatureAnalysisType.CRITERIA_SET;

public abstract class BaseCcDTOToCcEntityConverter<T extends BaseCcDTO<? extends CohortMetadataExt, ? extends FeAnalysisShortDTO>>
        extends BaseConversionServiceAwareConverter<T, CohortCharacterizationEntity> {

  @Autowired
  private ConverterUtils converterUtils;

  @Override
  public CohortCharacterizationEntity convert(T source) {

    final CohortCharacterizationEntity cohortCharacterization = new CohortCharacterizationEntity();

    cohortCharacterization.setName(StringUtils.trim(source.getName()));
    cohortCharacterization.setDescription(source.getDescription());
    cohortCharacterization.setStratifiedBy(source.getStratifiedBy());
    cohortCharacterization.setStrataOnly(source.getStrataOnly());

    cohortCharacterization.setId(source.getId());

    cohortCharacterization.setCohortDefinitions(converterUtils.convertSet(source.getCohorts(), CohortDefinition.class));

    source.getFeatureAnalyses().forEach(fa -> {
      // Legacy Criteria Analyses didn't have statType, they were always PREVALENCE
      if (Objects.equals(fa.getType(), CRITERIA_SET) && fa.getStatType() == null) {
        fa.setStatType(CcResultType.PREVALENCE);
      }
    });
    cohortCharacterization.setFeatureAnalyses(converterUtils.convertSet(source.getFeatureAnalyses(), FeAnalysisEntity.class));

    cohortCharacterization.setParameters(converterUtils.convertSet(source.getParameters(), CcParamEntity.class));
    cohortCharacterization.setStratas(converterUtils.convertSet(source.getStratas(), CcStrataEntity.class));

    CcStrataConceptSetEntity conceptSetEntity = new CcStrataConceptSetEntity();
    conceptSetEntity.setCohortCharacterization(cohortCharacterization);
    conceptSetEntity.setRawExpression(Utils.serialize(source.getStrataConceptSets()));
    cohortCharacterization.setConceptSetEntity(conceptSetEntity);

    if (Objects.nonNull(source.getTags())) {
      Set<Tag> tags = source.getTags().stream()
              .map(tag -> conversionService.convert(tag, Tag.class)).collect(Collectors.toSet());
      cohortCharacterization.setTags(tags);
    }

    return cohortCharacterization;
  }


}
