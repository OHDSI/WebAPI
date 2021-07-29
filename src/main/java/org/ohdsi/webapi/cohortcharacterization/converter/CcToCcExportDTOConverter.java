package org.ohdsi.webapi.cohortcharacterization.converter;

import org.ohdsi.webapi.cohortcharacterization.dto.CcExportDTO;
import org.springframework.stereotype.Component;

@Component
public class CcToCcExportDTOConverter extends BaseCcToCcExportDTOConverter<CcExportDTO> {

  @Override
  protected CcExportDTO createResultObject() {
    return new CcExportDTO();
  }
}
