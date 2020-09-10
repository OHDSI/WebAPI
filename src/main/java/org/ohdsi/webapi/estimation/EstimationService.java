package org.ohdsi.webapi.estimation;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.ohdsi.webapi.estimation.domain.EstimationGenerationEntity;
import org.ohdsi.webapi.estimation.specification.EstimationAnalysisImpl;
import org.ohdsi.webapi.job.JobExecutionResource;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public interface EstimationService {

  Iterable<Estimation> getAnalysisList();
  void delete(final int id);
  Estimation createEstimation(Estimation estimation) throws Exception;
  Estimation updateEstimation(final int id, Estimation est) throws Exception;
  Estimation copy(final int id) throws Exception;
  Estimation getAnalysis(int id);
  EstimationAnalysisImpl getAnalysisExpression(int id);
  EstimationAnalysisImpl exportAnalysis(Estimation est, String sourceKey);
  EstimationAnalysisImpl exportAnalysis(Estimation est);
  Estimation importAnalysis(EstimationAnalysisImpl est) throws Exception;
  String getNameForCopy(String dtoName);
  void hydrateAnalysis(EstimationAnalysisImpl analysis, String packageName, OutputStream out) throws JsonProcessingException;
  JobExecutionResource runGeneration(Estimation estimation, String sourceKey) throws IOException;
  List<EstimationGenerationEntity> getEstimationGenerations(Integer estimationAnalysisId);
  EstimationGenerationEntity getGeneration(Long generationId);
  Estimation getById(Integer id);
  int getCountEstimationWithSameName(Integer id, String name);
}
