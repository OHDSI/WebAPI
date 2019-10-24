package org.ohdsi.webapi.service;

import org.ohdsi.webapi.GenerationStatus;
import org.ohdsi.webapi.cohortdefinition.*;
import org.ohdsi.webapi.cohortfeatures.GenerateCohortFeaturesTasklet;
import org.ohdsi.webapi.generationcache.GenerationCacheHelper;
import org.ohdsi.webapi.job.GeneratesNotification;
import org.ohdsi.webapi.job.JobExecutionResource;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceService;
import org.ohdsi.webapi.util.SessionUtils;
import org.ohdsi.webapi.util.SourceUtils;
import org.ohdsi.webapi.util.TempTableCleanupManager;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.builder.SimpleJobBuilder;
import org.springframework.batch.repeat.exception.ExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

import static org.ohdsi.webapi.Constants.GENERATE_COHORT;
import static org.ohdsi.webapi.Constants.Params.*;

@Component
@DependsOn("flyway")
public class CohortGenerationService extends AbstractDaoService implements GeneratesNotification {

  private final CohortDefinitionRepository cohortDefinitionRepository;
  private final CohortGenerationInfoRepository cohortGenerationInfoRepository;
  private final JobBuilderFactory jobBuilders;
  private final StepBuilderFactory stepBuilders;
  private final JobService jobService;
  private final SourceService sourceService;
  private final GenerationCacheHelper generationCacheHelper;

  @Autowired
  public CohortGenerationService(CohortDefinitionRepository cohortDefinitionRepository,
                                 CohortGenerationInfoRepository cohortGenerationInfoRepository,
                                 JobBuilderFactory jobBuilders,
                                 StepBuilderFactory stepBuilders,
                                 JobService jobService,
                                 SourceService sourceService,
                                 GenerationCacheHelper generationCacheHelper) {
    this.cohortDefinitionRepository = cohortDefinitionRepository;
    this.cohortGenerationInfoRepository = cohortGenerationInfoRepository;
    this.jobBuilders = jobBuilders;
    this.stepBuilders = stepBuilders;
    this.jobService = jobService;
    this.sourceService = sourceService;
    this.generationCacheHelper = generationCacheHelper;
  }

  public JobExecutionResource generateCohortViaJob(CohortDefinition cohortDefinition, Source source, boolean includeFeatures) {

    CohortGenerationInfo info = cohortDefinition.getGenerationInfoList().stream()
            .filter(val -> Objects.equals(val.getId().getSourceId(), source.getSourceId())).findFirst()
            .orElse(new CohortGenerationInfo(cohortDefinition, source.getSourceId()));
    cohortDefinition.getGenerationInfoList().add(info);

    info.setStatus(GenerationStatus.PENDING)
            .setStartTime(Calendar.getInstance().getTime());

    info.setIncludeFeatures(includeFeatures);

    cohortDefinitionRepository.save(cohortDefinition);

    return runGenerateCohortJob(cohortDefinition, source, includeFeatures);
  }

  private Job buildGenerateCohortJob(CohortDefinition cohortDefinition, Source source, boolean includeFeatures, JobParameters jobParameters) {

    log.info("Beginning generate cohort for cohort definition id: {}", cohortDefinition.getId());

    GenerateCohortTasklet generateTasklet = new GenerateCohortTasklet(
      getSourceJdbcTemplate(source),
      getTransactionTemplate(),
      generationCacheHelper,
      cohortDefinitionRepository,
      sourceService
    );

    ExceptionHandler exceptionHandler = new GenerationTaskExceptionHandler(new TempTableCleanupManager(getSourceJdbcTemplate(source),
            getTransactionTemplate(),
            source.getSourceDialect(),
            jobParameters.getString(SESSION_ID),
            SourceUtils.getTempQualifierOrNull(source)
    ));

    Step generateCohortStep = stepBuilders.get("cohortDefinition.generateCohort")
            .tasklet(generateTasklet)
            .exceptionHandler(exceptionHandler)
            .build();

    SimpleJobBuilder generateJobBuilder = jobBuilders.get(GENERATE_COHORT).start(generateCohortStep);

    generateJobBuilder.listener(new GenerationJobExecutionListener(sourceService, cohortDefinitionRepository, this.getTransactionTemplateRequiresNew(),
            this.getSourceJdbcTemplate(source)));

    if (includeFeatures) {
      GenerateCohortFeaturesTasklet generateCohortFeaturesTasklet =
              new GenerateCohortFeaturesTasklet(getSourceJdbcTemplate(source), getTransactionTemplate());

      Step generateCohortFeaturesStep = stepBuilders.get("cohortFeatures.generateFeatures")
              .tasklet(generateCohortFeaturesTasklet)
              .exceptionHandler(exceptionHandler)
              .build();

      generateJobBuilder.next(generateCohortFeaturesStep);
    }

    return generateJobBuilder.build();
  }

  private JobExecutionResource runGenerateCohortJob(CohortDefinition cohortDefinition, Source source, boolean includeFeatures) {
    final JobParametersBuilder jobParametersBuilder = getJobParametersBuilder(source, cohortDefinition);
    Job job = buildGenerateCohortJob(cohortDefinition, source, includeFeatures, jobParametersBuilder.toJobParameters());
    return jobService.runJob(job, jobParametersBuilder.toJobParameters());
  }

  private JobParametersBuilder getJobParametersBuilder(Source source, CohortDefinition cohortDefinition) {

    JobParametersBuilder builder = new JobParametersBuilder();
    builder.addString(JOB_NAME, String.format("Generating cohort %d : %s (%s)", cohortDefinition.getId(), source.getSourceName(), source.getSourceKey()));
    builder.addString(TARGET_DATABASE_SCHEMA, SourceUtils.getResultsQualifier(source));
    builder.addString(SESSION_ID, SessionUtils.sessionId());
    builder.addString(COHORT_DEFINITION_ID, String.valueOf(cohortDefinition.getId()));
    builder.addString(SOURCE_ID, String.valueOf(source.getSourceId()));
    builder.addString(GENERATE_STATS, Boolean.TRUE.toString());
    return builder;
  }

  @PostConstruct
  public void init(){

    invalidateCohortGenerations();
  }

  private void invalidateCohortGenerations() {

    getTransactionTemplateRequiresNew().execute(status -> {
      List<CohortGenerationInfo> executions = cohortGenerationInfoRepository.findByStatusIn(INVALIDATE_STATUSES);
      invalidateExecutions(executions);
      cohortGenerationInfoRepository.save(executions);
      return null;
    });
  }

  @Override
  public String getJobName() {
    return GENERATE_COHORT;
  }

  @Override
  public String getExecutionFoldingKey() {
    return COHORT_DEFINITION_ID;
  }
}
