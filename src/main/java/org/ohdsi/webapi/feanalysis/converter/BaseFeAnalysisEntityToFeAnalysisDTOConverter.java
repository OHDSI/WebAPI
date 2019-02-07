package org.ohdsi.webapi.feanalysis.converter;

import org.ohdsi.webapi.converter.BaseConversionServiceAwareConverter;
import org.ohdsi.webapi.feanalysis.domain.FeAnalysisEntity;
import org.ohdsi.webapi.feanalysis.dto.FeAnalysisShortDTO;

import static org.ohdsi.webapi.util.ConversionUtils.convertMetadata;

public abstract class BaseFeAnalysisEntityToFeAnalysisDTOConverter<T extends FeAnalysisShortDTO> extends BaseConversionServiceAwareConverter<FeAnalysisEntity, T> {

  @Override
  public T convert(FeAnalysisEntity source) {
    T dto = createResultObject(source);
    dto.setType(source.getType());
    dto.setName(source.getName());
    dto.setId(source.getId());
    dto.setDomain(source.getDomain());
    dto.setDescription(source.getDescr());
    dto.setStatType(source.getStatType());
    convertMetadata(source, dto);

    return dto;
  }
}
