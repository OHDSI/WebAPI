package org.ohdsi.webapi.cohortcharacterization.converter;

import org.ohdsi.webapi.cohortcharacterization.domain.CohortCharacterizationEntity;
import org.ohdsi.webapi.cohortcharacterization.dto.CcExportDTO;
import org.ohdsi.webapi.cohortdefinition.dto.CohortDTO;
import org.ohdsi.webapi.feanalysis.dto.FeAnalysisDTO;
import org.springframework.stereotype.Component;

import java.util.TreeSet;

@Component
public class CcToCcExportDTOConverter extends BaseCcToCcDTOConverter<CcExportDTO> {

  @Override
  public CcExportDTO convert(CohortCharacterizationEntity source) {

    final CcExportDTO exportDTO = super.convert(source);

    exportDTO.setCohorts(new TreeSet<>(converterUtils.convertSet(source.getCohortDefinitions(), CohortDTO.class)));
    exportDTO.setFeatureAnalyses(new TreeSet<>(converterUtils.convertSet(source.getFeatureAnalyses(), FeAnalysisDTO.class)));
    return exportDTO;
  }

  @Override
  protected CcExportDTO createResultObject() {
    return new CcExportDTO();
  }
}
