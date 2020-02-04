package org.ohdsi.webapi.feanalysis.converter;

import com.odysseusinc.arachne.commons.converter.BaseConvertionServiceAwareConverter;
import org.ohdsi.webapi.feanalysis.domain.FeAnalysisAggregateEntity;
import org.ohdsi.webapi.feanalysis.dto.FeAnalysisAggregateDTO;
import org.springframework.stereotype.Component;

@Component
public class FeAnalysisAggregateDTOToEntityConverter extends BaseConvertionServiceAwareConverter<FeAnalysisAggregateDTO, FeAnalysisAggregateEntity> {

  @Override
  protected FeAnalysisAggregateEntity createResultObject(FeAnalysisAggregateDTO feAnalysisAggregateDTO) {

    return new FeAnalysisAggregateEntity();
  }

  @Override
  protected void convert(FeAnalysisAggregateDTO dto, FeAnalysisAggregateEntity entity) {

    entity.setDomain(dto.getDomain());
    entity.setExpression(dto.getExpression());
    entity.setFunction(dto.getFunction());
    entity.setId(dto.getId());
    entity.setName(dto.getName());
    entity.setQuery(dto.getQuery());
  }
}
