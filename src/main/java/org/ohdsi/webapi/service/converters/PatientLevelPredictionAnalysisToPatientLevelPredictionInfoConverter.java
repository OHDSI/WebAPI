package org.ohdsi.webapi.service.converters;

import org.ohdsi.webapi.prediction.PatientLevelPredictionAnalysis;
import org.ohdsi.webapi.prediction.PatientLevelPredictionAnalysisInfo;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.stereotype.Component;

@Component
public class PatientLevelPredictionAnalysisToPatientLevelPredictionInfoConverter extends BasePLPAnalysisToDTOConverter<PatientLevelPredictionAnalysis, PatientLevelPredictionAnalysisInfo> {

  public PatientLevelPredictionAnalysisToPatientLevelPredictionInfoConverter(GenericConversionService conversionService) {

    conversionService.addConverter(this);
  }

  @Override
  protected PatientLevelPredictionAnalysisInfo newTarget() {

    return new PatientLevelPredictionAnalysisInfo();
  }
}
