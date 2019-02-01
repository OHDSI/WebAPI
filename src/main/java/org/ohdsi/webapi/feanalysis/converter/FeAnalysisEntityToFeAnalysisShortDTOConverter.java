package org.ohdsi.webapi.feanalysis.converter;

import org.ohdsi.webapi.feanalysis.dto.FeAnalysisShortDTO;
import org.springframework.stereotype.Component;

@Component
public class FeAnalysisEntityToFeAnalysisShortDTOConverter extends BaseFeAnalysisEntityToFeAnalysisDTOConverter<FeAnalysisShortDTO> {

  @Override
  protected FeAnalysisShortDTO createResultObject() {
    return new FeAnalysisShortDTO();
  }
}
