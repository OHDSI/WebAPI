package org.ohdsi.webapi.service.converters;

import org.ohdsi.webapi.prediction.PatientLevelPredictionAnalysis;
import org.ohdsi.webapi.service.dto.PatientLevelPredictionAnalysisDTO;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.stereotype.Component;

@Component
public class PatientLevelPredictionAnalysisToPatientLevelPredictionAnalysisDTOConverter extends BasePLPAnalysisToDTOConverter<PatientLevelPredictionAnalysis, PatientLevelPredictionAnalysisDTO> {

  public PatientLevelPredictionAnalysisToPatientLevelPredictionAnalysisDTOConverter(GenericConversionService conversionService) {

    conversionService.addConverter(this);
  }

  @Override
  protected PatientLevelPredictionAnalysisDTO newTarget() {
    return new PatientLevelPredictionAnalysisDTO();
  }

}
