package org.ohdsi.webapi.prediction;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.ohdsi.webapi.job.JobExecutionResource;
import org.ohdsi.webapi.prediction.domain.PredictionGenerationEntity;
import org.ohdsi.webapi.prediction.specification.PatientLevelPredictionAnalysisImpl;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public interface PredictionService {

  Iterable<PredictionAnalysis> getAnalysisList();

  void delete(int id);

  PredictionAnalysis createAnalysis(PredictionAnalysis pred);

  PredictionAnalysis updateAnalysis(int id, PredictionAnalysis pred);

  PredictionAnalysis copy(int id);

  PredictionAnalysis getAnalysis(int id);

  PatientLevelPredictionAnalysisImpl exportAnalysis(int id, String sourceKey);

  PatientLevelPredictionAnalysisImpl exportAnalysis(int id);
  
  PredictionAnalysis importAnalysis(PatientLevelPredictionAnalysisImpl analysis) throws Exception;

  String getNameForCopy(String dtoName);

  void hydrateAnalysis(PatientLevelPredictionAnalysisImpl analysis, String packageName, OutputStream out) throws JsonProcessingException;

  JobExecutionResource runGeneration(PredictionAnalysis predictionAnalysis, String sourceKey) throws IOException;

  PredictionGenerationEntity getGeneration(Long generationId);

  List<PredictionGenerationEntity> getPredictionGenerations(Integer predictionAnalysisId);
  
  PredictionAnalysis getById(Integer id);

  int getCountPredictionWithSameName(Integer id, String name);
}
