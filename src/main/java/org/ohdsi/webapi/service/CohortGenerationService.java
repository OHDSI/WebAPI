package org.ohdsi.webapi.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.MoreObjects;
import org.ohdsi.circe.cohortdefinition.CohortExpression;
import org.ohdsi.circe.cohortdefinition.CohortExpressionQueryBuilder;
import org.ohdsi.circe.cohortdefinition.InclusionRule;
import org.ohdsi.sql.SqlRender;
import org.ohdsi.sql.SqlSplit;
import org.ohdsi.sql.SqlTranslate;
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
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

import static org.ohdsi.webapi.Constants.GENERATE_COHORT;
import static org.ohdsi.webapi.Constants.Params.*;

@Component
public class CohortGenerationService extends AbstractDaoService implements GeneratesNotification {

  private final CohortDefinitionRepository cohortDefinitionRepository;
  private final CohortGenerationInfoRepository cohortGenerationInfoRepository;
  private final JobBuilderFactory jobBuilders;
  private final StepBuilderFactory stepBuilders;
  private final JobService jobService;
  private final ObjectMapper objectMapper;
  private final SourceService sourceService;
  private final GenerationCacheHelper generationCacheHelper;

  @Autowired
  public CohortGenerationService(CohortDefinitionRepository cohortDefinitionRepository,
                                 CohortGenerationInfoRepository cohortGenerationInfoRepository,
                                 JobBuilderFactory jobBuilders,
                                 StepBuilderFactory stepBuilders,
                                 JobService jobService,
                                 ObjectMapper objectMapper,
                                 SourceService sourceService,
                                 GenerationCacheHelper generationCacheHelper) {
    this.cohortDefinitionRepository = cohortDefinitionRepository;
    this.cohortGenerationInfoRepository = cohortGenerationInfoRepository;
    this.jobBuilders = jobBuilders;
    this.stepBuilders = stepBuilders;
    this.jobService = jobService;
    this.objectMapper = objectMapper;
    this.sourceService = sourceService;
    this.generationCacheHelper = generationCacheHelper;
  }

  public JobExecutionResource generateCohort(CohortDefinition cohortDefinition, Source source, boolean includeFeatures) {

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

  public Job buildGenerateCohortJob(CohortDefinition cohortDefinition, Source source, boolean includeFeatures, JobParameters jobParameters) {

    log.info("Beginning generate cohort for cohort definition id: {}", cohortDefinition.getId());

    GenerateCohortTasklet generateTasklet = new GenerateCohortTasklet(
      getSourceJdbcTemplate(source),
      getTransactionTemplate(),
      this,
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

  public JobExecutionResource runGenerateCohortJob(CohortDefinition cohortDefinition, Source source, boolean includeFeatures) {
    final JobParametersBuilder jobParametersBuilder = getJobParametersBuilder(source, cohortDefinition);
    Job job = buildGenerateCohortJob(cohortDefinition, source, includeFeatures, jobParametersBuilder.toJobParameters());
    return jobService.runJob(job, jobParametersBuilder.toJobParameters());
  }

  public JobParametersBuilder getJobParametersBuilder(Source source, CohortDefinition cohortDefinition) {

    JobParametersBuilder builder = new JobParametersBuilder();
    builder.addString(JOB_NAME, String.format("Generating cohort %d : %s (%s)", cohortDefinition.getId(), source.getSourceName(), source.getSourceKey()));
    builder.addString(TARGET_DATABASE_SCHEMA, SourceUtils.getResultsQualifier(source));
    builder.addString(SESSION_ID, SessionUtils.sessionId());
    builder.addString(COHORT_DEFINITION_ID, String.valueOf(cohortDefinition.getId()));
    builder.addString(SOURCE_ID, String.valueOf(source.getSourceId()));
    builder.addString(GENERATE_STATS, Boolean.TRUE.toString());
    return builder;
  }

  public String[] buildGenerationSql(
    CohortExpression expression,
    Integer sourceId,
    String sessionId,
    String targetSchema,
    String targetTable,
    String targetIdFieldName,
    Integer targetId,
    boolean generateStats
  ) {

    Source source = sourceService.findBySourceId(sourceId);

    String cdmSchema = SourceUtils.getCdmQualifier(source);
    String vocabSchema = SourceUtils.getVocabQualifierOrNull(source);
    String resultsSchema = SourceUtils.getResultsQualifier(source);

    CohortExpressionQueryBuilder expressionQueryBuilder = new CohortExpressionQueryBuilder();
    StringBuilder sqlBuilder = new StringBuilder();

    CohortExpressionQueryBuilder.BuildExpressionQueryOptions options = new CohortExpressionQueryBuilder.BuildExpressionQueryOptions();
    options.cohortIdFieldName = targetIdFieldName;
    options.cohortId = targetId;
    options.cdmSchema = cdmSchema;
    options.resultSchema = resultsSchema;
    options.targetTable = targetSchema + "." + targetTable;
    options.vocabularySchema = vocabSchema;
    options.generateStats = generateStats;

    final String oracleTempSchema = SourceUtils.getTempQualifier(source);

    if (generateStats) {

      String deleteSql = "DELETE FROM @target_database_schema.cohort_inclusion WHERE @cohort_id_field_name = @target_cohort_id;";
      sqlBuilder.append(deleteSql).append("\n");

      String insertSql = "INSERT INTO @target_database_schema.cohort_inclusion (@cohort_id_field_name, rule_sequence, name, description) SELECT @target_cohort_id as @cohort_id_field_name, @iteration as rule_sequence, CAST('@ruleName' as VARCHAR(255)) as name, CAST('@ruleDescription' as VARCHAR(1000)) as description;";

      String[] names = new String[]{"iteration", "ruleName", "ruleDescription"};
      List<InclusionRule> inclusionRules = expression.inclusionRules;
      for (int i = 0; i < inclusionRules.size(); i++) {
        InclusionRule r = inclusionRules.get(i);
        String[] values = new String[]{((Integer) i).toString(), r.name, MoreObjects.firstNonNull(r.description, "")};

        String inclusionRuleSql = SqlRender.renderSql(insertSql, names, values);
        sqlBuilder.append(inclusionRuleSql).append("\n");
      }
    }

    String expressionSql = expressionQueryBuilder.buildExpressionQuery(expression, options);
    sqlBuilder.append(expressionSql);

    String renderedSql = SqlRender.renderSql(
      sqlBuilder.toString(),
      new String[] {TARGET_DATABASE_SCHEMA, COHORT_ID_FIELD_NAME, TARGET_COHORT_ID},
      new String[]{targetSchema, targetIdFieldName, targetId.toString()}
    );
    String translatedSql = SqlTranslate.translateSql(renderedSql, source.getSourceDialect(), sessionId, oracleTempSchema);
    return SqlSplit.splitSql(translatedSql);
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
