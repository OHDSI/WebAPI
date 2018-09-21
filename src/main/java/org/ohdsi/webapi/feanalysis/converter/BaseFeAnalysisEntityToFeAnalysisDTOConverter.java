package org.ohdsi.webapi.feanalysis.converter;

import org.ohdsi.webapi.converter.BaseConversionServiceAwareConverter;
import org.ohdsi.webapi.feanalysis.domain.FeAnalysisEntity;
import org.ohdsi.webapi.feanalysis.dto.FeAnalysisShortDTO;

public abstract class BaseFeAnalysisEntityToFeAnalysisDTOConverter<T extends FeAnalysisShortDTO> extends BaseConversionServiceAwareConverter<FeAnalysisEntity, T> {

  @Override
  public T convert(FeAnalysisEntity source) {
    T dto = getReturnObject();
    dto.setType(source.getType());
    dto.setName(source.getName());
    dto.setId(source.getId());
    dto.setDomain(source.getDomain());
    dto.setDescription(source.getDescr());

    return dto;
  }

  protected abstract T getReturnObject();
}
