package org.ohdsi.webapi.prediction;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.ohdsi.webapi.prediction.domain.PredictionGenerationEntity;
import org.ohdsi.webapi.prediction.specification.PatientLevelPredictionAnalysis;

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

  PatientLevelPredictionAnalysis exportAnalysis(int id);

  void hydrateAnalysis(PatientLevelPredictionAnalysis plpa, OutputStream out) throws JsonProcessingException;

  void runGeneration(PredictionAnalysis predictionAnalysis, String sourceKey) throws IOException;

  PredictionGenerationEntity getGeneration(Long generationId);

  List<PredictionGenerationEntity> getPredictionGenerations(Integer predictionAnalysisId);
}
