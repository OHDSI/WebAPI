package org.ohdsi.webapi.feanalysis.converter;

import org.ohdsi.webapi.feanalysis.domain.FeAnalysisWithStringEntity;
import org.ohdsi.webapi.feanalysis.dto.FeAnalysisShortDTO;
import org.springframework.stereotype.Component;

@Component
public class FeAnalysisShortDTOToFeAnalysisWithStringConverter extends BaseFeAnalysisShortDTOToFeAnalysisConverter<FeAnalysisShortDTO, FeAnalysisWithStringEntity> {
  @Override
  protected FeAnalysisWithStringEntity createResultObject() {
    return new FeAnalysisWithStringEntity();
  }
}
