package org.ohdsi.webapi.service;

import org.ohdsi.webapi.GenerationStatus;
import org.ohdsi.webapi.cohortcharacterization.CreateCohortTableTasklet;
import org.ohdsi.webapi.cohortcharacterization.DropCohortTableListener;
import org.ohdsi.webapi.cohortcharacterization.GenerateLocalCohortTasklet;
import org.ohdsi.webapi.cohortdefinition.CohortDefinition;
import org.ohdsi.webapi.cohortdefinition.CohortDefinitionRepository;
import org.ohdsi.webapi.cohortdefinition.CohortGenerationInfo;
import org.ohdsi.webapi.cohortdefinition.CohortGenerationInfoRepository;
import org.ohdsi.webapi.cohortdefinition.GenerateCohortTasklet;
import org.ohdsi.webapi.cohortdefinition.GenerationJobExecutionListener;
import org.ohdsi.webapi.common.generation.AutoremoveJobListener;
import org.ohdsi.webapi.common.generation.GenerationUtils;
import org.ohdsi.webapi.feanalysis.repository.FeAnalysisEntityRepository;
import org.ohdsi.webapi.generationcache.GenerationCacheHelper;
import org.ohdsi.webapi.job.GeneratesNotification;
import org.ohdsi.webapi.job.JobExecutionResource;
import org.ohdsi.webapi.shiro.Entities.UserEntity;
import org.ohdsi.webapi.shiro.Entities.UserRepository;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceService;
import org.ohdsi.webapi.sqlrender.SourceAwareSqlRender;
import org.ohdsi.webapi.util.SessionUtils;
import org.ohdsi.webapi.util.SourceUtils;
import org.ohdsi.webapi.util.TempTableCleanupManager;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.builder.SimpleJobBuilder;
import org.springframework.batch.repeat.exception.ExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.PostConstruct;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

import static org.ohdsi.webapi.Constants.GENERATE_COHORT;
import static org.ohdsi.webapi.Constants.Params.COHORT_CHARACTERIZATION_ID;
import static org.ohdsi.webapi.Constants.Params.COHORT_DEFINITION_ID;
import static org.ohdsi.webapi.Constants.Params.GENERATE_STATS;
import static org.ohdsi.webapi.Constants.Params.JOB_NAME;
import static org.ohdsi.webapi.Constants.Params.SESSION_ID;
import static org.ohdsi.webapi.Constants.Params.SOURCE_ID;
import static org.ohdsi.webapi.Constants.Params.TARGET_DATABASE_SCHEMA;
import static org.ohdsi.webapi.Constants.Params.TARGET_TABLE;
import static org.ohdsi.webapi.Constants.Params.DEMOGRAPHIC_STATS;

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
  private final FeAnalysisEntityRepository feAnalysisRepository;
  private final SourceAwareSqlRender sourceAwareSqlRender;
  private TransactionTemplate transactionTemplate;
  private StepBuilderFactory stepBuilderFactory;

  @Autowired
  public CohortGenerationService(CohortDefinitionRepository cohortDefinitionRepository,
                                 CohortGenerationInfoRepository cohortGenerationInfoRepository,
                                 JobBuilderFactory jobBuilders,
                                 StepBuilderFactory stepBuilders,
                                 JobService jobService,
                                 SourceService sourceService,
                                 GenerationCacheHelper generationCacheHelper,
                                 FeAnalysisEntityRepository feAnalysisRepository,
          TransactionTemplate transactionTemplate, StepBuilderFactory stepBuilderFactory,
          SourceAwareSqlRender sourceAwareSqlRender) {
    this.cohortDefinitionRepository = cohortDefinitionRepository;
    this.cohortGenerationInfoRepository = cohortGenerationInfoRepository;
    this.jobBuilders = jobBuilders;
    this.stepBuilders = stepBuilders;
    this.jobService = jobService;
    this.sourceService = sourceService;
    this.generationCacheHelper = generationCacheHelper;
    this.feAnalysisRepository = feAnalysisRepository;
    this.transactionTemplate = transactionTemplate;
    this.stepBuilderFactory = stepBuilderFactory;
    this.sourceAwareSqlRender = sourceAwareSqlRender;
  }

  public JobExecutionResource generateCohortViaJob(UserEntity userEntity, CohortDefinition cohortDefinition,
          Source source, Boolean demographicStat) {
      CohortGenerationInfo info = cohortDefinition.getGenerationInfoList().stream()
              .filter(val -> Objects.equals(val.getId().getSourceId(), source.getSourceId())).findFirst()
              .orElse(new CohortGenerationInfo(cohortDefinition, source.getSourceId()));

      info.setCreatedBy(userEntity);
      info.setIsChooseDemographic(demographicStat);

      cohortDefinition.getGenerationInfoList().add(info);

      info.setStatus(GenerationStatus.PENDING)
              .setStartTime(Calendar.getInstance().getTime());

      cohortDefinitionRepository.save(cohortDefinition);

      cohortDefinition.getDetails().getExpression();

      return runGenerateCohortJob(cohortDefinition, source, demographicStat);  }

  private Job buildGenerateCohortJob(CohortDefinition cohortDefinition, Source source, JobParameters jobParameters) {

    log.info("Beginning generate cohort for cohort definition id: {}", cohortDefinition.getId());

    GenerateCohortTasklet generateTasklet = new GenerateCohortTasklet(
      getSourceJdbcTemplate(source),
      getTransactionTemplate(),
      generationCacheHelper,
      cohortDefinitionRepository,
            sourceService, feAnalysisRepository
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

    return generateJobBuilder.build();
  }

  public Job buildJobForCohortGenerationWithDemographic(
          CohortDefinition cohortDefinition,
          Source source,
          JobParametersBuilder builder) {
      JobParameters jobParameters = builder.toJobParameters();
      addSessionParams(builder, jobParameters.getString(SESSION_ID));

      CreateCohortTableTasklet createCohortTableTasklet = new CreateCohortTableTasklet(getSourceJdbcTemplate(source), transactionTemplate, sourceService, sourceAwareSqlRender);
      Step createCohortTableStep = stepBuilderFactory.get(GENERATE_COHORT + ".createCohortTable")
              .tasklet(createCohortTableTasklet)
              .build();

      log.info("Beginning generate cohort for cohort definition id: {}", cohortDefinition.getId());

      GenerateLocalCohortTasklet generateLocalCohortTasklet = new GenerateLocalCohortTasklet(
              transactionTemplate,
              getSourceJdbcTemplate(source),
              this,
              sourceService,
              chunkContext -> {
                  return Arrays.asList(cohortDefinition);
              },
              generationCacheHelper,
              false
      );
      Step generateLocalCohortStep = stepBuilderFactory.get(GENERATE_COHORT + ".generateCohort")
              .tasklet(generateLocalCohortTasklet)
              .build();

      GenerateCohortTasklet generateTasklet = new GenerateCohortTasklet(getSourceJdbcTemplate(source),
              getTransactionTemplate(), generationCacheHelper, cohortDefinitionRepository, sourceService,
              feAnalysisRepository);

      ExceptionHandler exceptionHandler = new GenerationTaskExceptionHandler(new TempTableCleanupManager(
              getSourceJdbcTemplate(source), getTransactionTemplate(), source.getSourceDialect(),
              jobParameters.getString(SESSION_ID), SourceUtils.getTempQualifierOrNull(source)));

      Step generateCohortStep = stepBuilders.get("cohortDefinition.generateCohort").tasklet(generateTasklet)
              .exceptionHandler(exceptionHandler).build();

      DropCohortTableListener dropCohortTableListener = new DropCohortTableListener(getSourceJdbcTemplate(source), transactionTemplate, sourceService, sourceAwareSqlRender);

      SimpleJobBuilder generateJobBuilder = jobBuilders.get(GENERATE_COHORT)
              .start(createCohortTableStep)
              .next(generateLocalCohortStep)
              .next(generateCohortStep)
              .listener(dropCohortTableListener);

      generateJobBuilder.listener(new GenerationJobExecutionListener(sourceService, cohortDefinitionRepository, this.getTransactionTemplateRequiresNew(),
              this.getSourceJdbcTemplate(source)));

      return generateJobBuilder.build();
  }

  private JobExecutionResource runGenerateCohortJob(CohortDefinition cohortDefinition, Source source, Boolean retainCohortCovariates) {
    final JobParametersBuilder jobParametersBuilder = getJobParametersBuilder(source, cohortDefinition, retainCohortCovariates);
    Job job = buildGenerateCohortJob(cohortDefinition, source, jobParametersBuilder.toJobParameters());
    return jobService.runJob(job, jobParametersBuilder.toJobParameters());

  protected void addSessionParams(JobParametersBuilder builder, String sessionId) {
      builder.addString(TARGET_TABLE, GenerationUtils.getTempCohortTableName(sessionId));
  }

  private JobExecutionResource runGenerateCohortJob(CohortDefinition cohortDefinition, Source source) {
      return runGenerateCohortJob(cohortDefinition, source, null);
  }

  private JobExecutionResource runGenerateCohortJob(CohortDefinition cohortDefinition, Source source,
          Boolean demographic) {
      final JobParametersBuilder jobParametersBuilder = getJobParametersBuilder(source, cohortDefinition);

      if (demographic != null && demographic) {
         jobParametersBuilder.addString(DEMOGRAPHIC_STATS, Boolean.TRUE.toString());
         Job job = buildJobForCohortGenerationWithDemographic(cohortDefinition, source, jobParametersBuilder);
         return jobService.runJob(job, jobParametersBuilder.toJobParameters());      } else {
          Job job = buildGenerateCohortJob(cohortDefinition, source, jobParametersBuilder.toJobParameters());
          return jobService.runJob(job, jobParametersBuilder.toJobParameters());
      }

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
