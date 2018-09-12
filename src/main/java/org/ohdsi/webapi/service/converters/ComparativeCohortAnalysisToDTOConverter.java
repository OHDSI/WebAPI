package org.ohdsi.webapi.service.converters;

import org.ohdsi.webapi.cohortcomparison.ComparativeCohortAnalysis;
import org.ohdsi.webapi.service.dto.ComparativeCohortAnalysisDTO;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.stereotype.Component;


@Component
public class ComparativeCohortAnalysisToDTOConverter extends BaseComparativeCohortAnalysisToDTOConverter<ComparativeCohortAnalysis, ComparativeCohortAnalysisDTO> {

  public ComparativeCohortAnalysisToDTOConverter(GenericConversionService conversionService) {

    conversionService.addConverter(this);
  }

  @Override
  protected ComparativeCohortAnalysisDTO newTarget() {
    return new ComparativeCohortAnalysisDTO();
  }

}
