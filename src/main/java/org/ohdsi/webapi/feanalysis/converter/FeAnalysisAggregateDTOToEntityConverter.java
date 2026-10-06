package org.ohdsi.webapi.feanalysis.converter;

import org.ohdsi.webapi.converter.BaseConversionServiceAwareConverter;
import org.ohdsi.webapi.feanalysis.domain.FeAnalysisAggregateEntity;
import org.ohdsi.webapi.feanalysis.dto.FeAnalysisAggregateDTO;
import org.ohdsi.webapi.feanalysis.repository.FeAnalysisAggregateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class FeAnalysisAggregateDTOToEntityConverter extends BaseConversionServiceAwareConverter<FeAnalysisAggregateDTO, FeAnalysisAggregateEntity> {

  @Autowired
  private FeAnalysisAggregateRepository aggregateRepository;

  @Override
  protected FeAnalysisAggregateEntity createResultObject(FeAnalysisAggregateDTO feAnalysisAggregateDTO) {

    return new FeAnalysisAggregateEntity();
  }

  @Override
  public FeAnalysisAggregateEntity convert(FeAnalysisAggregateDTO dto) {

    // Fetch eagerly rather than getOne()/getReference(): the resulting entity may be read back out
    // (e.g. converted to a response DTO) after the request's transaction/session has already closed,
    // and an uninitialized proxy would throw LazyInitializationException at that point.
    if (Objects.nonNull(dto.getId())) {
      return aggregateRepository.findById(dto.getId())
              .orElseThrow(() -> new IllegalArgumentException(String.format("There is no feature analysis aggregate with id = %d.", dto.getId())));
    } else {
      return aggregateRepository.findDefault().orElse(null);
    }
  }

}
