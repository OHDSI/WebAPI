package org.ohdsi.webapi.service.converters;

import org.ohdsi.webapi.cohortcomparison.ComparativeCohortAnalysis;
import org.ohdsi.webapi.cohortcomparison.ComparativeCohortAnalysisInfo;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.stereotype.Component;

@Component
public class ComparativeCohortAnalysisToInfoConverter extends BaseComparativeCohortAnalysisToDTOConverter<ComparativeCohortAnalysis, ComparativeCohortAnalysisInfo> {

  public ComparativeCohortAnalysisToInfoConverter(GenericConversionService conversionService) {
    conversionService.addConverter(this);
  }

  @Override
  protected ComparativeCohortAnalysisInfo newTarget() {
    return new ComparativeCohortAnalysisInfo();
  }
}
