package org.ohdsi.webapi.cohortcharacterization.converter;

import org.apache.commons.lang3.ObjectUtils;
import org.ohdsi.webapi.cohortcharacterization.domain.CohortCharacterizationEntity;
import org.ohdsi.webapi.cohortcharacterization.dto.CcExportDTO;
import org.ohdsi.webapi.cohortdefinition.dto.CohortDTO;
import org.ohdsi.webapi.feanalysis.dto.FeAnalysisDTO;

import java.util.Set;
import java.util.TreeSet;

public abstract class BaseCcToCcExportDTOConverter<T extends CcExportDTO> extends BaseCcToCcDTOConverter<T> {
  @Override
  public T convert(CohortCharacterizationEntity source) {

    final T exportDTO = super.convert(source);

    Set<CohortDTO> convertedCohortDTOs = new TreeSet<>((o1, o2) -> ObjectUtils.compare(o1.getId(), o2.getId()));
    convertedCohortDTOs.addAll(converterUtils.convertSet(source.getCohortDefinitions(), CohortDTO.class));
    exportDTO.setCohorts(convertedCohortDTOs);

    Set<FeAnalysisDTO> convertedFeAnalysisDTOs = new TreeSet<>((o1, o2) -> ObjectUtils.compare(o1.getId(), o2.getId()));
    convertedFeAnalysisDTOs.addAll(converterUtils.convertSet(source.getFeatureAnalyses(), FeAnalysisDTO.class));
    exportDTO.setFeatureAnalyses(convertedFeAnalysisDTOs);
    return exportDTO;
  }
}
