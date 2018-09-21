package org.ohdsi.webapi.cohortcharacterization.converter;

import org.ohdsi.webapi.cohortcharacterization.domain.CohortCharacterizationEntity;
import org.ohdsi.webapi.cohortcharacterization.dto.CcExportDTO;
import org.ohdsi.webapi.cohortdefinition.dto.CohortDTO;
import org.ohdsi.webapi.feanalysis.dto.FeAnalysisDTO;
import org.springframework.stereotype.Component;

@Component
public class CcToCcExportDTOConverter extends BaseCcToCcDTOConverter<CcExportDTO> {

  @Override
  public CcExportDTO convert(CohortCharacterizationEntity source) {

    final CcExportDTO exportDTO = super.convert(source);

    exportDTO.setCohorts(converterUtils.convertList(source.getCohortDefinitions(), CohortDTO.class));
    exportDTO.setFeatureAnalyses(converterUtils.convertList(toList(source.getFeatureAnalyses()), FeAnalysisDTO.class));
    return exportDTO;
  }

  @Override
  protected CcExportDTO createResultObject() {
    return new CcExportDTO();
  }
}
