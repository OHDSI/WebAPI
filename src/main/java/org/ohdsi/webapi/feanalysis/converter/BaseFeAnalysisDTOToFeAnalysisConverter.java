package org.ohdsi.webapi.feanalysis.converter;

import org.ohdsi.webapi.converter.BaseConversionServiceAwareConverter;
import org.ohdsi.webapi.feanalysis.domain.FeAnalysisEntity;
import org.ohdsi.webapi.feanalysis.dto.FeAnalysisShortDTO;

public abstract class BaseFeAnalysisDTOToFeAnalysisConverter<D extends FeAnalysisShortDTO, T extends FeAnalysisEntity>
        extends BaseConversionServiceAwareConverter<D, T> {

  @Override
  public T convert(D source) {
    final T result = createResultObject(source);

    result.setId(source.getId());
    result.setDescr(source.getDescription());
    result.setDomain(source.getDomain());
    result.setName(source.getName());
    result.setType(source.getType());
    result.setStatType(source.getStatType());

    return result;
  }
}
