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
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.ohdsi.webapi.Constants.Params.*;

public abstract class BaseGenerateLocalCohortTasklet implements StoppableTasklet {

  protected TransactionTemplate transactionTemplate;
  protected final CohortGenerationService cohortGenerationService;

  protected final SourceRepository sourceRepository;
  private long checkInterval = 3000L;
  private boolean stopped = false;

  public BaseGenerateLocalCohortTasklet(TransactionTemplate transactionTemplate,
                                        CohortGenerationService cohortGenerationService,
                                        SourceRepository sourceRepository) {
    this.transactionTemplate = transactionTemplate;

    this.cohortGenerationService = cohortGenerationService;
    this.sourceRepository = sourceRepository;
  }

  @Override
  public void stop() {
    this.stopped = true;
  }

  @Override
  public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {

    transactionTemplate.execute(transactionStatus -> doTask(chunkContext));
    return RepeatStatus.FINISHED;
  }

  private Object doTask(final ChunkContext chunkContext) {

    new GenerateTask(chunkContext).run(getCohortDefinitions(chunkContext));
    return null;
  }

  protected abstract List<CohortDefinition> getCohortDefinitions(ChunkContext chunkContext);

  class GenerateTask {

    private String targetTable;
    private Source source;

    public GenerateTask(ChunkContext chunkContext) {
      Map<String, Object> jobParameters = chunkContext.getStepContext().getJobParameters();
      targetTable = jobParameters.get(TARGET_TABLE).toString();
      source = sourceRepository.findBySourceId(Integer.valueOf(jobParameters.get(SOURCE_ID).toString()));
    }

    private JobExecutionResource generateCohort(CohortDefinition cd) {

      return cohortGenerationService.runGenerateCohortJob(cd, source, false, false, targetTable);
    }

    public void run(List<CohortDefinition> cohortDefinitions) {
      List<Long> executionIds = cohortDefinitions.stream()
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
