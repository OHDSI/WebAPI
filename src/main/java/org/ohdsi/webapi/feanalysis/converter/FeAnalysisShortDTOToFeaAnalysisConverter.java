package org.ohdsi.webapi.feanalysis.converter;

import org.ohdsi.webapi.feanalysis.domain.FeAnalysisEntity;
import org.ohdsi.webapi.feanalysis.dto.FeAnalysisShortDTO;
import org.springframework.stereotype.Component;

@Component
public class FeAnalysisShortDTOToFeaAnalysisConverter extends BaseFeAnalysisShortDTOToFeAnalysisConverter<FeAnalysisShortDTO, FeAnalysisEntity> {

  @Override
  protected FeAnalysisEntity createResultObject() {
    return new FeAnalysisEntity();
  }
}
