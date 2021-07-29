package org.ohdsi.webapi.cohortcharacterization.converter;

import org.ohdsi.webapi.cohortcharacterization.specification.CohortCharacterizationImpl;
import org.springframework.stereotype.Component;

@Component
public class CcToCohortCharacterizationImplConverter extends BaseCcToCcExportDTOConverter<CohortCharacterizationImpl> {
  @Override
  protected CohortCharacterizationImpl createResultObject() {
    return new CohortCharacterizationImpl();
  }
}
