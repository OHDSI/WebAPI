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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.ohdsi.webapi.Constants.GENERATE_COHORT;
import static org.ohdsi.webapi.Constants.Params.*;
import static org.ohdsi.webapi.source.SourceDaimon.DaimonType.CDM;
import static org.ohdsi.webapi.source.SourceDaimon.DaimonType.Vocabulary;

@Component
public class CohortGenerationService extends AbstractDaoService implements GeneratesNotification {

  private static final String DEFAULT_COHORT_TABLE = "cohort";
  private final CohortDefinitionRepository cohortDefinitionRepository;
  private final CohortGenerationInfoRepository cohortGenerationInfoRepository;
  private final JobBuilderFactory jobBuilders;
  private final StepBuilderFactory stepBuilders;
  private JobService jobService;
  private ObjectMapper objectMapper;
  private SourceService sourceService;

  @Autowired
  public CohortGenerationService(CohortDefinitionRepository cohortDefinitionRepository,
                                 CohortGenerationInfoRepository cohortGenerationInfoRepository,
                                 JobBuilderFactory jobBuilders,
                                 StepBuilderFactory stepBuilders,
                                 JobService jobService,
                                 ObjectMapper objectMapper,
                                 SourceService sourceService) {
    this.cohortDefinitionRepository = cohortDefinitionRepository;
    this.cohortGenerationInfoRepository = cohortGenerationInfoRepository;
    this.jobBuilders = jobBuilders;
    this.stepBuilders = stepBuilders;
    this.jobService = jobService;
    this.objectMapper = objectMapper;
    this.sourceService = sourceService;
  }

  public JobExecutionResource generateCohort(CohortDefinition cohortDefinition, Source source, boolean includeFeatures) {

    return generateCohort(cohortDefinition, source, includeFeatures, DEFAULT_COHORT_TABLE);
  }

  public JobExecutionResource generateCohort(CohortDefinition cohortDefinition, Source source, boolean includeFeatures, String targetTable) {

    CohortGenerationInfo info = cohortDefinition.getGenerationInfoList().stream()
            .filter(val -> Objects.equals(val.getId().getSourceId(), source.getSourceId())).findFirst()
            .orElse(new CohortGenerationInfo(cohortDefinition, source.getSourceId()));
    cohortDefinition.getGenerationInfoList().add(info);

    info.setStatus(GenerationStatus.PENDING)
            .setStartTime(Calendar.getInstance().getTime());

    info.setIncludeFeatures(includeFeatures);

    cohortDefinitionRepository.save(cohortDefinition);

    return runGenerateCohortJob(cohortDefinition, source, includeFeatures, true, targetTable);
  }

  public Job buildGenerateCohortJob(CohortDefinition cohortDefinition, Source source, boolean includeFeatures, boolean updateGenerationInfo,
                                    String jobName, JobParameters jobParameters) {

    log.info("Beginning generate cohort for cohort definition id: {}", cohortDefinition.getId());

    GenerateCohortTasklet generateTasklet = new GenerateCohortTasklet(getSourceJdbcTemplate(source), getTransactionTemplate(), this);

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

    SimpleJobBuilder generateJobBuilder = jobBuilders.get(jobName).start(generateCohortStep);

    if (updateGenerationInfo) {
      generateJobBuilder.listener(new GenerationJobExecutionListener(sourceService, cohortDefinitionRepository, this.getTransactionTemplateRequiresNew(),
              this.getSourceJdbcTemplate(source)));
    }

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

  public JobExecutionResource runGenerateCohortJob(CohortDefinition cohortDefinition, Source source, boolean includeFeatures, boolean updateGenerationInfo, String targetTable, Map<String, String> extraJobParams, String jobName) {
    final JobParametersBuilder jobParametersBuilder = getJobParametersBuilder(source, cohortDefinition, targetTable);
    Job job = buildGenerateCohortJob(cohortDefinition, source, includeFeatures, updateGenerationInfo, jobName, jobParametersBuilder.toJobParameters());
    extraJobParams.forEach(jobParametersBuilder::addString);
    return jobService.runJob(job, jobParametersBuilder.toJobParameters());
  }

  public JobExecutionResource runGenerateCohortJob(CohortDefinition cohortDefinition, Source source, boolean includeFeatures, boolean updateGenerationInfo, String targetTable) {
    return runGenerateCohortJob(cohortDefinition, source, includeFeatures, updateGenerationInfo, targetTable, new HashMap<>(), GENERATE_COHORT);
  }

  public JobParametersBuilder getJobParametersBuilder(Source source, CohortDefinition cohortDefinition, String targetTable) {

    JobParametersBuilder builder = new JobParametersBuilder();
    builder.addString(JOB_NAME, String.format("Generating cohort %d : %s (%s)", cohortDefinition.getId(), source.getSourceName(), source.getSourceKey()));
    builder.addString(TARGET_DATABASE_SCHEMA, SourceUtils.getResultsQualifier(source));
    builder.addString(TARGET_TABLE, targetTable);
    builder.addString(SESSION_ID, SessionUtils.sessionId());
    builder.addString(COHORT_DEFINITION_ID, String.valueOf(cohortDefinition.getId()));
    builder.addString(SOURCE_ID, String.valueOf(source.getSourceId()));
    builder.addString(GENERATE_STATS, Boolean.TRUE.toString());
    return builder;
  }

  public String[] buildGenerationSql(
    Integer cohortDefinitionId,
    Integer sourceId,
    String sessionId,
    String targetSchema,
    String targetTable,
    boolean generateStats
  ) {

    Source source = sourceService.findBySourceId(sourceId);

    String cdmSchema = SourceUtils.getCdmQualifier(source);
    String vocabSchema = SourceUtils.getVocabQualifierOrNull(source);
    String resultsSchema = SourceUtils.getResultsQualifier(source);

    CohortExpressionQueryBuilder expressionQueryBuilder = new CohortExpressionQueryBuilder();
    StringBuilder sqlBuilder = new StringBuilder();

    CohortDefinition def = this.cohortDefinitionRepository.findOneWithDetail(cohortDefinitionId);
    CohortExpression expression = def.getDetails().getExpressionObject();

    CohortExpressionQueryBuilder.BuildExpressionQueryOptions options = new CohortExpressionQueryBuilder.BuildExpressionQueryOptions();
    options.cohortId = cohortDefinitionId;
    options.cdmSchema = cdmSchema;
    options.resultSchema = resultsSchema;
    options.targetTable = targetSchema + "." + targetTable;
    options.vocabularySchema = vocabSchema;
    options.generateStats = generateStats;

    final String oracleTempSchema = SourceUtils.getTempQualifier(source);

    if (generateStats) {

      String deleteSql = "DELETE FROM @target_database_schema.cohort_inclusion WHERE cohort_definition_id = @target_cohort_id;";
      sqlBuilder.append(deleteSql).append("\n");

      String insertSql = "INSERT INTO @target_database_schema.cohort_inclusion (cohort_definition_id, rule_sequence, name, description) SELECT @target_cohort_id as cohort_definition_id, @iteration as rule_sequence, CAST('@ruleName' as VARCHAR(255)) as name, CAST('@ruleDescription' as VARCHAR(1000)) as description;";

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

    String renderedSql = SqlRender.renderSql(sqlBuilder.toString(), new String[] {"target_database_schema", "target_cohort_id"}, new String[]{targetSchema, cohortDefinitionId.toString()});
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
