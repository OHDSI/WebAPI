package org.ohdsi.webapi.service;

import org.ohdsi.webapi.GenerationStatus;
import org.ohdsi.webapi.cohortdefinition.*;
import org.ohdsi.webapi.cohortfeatures.GenerateCohortFeaturesTasklet;
import org.ohdsi.webapi.job.GeneratesNotification;
import org.ohdsi.webapi.job.JobExecutionResource;
import org.ohdsi.webapi.job.JobTemplate;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceDaimon;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.job.builder.SimpleJobBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static org.ohdsi.webapi.Constants.GENERATE_COHORT;
import static org.ohdsi.webapi.Constants.Params.*;

@Component
public class CohortGenerationService extends AbstractDaoService implements GeneratesNotification {


  private static final String DEFAULT_COHORT_TABLE = "cohort";

  private final CohortDefinitionRepository cohortDefinitionRepository;

  private final CohortGenerationInfoRepository cohortGenerationInfoRepository;

  private final JobBuilderFactory jobBuilders;

  private final StepBuilderFactory stepBuilders;

  private final JobTemplate jobTemplate;

  private final JobExplorer jobExplorer;

  private Map<Long, Job> jobMap = new HashMap<>();

  @Autowired
  public CohortGenerationService(CohortDefinitionRepository cohortDefinitionRepository,
                                 CohortGenerationInfoRepository cohortGenerationInfoRepository,
                                 JobBuilderFactory jobBuilders,
                                 StepBuilderFactory stepBuilders,
                                 JobTemplate jobTemplate,
                                 JobExplorer jobExplorer) {
    this.cohortDefinitionRepository = cohortDefinitionRepository;
    this.cohortGenerationInfoRepository = cohortGenerationInfoRepository;
    this.jobBuilders = jobBuilders;
    this.stepBuilders = stepBuilders;
    this.jobTemplate = jobTemplate;
    this.jobExplorer = jobExplorer;
  }

  public JobExecutionResource generateCohort(CohortDefinition cohortDefinition, Source source, boolean includeFeatures) {

    return generateCohort(cohortDefinition, source, includeFeatures, DEFAULT_COHORT_TABLE);
  }

  public JobExecutionResource generateCohort(CohortDefinition cohortDefinition, Source source, boolean includeFeatures, String targetTable) {

    CohortGenerationInfo info =  cohortDefinition.getGenerationInfoList().stream()
            .filter(val -> Objects.equals(val.getId().getSourceId(), source.getSourceId())).findFirst()
            .orElse(new CohortGenerationInfo(cohortDefinition, source.getSourceId()));
    cohortDefinition.getGenerationInfoList().add(info);

    info.setStatus(GenerationStatus.PENDING)
            .setStartTime(Calendar.getInstance().getTime());

    info.setIncludeFeatures(includeFeatures);

    cohortDefinitionRepository.save(cohortDefinition);

    return runGenerateCohortJob(cohortDefinition, source, includeFeatures, true, targetTable);
  }

  public Job buildGenerateCohortJob(CohortDefinition cohortDefinition, Source source, boolean includeFeatures, boolean updateGenerationInfo, String jobName) {

    log.info("Beginning generate cohort for cohort definition id: {}", cohortDefinition.getId());

    GenerateCohortTasklet generateTasklet = new GenerateCohortTasklet(getSourceJdbcTemplate(source), getTransactionTemplate(), cohortDefinitionRepository,
            getSourceRepository());

    Step generateCohortStep = stepBuilders.get("cohortDefinition.generateCohort")
            .tasklet(generateTasklet)
            .build();

    SimpleJobBuilder generateJobBuilder = jobBuilders.get(jobName).start(generateCohortStep);

    if (updateGenerationInfo) {
      generateJobBuilder.listener(new GenerationJobExecutionListener(this, cohortDefinitionRepository, this.getTransactionTemplateRequiresNew(),
              this.getSourceJdbcTemplate(source)));
    }

    if (includeFeatures) {
      GenerateCohortFeaturesTasklet generateCohortFeaturesTasklet =
              new GenerateCohortFeaturesTasklet(getSourceJdbcTemplate(source), getTransactionTemplate());

      Step generateCohortFeaturesStep = stepBuilders.get("cohortFeatures.generateFeatures")
              .tasklet(generateCohortFeaturesTasklet)
              .build();

      generateJobBuilder.next(generateCohortFeaturesStep);
    }

    return generateJobBuilder.build();
  }

  public JobExecutionResource runGenerateCohortJob(CohortDefinition cohortDefinition, Source source, boolean includeFeatures, boolean updateGenerationInfo, String targetTable, Map<String, String> extraJobParams, String jobName) {
    Job job = buildGenerateCohortJob(cohortDefinition, source, includeFeatures, updateGenerationInfo, jobName);
    final JobParametersBuilder jobParametersBuilder = getJobParametersBuilder(source, cohortDefinition, targetTable);
    extraJobParams.forEach(jobParametersBuilder::addString);
    JobExecutionResource jobExecution = this.jobTemplate.launch(job, jobParametersBuilder.toJobParameters());
    jobMap.put(jobExecution.getExecutionId(), job);
    return jobExecution;
  }

  public JobExecutionResource runGenerateCohortJob(CohortDefinition cohortDefinition, Source source, boolean includeFeatures, boolean updateGenerationInfo, String targetTable) {
    return runGenerateCohortJob(cohortDefinition, source, includeFeatures, updateGenerationInfo, targetTable, new HashMap<>(), GENERATE_COHORT);
  }

  public Optional<JobExecution> getJobExecution(Source source, Integer cohortDefinitionId) {

    return jobExplorer.findRunningJobExecutions(GENERATE_COHORT)
            .stream().filter(e -> {
      JobParameters parameters = e.getJobParameters();
      return Objects.equals(parameters.getString(COHORT_DEFINITION_ID), Integer.toString(cohortDefinitionId))
              && Objects.equals(parameters.getString(SOURCE_ID), Integer.toString(source.getSourceId()));
    }).findFirst();
  }

  public JobExecution getJobExecution(Long jobExecutionId) {

    return jobExplorer.getJobExecution(jobExecutionId);
  }

  public Job getRunningJob(Long jobExecutionId) {

    return jobMap.get(jobExecutionId);
  }

  public void removeJob(Long jobExecutionId) {

    jobMap.remove(jobExecutionId);
  }

  public JobParametersBuilder getJobParametersBuilder(Source source, CohortDefinition cohortDefinition, String targetTable) {

    String cdmTableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.CDM);
    String resultsTableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Results);
    String vocabularyTableQualifier = source.getTableQualifierOrNull(SourceDaimon.DaimonType.Vocabulary);

    JobParametersBuilder builder = new JobParametersBuilder();
    builder.addString(JOB_NAME, String.format("Generating cohort %d : %s (%s)", cohortDefinition.getId(), source.getSourceName(), source.getSourceKey()));
    builder.addString(CDM_DATABASE_SCHEMA, cdmTableQualifier);
    builder.addString(RESULTS_DATABASE_SCHEMA, resultsTableQualifier);

    if (targetTable.indexOf('.') != -1) {
      String[] targetParts = targetTable.split("\\.");
      builder.addString(TARGET_DATABASE_SCHEMA, targetParts[0]);
      builder.addString(TARGET_TABLE, targetParts[1]);
    } else {
      builder.addString(TARGET_DATABASE_SCHEMA, resultsTableQualifier);
      builder.addString(TARGET_TABLE, targetTable);
    }

    if (vocabularyTableQualifier != null) {
      builder.addString(VOCABULARY_DATABASE_SCHEMA, vocabularyTableQualifier);
    }
    builder.addString(TARGET_DIALECT, source.getSourceDialect());
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
