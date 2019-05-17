package org.ohdsi.webapi.cohortdefinition.converter;

import org.ohdsi.webapi.cohortdefinition.CohortDefinition;
import org.ohdsi.webapi.cohortdefinition.dto.CohortMetadataDTO;
import org.ohdsi.webapi.converter.BaseConversionServiceAwareConverter;
import org.ohdsi.webapi.util.UserUtils;

public abstract class BaseCohortDefinitionToCohortMetadataDTOConverter<T extends CohortMetadataDTO> extends BaseConversionServiceAwareConverter<CohortDefinition, T> {

  @Override
  public T convert(CohortDefinition def) {

    T target = getResultObject();
    target.setId(def.getId());
    target.setName(def.getName());
    target.setDescription(def.getDescription());
    target.setCreatedBy(UserUtils.nullSafeLogin(def.getCreatedBy()));
    target.setCreatedDate(def.getCreatedDate());
    target.setModifiedBy(UserUtils.nullSafeLogin(def.getModifiedBy()));
    target.setModifiedDate(def.getModifiedDate());
    return target;
  }

  protected abstract T getResultObject();
}
