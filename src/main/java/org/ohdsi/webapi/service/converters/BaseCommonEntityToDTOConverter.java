package org.ohdsi.webapi.service.converters;

import org.ohdsi.webapi.model.CommonEntity;
import org.ohdsi.webapi.service.dto.CommonEntityDTO;
import org.ohdsi.webapi.util.UserUtils;
import org.springframework.core.convert.converter.Converter;

public abstract class BaseCommonEntityToDTOConverter<S extends CommonEntity, T extends CommonEntityDTO> implements Converter<S, T> {

  protected abstract T newTarget();

  protected abstract void doConvert(T target, S source);

  @Override
  public final T convert(S s) {

    T target = newTarget();
    target.setCreatedBy(UserUtils.nullSafeLogin(s.getCreatedBy()));
    target.setCreatedDate(s.getCreatedDate());
    target.setModifiedBy(UserUtils.nullSafeLogin(s.getModifiedBy()));
    target.setModifiedDate(s.getModifiedDate());
    doConvert(target, s);
    return target;
  }
}
