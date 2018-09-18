package org.ohdsi.webapi.cohortcharacterization;

import org.ohdsi.webapi.cohortcharacterization.domain.CohortCharacterizationEntity;
import org.ohdsi.webapi.cohortdefinition.CohortDefinition;
import org.ohdsi.webapi.job.JobExecutionResource;
import org.ohdsi.webapi.service.CohortGenerationService;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceRepository;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.StoppableTasklet;
import org.springframework.batch.repeat.RepeatStatus;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.ohdsi.webapi.Constants.Params.*;

public class GenerateLocalCohortTasklet implements StoppableTasklet {

  private final CohortGenerationService cohortGenerationService;

  private final CcService ccService;

  private final SourceRepository sourceRepository;
  private long checkInterval = 3000L;
  private boolean stopped = false;

  public GenerateLocalCohortTasklet(CohortGenerationService cohortGenerationService,
                                    CcService ccService,
                                    SourceRepository sourceRepository) {

    this.cohortGenerationService = cohortGenerationService;
    this.ccService = ccService;
    this.sourceRepository = sourceRepository;
  }

  @Override
  public void stop() {
    this.stopped = true;
  }

  @Override
  public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {

    doTask(chunkContext);
    return RepeatStatus.FINISHED;
  }

  private Object doTask(final ChunkContext chunkContext) {

    new GenerateTask(chunkContext).run();
    return null;
  }

  class GenerateTask implements Runnable {

    private String targetTable;
    private CohortCharacterizationEntity characterization;
    private Source source;

    public GenerateTask(ChunkContext chunkContext) {
      Map<String, Object> jobParameters = chunkContext.getStepContext().getJobParameters();
      targetTable = jobParameters.get(TARGET_TABLE).toString();
      characterization = ccService.findById(Long.valueOf(jobParameters.get(COHORT_CHARACTERIZATION_ID).toString()));
      source = sourceRepository.findBySourceId(Integer.valueOf(jobParameters.get(SOURCE_ID).toString()));
    }

    private JobExecutionResource generateCohort(CohortDefinition cd) {

      return cohortGenerationService.generateCohort(cd, source, false, targetTable);
    }

    @Override
    public void run() {
      List<Long> executionIds = characterization.getCohortDefinitions().stream()
              .map(this::generateCohort)
              .map(JobExecutionResource::getExecutionId)
              .collect(Collectors.toList());
      waitFor(executionIds);
    }

    private void waitFor(List<Long> executionIds) {
      try {
        while (true) {
          Thread.sleep(checkInterval);
          List<JobExecution> executions = executionIds.stream().map(cohortGenerationService::getJobExecution)
                  .filter(Objects::nonNull)
                  .collect(Collectors.toList());
          if (stopped) {
            executions.forEach(JobExecution::stop);
          }
          if (stopped || executions.stream().noneMatch(job -> job.getStatus().isRunning()) || executions.isEmpty()) {
            break;
          }
        }
      }catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }
}
