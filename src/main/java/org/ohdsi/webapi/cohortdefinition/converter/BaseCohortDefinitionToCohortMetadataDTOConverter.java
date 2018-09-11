package org.ohdsi.webapi.cohortdefinition.converter;

import org.ohdsi.webapi.cohortdefinition.CohortDefinition;
import org.ohdsi.webapi.cohortdefinition.dto.CohortMetadataDTO;
import org.ohdsi.webapi.converter.BaseConversionServiceAwareConverter;

public abstract class BaseCohortDefinitionToCohortMetadataDTOConverter<T extends CohortMetadataDTO> extends BaseConversionServiceAwareConverter<CohortDefinition, T> {

  @Override
  public T convert(CohortDefinition def) {

    T target = getResultObject();
    target.setId(def.getId());
    target.setName(def.getName());
    target.setDescription(def.getDescription());
    return target;
  }

  protected abstract T getResultObject();
}
