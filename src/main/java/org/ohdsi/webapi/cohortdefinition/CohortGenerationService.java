package org.ohdsi.webapi.cohortdefinition;

import org.ohdsi.webapi.GenerationStatus;
import org.ohdsi.webapi.cohortcharacterization.CreateCohortTableTasklet;
import org.ohdsi.webapi.cohortcharacterization.DropCohortTableListener;
import org.ohdsi.webapi.cohortcharacterization.GenerateLocalCohortTasklet;
import org.ohdsi.webapi.common.generation.GenerationUtils;
import org.ohdsi.webapi.feanalysis.repository.FeAnalysisEntityRepository;
import org.ohdsi.webapi.generationcache.GenerationCacheHelper;
import org.ohdsi.webapi.job.GeneratesNotification;
import org.ohdsi.webapi.job.JobExecutionResource;
import org.ohdsi.webapi.security.authz.UserEntity;
import org.ohdsi.webapi.service.AbstractDaoService;
import org.ohdsi.webapi.service.GenerationTaskExceptionHandler;
import org.ohdsi.webapi.service.JobService;
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
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.builder.SimpleJobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.exception.ExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.support.TransactionTemplate;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

import static org.ohdsi.webapi.Constants.GENERATE_COHORT;
import static org.ohdsi.webapi.Constants.Params.COHORT_DEFINITION_ID;
import static org.ohdsi.webapi.Constants.Params.GENERATE_STATS;
import static org.ohdsi.webapi.Constants.Params.JOB_NAME;
import static org.ohdsi.webapi.Constants.Params.SESSION_ID;
import static org.ohdsi.webapi.Constants.Params.SOURCE_ID;
import static org.ohdsi.webapi.Constants.Params.TARGET_DATABASE_SCHEMA;
import static org.ohdsi.webapi.Constants.Params.TARGET_TABLE;
import static org.ohdsi.webapi.Constants.Params.DEMOGRAPHIC_STATS;

@Component
public class CohortGenerationService extends AbstractDaoService implements GeneratesNotification {

  private final CohortDefinitionRepository cohortDefinitionRepository;
  private final CohortGenerationInfoRepository cohortGenerationInfoRepository;
  private final JobRepository jobRepository;
  private final PlatformTransactionManager transactionManager;
  private final JobService jobService;
  private final SourceService sourceService;
  private final GenerationCacheHelper generationCacheHelper;
  private final FeAnalysisEntityRepository feAnalysisRepository;
  private final SourceAwareSqlRender sourceAwareSqlRender;
  private TransactionTemplate transactionTemplate;

  @Autowired
  public CohortGenerationService(CohortDefinitionRepository cohortDefinitionRepository,
                                 CohortGenerationInfoRepository cohortGenerationInfoRepository,
                                 JobRepository jobRepository,
                                 PlatformTransactionManager transactionManager,
                                 JobService jobService,
                                 SourceService sourceService,
                                 GenerationCacheHelper generationCacheHelper,
                                 FeAnalysisEntityRepository feAnalysisRepository,
          @Qualifier("transactionTemplate") TransactionTemplate transactionTemplate,
          SourceAwareSqlRender sourceAwareSqlRender) {
    this.cohortDefinitionRepository = cohortDefinitionRepository;
    this.cohortGenerationInfoRepository = cohortGenerationInfoRepository;
    this.jobRepository = jobRepository;
    this.transactionManager = transactionManager;
    this.jobService = jobService;
    this.sourceService = sourceService;
    this.generationCacheHelper = generationCacheHelper;
    this.feAnalysisRepository = feAnalysisRepository;
    this.transactionTemplate = transactionTemplate;
    this.sourceAwareSqlRender = sourceAwareSqlRender;
  }

  @Transactional(propagation = Propagation.NOT_SUPPORTED)
  public JobExecutionResource generateCohortViaJob(Long userId, Integer cohortDefId, String sourceKey,
          boolean demographicStat) {
      // Execute the persistence logic in a new separate transaction that completes before batch job
      transactionTemplate.execute(status -> {
          // Load all entities in this transaction
          UserEntity userEntity = userRepository.findById(userId)
              .orElseThrow(() -> new RuntimeException("User not found: " + userId));
          
          CohortDefinitionEntity cd = cohortDefinitionRepository.findOneWithDetail(cohortDefId);
          if (cd == null) {
              throw new RuntimeException("CohortDefinition not found: " + cohortDefId);
          }
          
          Source source = getSourceRepository().findBySourceKey(sourceKey);
          if (source == null) {
              throw new RuntimeException("Source not found: " + sourceKey);
          }
          
          Integer sourceId = source.getSourceId();
          
          CohortGenerationInfo info = cd.getGenerationInfoList().stream()
                  .filter(val -> Objects.equals(val.getId().getSourceId(), sourceId)).findFirst()
                  .orElse(new CohortGenerationInfo(cd, sourceId));

          info.setCreatedBy(userEntity);
          info.setIsDemographic(demographicStat);

          cd.getGenerationInfoList().add(info);

          info.setStatus(GenerationStatus.PENDING)
                  .setStartTime(Calendar.getInstance().getTime());

          cohortDefinitionRepository.save(cd);
          
          // Ensure lazy fields are initialized before transaction ends
          if (cd.getDetails() != null) {
              cd.getDetails().getExpression();
          }
          
          return null;
      });

      // Reload for the batch job in another short transaction
      CohortDefinitionEntity reloadedDef = transactionTemplate.execute(status -> 
          cohortDefinitionRepository.findOneWithDetail(cohortDefId)
      );
      
      Source source = getSourceRepository().findBySourceKey(sourceKey);
      
      return runGenerateCohortJob(reloadedDef, source, demographicStat);
  }

  private Job buildGenerateCohortJob(CohortDefinitionEntity cohortDefinition, Source source, JobParameters jobParameters) {

    log.info("Beginning generate cohort for cohort definition id: {}", cohortDefinition.getId());

    GenerateCohortTasklet generateTasklet = new GenerateCohortTasklet(
      getSourceJdbcTemplate(source),
      getBatchTransactionTemplate(),
      generationCacheHelper,
      cohortDefinitionRepository,
      sourceService
    );

    ExceptionHandler exceptionHandler = new GenerationTaskExceptionHandler(new TempTableCleanupManager(getSourceJdbcTemplate(source),
            getBatchTransactionTemplate(),
            source.getSourceDialect(),
            jobParameters.getString(SESSION_ID),
            SourceUtils.getTempQualifierOrNull(source)
    ));

    Step generateCohortStep = new StepBuilder("cohortDefinition.generateCohort", jobRepository)
            .tasklet(generateTasklet, transactionManager)
            .exceptionHandler(exceptionHandler)
            .build();

    SimpleJobBuilder generateJobBuilder = new JobBuilder(GENERATE_COHORT, jobRepository).start(generateCohortStep);

    // Listener runs outside step context, needs JpaTransactionManager for entity operations
    generateJobBuilder.listener(new GenerationJobExecutionListener(sourceService, cohortDefinitionRepository, this.getTransactionTemplateRequiresNew(),
            this.getSourceJdbcTemplate(source)));

    return generateJobBuilder.build();
  }

  public Job buildJobForCohortGenerationWithDemographic(
          CohortDefinitionEntity cohortDefinition,
          Source source,
          JobParametersBuilder builder) {
      JobParameters jobParameters = builder.toJobParameters();
      addSessionParams(builder, jobParameters.getString(SESSION_ID));

      CreateCohortTableTasklet createCohortTableTasklet = new CreateCohortTableTasklet(getSourceJdbcTemplate(source), transactionTemplate, sourceService, sourceAwareSqlRender);
      Step createCohortTableStep = new StepBuilder(GENERATE_COHORT + ".createCohortTable", jobRepository)
              .tasklet(createCohortTableTasklet, transactionManager)
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
      Step generateLocalCohortStep =  new StepBuilder(GENERATE_COHORT + ".generateCohort", jobRepository)
              .tasklet(generateLocalCohortTasklet, transactionManager)
              .build();

      GenerateCohortTasklet generateTasklet = new GenerateCohortTasklet(getSourceJdbcTemplate(source),
              getTransactionTemplate(), generationCacheHelper, cohortDefinitionRepository, sourceService,
              feAnalysisRepository);

      ExceptionHandler exceptionHandler = new GenerationTaskExceptionHandler(new TempTableCleanupManager(
              getSourceJdbcTemplate(source), getTransactionTemplate(), source.getSourceDialect(),
              jobParameters.getString(SESSION_ID), SourceUtils.getTempQualifierOrNull(source)));

      Step generateCohortStep = new StepBuilder("cohortDefinition.generateCohort", jobRepository)
        .tasklet(generateTasklet, transactionManager)
        .exceptionHandler(exceptionHandler).build();

      DropCohortTableListener dropCohortTableListener = new DropCohortTableListener(getSourceJdbcTemplate(source), sourceService, sourceAwareSqlRender);

      SimpleJobBuilder generateJobBuilder = new JobBuilder(GENERATE_COHORT, jobRepository)
              .start(createCohortTableStep)
              .next(generateLocalCohortStep)
              .next(generateCohortStep)
              .listener(dropCohortTableListener);

      generateJobBuilder.listener(new GenerationJobExecutionListener(sourceService, cohortDefinitionRepository, this.getTransactionTemplateRequiresNew(),
              this.getSourceJdbcTemplate(source)));

      return generateJobBuilder.build();
  }

  protected void addSessionParams(JobParametersBuilder builder, String sessionId) {
      builder.addString(TARGET_TABLE, GenerationUtils.getTempCohortTableName(sessionId));
  }

  private JobExecutionResource runGenerateCohortJob(CohortDefinitionEntity cohortDefinition, Source source,
          boolean demographic) {
      final JobParametersBuilder jobParametersBuilder = getJobParametersBuilder(source, cohortDefinition);

      if (demographic) {
         jobParametersBuilder.addString(DEMOGRAPHIC_STATS, Boolean.TRUE.toString());
         Job job = buildJobForCohortGenerationWithDemographic(cohortDefinition, source, jobParametersBuilder);
         return jobService.runJob(job, jobParametersBuilder.toJobParameters());
      } else {
          Job job = buildGenerateCohortJob(cohortDefinition, source, jobParametersBuilder.toJobParameters());
          return jobService.runJob(job, jobParametersBuilder.toJobParameters());
      }
  }

  private JobParametersBuilder getJobParametersBuilder(Source source, CohortDefinitionEntity cohortDefinition) {

    JobParametersBuilder builder = new JobParametersBuilder();
    builder.addString(JOB_NAME, String.format("Generating cohort %d : %s (%s)", cohortDefinition.getId(), source.getSourceName(), source.getSourceKey()));
    builder.addString(TARGET_DATABASE_SCHEMA, SourceUtils.getResultsQualifier(source));
    builder.addString(SESSION_ID, SessionUtils.sessionId());
    builder.addString(COHORT_DEFINITION_ID, String.valueOf(cohortDefinition.getId()));
    builder.addString(SOURCE_ID, String.valueOf(source.getSourceId()));
    builder.addString(GENERATE_STATS, Boolean.TRUE.toString());
    return builder;
  }

  @EventListener(ApplicationReadyEvent.class)
  public void init(){

    invalidateCohortGenerations();
  }

  private void invalidateCohortGenerations() {

    getTransactionTemplateRequiresNew().execute(status -> {
      List<CohortGenerationInfo> executions = cohortGenerationInfoRepository.findByStatusIn(INVALIDATE_STATUSES);
      invalidateExecutions(executions);
      cohortGenerationInfoRepository.saveAll(executions);
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
