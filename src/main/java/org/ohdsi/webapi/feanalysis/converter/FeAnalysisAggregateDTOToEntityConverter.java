package org.ohdsi.webapi.feanalysis.converter;

import com.odysseusinc.arachne.commons.converter.BaseConvertionServiceAwareConverter;
import org.ohdsi.webapi.feanalysis.domain.FeAnalysisAggregateEntity;
import org.ohdsi.webapi.feanalysis.dto.FeAnalysisAggregateDTO;
import org.ohdsi.webapi.feanalysis.repository.FeAnalysisAggregateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class FeAnalysisAggregateDTOToEntityConverter extends BaseConvertionServiceAwareConverter<FeAnalysisAggregateDTO, FeAnalysisAggregateEntity> {

  @Autowired
  private FeAnalysisAggregateRepository aggregateRepository;

  @Override
  protected FeAnalysisAggregateEntity createResultObject(FeAnalysisAggregateDTO feAnalysisAggregateDTO) {

    return new FeAnalysisAggregateEntity();
  }

  @Override
  public FeAnalysisAggregateEntity convert(FeAnalysisAggregateDTO dto) {

    if (Objects.nonNull(dto.getId())) {
      return aggregateRepository.getOne(dto.getId());
    } else {
      return aggregateRepository.findDefault().orElse(null);
    }
  }

  @Override
  protected void convert(FeAnalysisAggregateDTO dto, FeAnalysisAggregateEntity entity) {
  }
}
