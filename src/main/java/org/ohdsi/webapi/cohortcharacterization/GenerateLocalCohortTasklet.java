package org.ohdsi.webapi.cohortcharacterization;

import com.google.common.collect.ImmutableMap;
import org.ohdsi.webapi.cohortdefinition.CohortDefinition;
import org.ohdsi.webapi.job.JobExecutionResource;
import org.ohdsi.webapi.service.CohortGenerationService;
import org.ohdsi.webapi.service.JobService;
import org.ohdsi.webapi.service.SourceService;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.util.SourceUtils;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.StoppableTasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.ohdsi.webapi.Constants.Params.*;

public class GenerateLocalCohortTasklet implements StoppableTasklet {
  String GENERATE_LOCAL_COHORT = "generateLocalCohort";

  protected TransactionTemplate transactionTemplate;
  protected final CohortGenerationService cohortGenerationService;
  private final JobService jobService;
  protected final Function<ChunkContext, Collection<CohortDefinition>> cohortGetter;

  protected final SourceService sourceService;
  private long checkInterval = 3000L;
  private boolean stopped = false;

  public GenerateLocalCohortTasklet(TransactionTemplate transactionTemplate,
                                    CohortGenerationService cohortGenerationService,
                                    SourceService sourceService,
                                    JobService jobService,
                                    Function<ChunkContext, Collection<CohortDefinition>> cohortGetter) {
    this.transactionTemplate = transactionTemplate;

    this.cohortGenerationService = cohortGenerationService;
    this.sourceService = sourceService;
    this.cohortGetter = cohortGetter;
    this.jobService = jobService;
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

    new GenerateTask(chunkContext).run(cohortGetter.apply(chunkContext));
    return null;
  }

  class GenerateTask {

    private final String targetTable;
    private final Source source;
    private final String jobAuthorLogin;

    public GenerateTask(ChunkContext chunkContext) {
      Map<String, Object> jobParameters = chunkContext.getStepContext().getJobParameters();
      source = sourceService.findBySourceId(Integer.valueOf(jobParameters.get(SOURCE_ID).toString()));
      targetTable = jobParameters.get(TARGET_TABLE).toString();
      jobAuthorLogin = jobParameters.get(JOB_AUTHOR).toString();
    }

    private JobExecutionResource generateCohort(CohortDefinition cd) {

      Map<String, String> extraParams = ImmutableMap.<String, String>builder()
        .put(JOB_AUTHOR, jobAuthorLogin)
        .put(GENERATE_STATS, Boolean.FALSE.toString())
        .put(TARGET_DATABASE_SCHEMA, SourceUtils.getTempQualifier(source))
        .build();
      return cohortGenerationService.runGenerateCohortJob(cd, source, false, false, targetTable, extraParams, GENERATE_LOCAL_COHORT);
    }

    public void run(Collection<CohortDefinition> cohortDefinitions) {
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
          List<JobExecution> executions = executionIds.stream().map(jobService::getJobExecution)
                  .filter(Objects::nonNull)
                  .collect(Collectors.toList());
          if (stopped) {
            executions.forEach(jobExecution -> {
              Job job = jobService.getRunningJob(jobExecution.getJobId());
              if (Objects.nonNull(job)) {
                jobService.stopJob(jobExecution, job);
              }
            });
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
