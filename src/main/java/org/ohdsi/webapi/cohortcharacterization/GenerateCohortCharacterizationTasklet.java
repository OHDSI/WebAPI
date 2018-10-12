/*
 * Copyright 2017 Observational Health Data Sciences and Informatics <OHDSI.org>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ohdsi.webapi.cohortcharacterization;

import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.ohdsi.analysis.Utils;
import org.ohdsi.analysis.cohortcharacterization.design.StandardFeatureAnalysisType;
import org.ohdsi.circe.cohortdefinition.*;
import org.ohdsi.circe.helper.ResourceHelper;
import org.ohdsi.featureExtraction.FeatureExtraction;
import org.ohdsi.sql.SqlRender;
import org.ohdsi.sql.SqlSplit;
import org.ohdsi.sql.SqlTranslate;
import org.ohdsi.webapi.cohortcharacterization.converter.SerializedCcToCcConverter;
import org.ohdsi.webapi.cohortcharacterization.domain.CcParamEntity;
import org.ohdsi.webapi.cohortcharacterization.domain.CohortCharacterizationEntity;
import org.ohdsi.webapi.cohortcharacterization.repository.AnalysisGenerationInfoEntityRepository;
import org.ohdsi.webapi.cohortdefinition.CohortDefinition;
import org.ohdsi.webapi.common.generation.AnalysisTasklet;
import org.ohdsi.webapi.feanalysis.FeAnalysisService;
import org.ohdsi.webapi.feanalysis.domain.*;
import org.ohdsi.webapi.service.SourceService;
import org.ohdsi.webapi.shiro.Entities.UserEntity;
import org.ohdsi.webapi.shiro.Entities.UserRepository;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceDaimon;
import org.ohdsi.webapi.util.SessionUtils;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.stream.Collectors;

import static org.ohdsi.webapi.Constants.Params.*;

public class GenerateCohortCharacterizationTasklet extends AnalysisTasklet {
    private static final String[] CUSTOM_PARAMETERS = {"analysisId", "analysisName", "cohortId", "jobId", "design"};
    private static final String[] RETRIEVING_PARAMETERS = {"features", "featureRefs", "analysisRefs", "cohortId", "executionId"};

    private static final String COHORT_STATS_QUERY = ResourceHelper.GetResourceAsString("/resources/cohortcharacterizations/sql/prevalenceWithCriteria.sql");
    private static final String CREATE_COHORT_SQL = ResourceHelper.GetResourceAsString("/resources/cohortcharacterizations/sql/createCohortTable.sql");
    private static final String DROP_TABLE_SQL = "DROP TABLE @results_database_schema.@target_table;";

    private final ExecutorService taskExecutor;
    private final JdbcTemplate jdbcTemplate;
    private final CcService ccService;
    private final FeAnalysisService feAnalysisService;
    private final SourceService sourceService;
    private final UserRepository userRepository;
    private final CohortExpressionQueryBuilder queryBuilder;

    public GenerateCohortCharacterizationTasklet(
            final JdbcTemplate jdbcTemplate,
            final TransactionTemplate transactionTemplate,
            final CcService ccService,
            final FeAnalysisService feAnalysisService,
            final AnalysisGenerationInfoEntityRepository analysisGenerationInfoEntityRepository,
            final SourceService sourceService,
            final UserRepository userRepository
    ) {
        super(LogFactory.getLog(GenerateCohortCharacterizationTasklet.class), transactionTemplate, analysisGenerationInfoEntityRepository);
        this.jdbcTemplate = jdbcTemplate;
        this.ccService = ccService;
        this.feAnalysisService = feAnalysisService;
        this.sourceService = sourceService;
        this.userRepository = userRepository;
        this.taskExecutor = Executors.newSingleThreadExecutor();
        this.queryBuilder = new CohortExpressionQueryBuilder();
    }

    protected int[] doTask(ChunkContext chunkContext) {
        initTx();
        new CcTask(chunkContext).run();
        return null;
    }

    private void initTx() {
        DefaultTransactionDefinition txDefinition = new DefaultTransactionDefinition();
        txDefinition.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        TransactionStatus initStatus = this.transactionTemplate.getTransactionManager().getTransaction(txDefinition);
        this.transactionTemplate.getTransactionManager().commit(initStatus);
    }

    private class CohortExpressionBuilder {
        private String json;
        private int conceptSetIndex;
        private TypeReference<CohortExpression> cohortExpressionTypeRef;

        CohortExpressionBuilder(CohortDefinition cohortDefinition, FeAnalysisCriteriaEntity feature) {

            cohortExpressionTypeRef = new TypeReference<CohortExpression>() {};
            json = Utils.serialize(cohortDefinition.getExpression());
            initConceptSets(feature);
        }

        private void initConceptSets(FeAnalysisCriteriaEntity feature) {

            CohortExpression expression = Utils.deserialize(this.json, cohortExpressionTypeRef);
            this.conceptSetIndex = expression.conceptSets.length;
            List<org.ohdsi.circe.cohortdefinition.ConceptSet> conceptSets = new ArrayList<>(Arrays.asList(expression.conceptSets));
            List<ConceptSet> featureConceptSets = feature.getFeatureAnalysis().getConceptSets();
            if (Objects.nonNull(featureConceptSets)) {
                conceptSets.addAll(feature.getFeatureAnalysis().getConceptSets().stream()
                        .map(this::cloneConceptSet)
                        .peek(conceptSet -> conceptSet.id += conceptSetIndex)
                        .collect(Collectors.toList()));
            }
            expression.conceptSets = conceptSets.toArray(new org.ohdsi.circe.cohortdefinition.ConceptSet[0]);
            this.json = Utils.serialize(expression);
        }

        private org.ohdsi.circe.cohortdefinition.ConceptSet cloneConceptSet(org.ohdsi.circe.cohortdefinition.ConceptSet conceptSet) {
            org.ohdsi.circe.cohortdefinition.ConceptSet result = new org.ohdsi.circe.cohortdefinition.ConceptSet();
            result.id = conceptSet.id;
            result.name = conceptSet.name;
            result.expression = conceptSet.expression;
            return result;
        }

        CohortExpression withCriteria(CriteriaGroup group) {

                CohortExpression expression = Utils.deserialize(json, cohortExpressionTypeRef);
                CriteriaGroup copy = copy(group, new TypeReference<CriteriaGroup>() {});
                Arrays.stream(copy.criteriaList)
                        .map(cc -> cc.criteria)
                        .forEach(this::mapCodesetId);

                expression.inclusionRules.add(newRule(copy));
                return expression;
        }

        private <T> T copy(T object, TypeReference<T> typeRef) {
            final String json = Utils.serialize(object);
            return Utils.deserialize(json, typeRef);
        }

        private void mapCodesetId(Criteria criteria) {
            if (criteria instanceof ObservationPeriod || criteria instanceof PayerPlanPeriod) {
                return;
            }
            try {
                Integer codesetId = (Integer) FieldUtils.readDeclaredField(criteria, "codesetId");
                FieldUtils.writeDeclaredField(criteria, "codesetId", codesetId + conceptSetIndex);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        private InclusionRule newRule(CriteriaGroup group) {
            InclusionRule rule = new InclusionRule();
            rule.expression = group;
            return rule;
        }

    }

    private class CcTask {

        final String prevalenceRetrievingQuery = ResourceHelper.GetResourceAsString("/resources/cohortcharacterizations/sql/prevalenceRetrieving.sql");
        
        final String distributionRetrievingQuery = ResourceHelper.GetResourceAsString("/resources/cohortcharacterizations/sql/distributionRetrieving.sql");
        
        final String customDistributionQueryWrapper = ResourceHelper.GetResourceAsString("/resources/cohortcharacterizations/sql/customDistribution.sql");
        
        final String customPrevalenceQueryWrapper = ResourceHelper.GetResourceAsString("/resources/cohortcharacterizations/sql/customPrevalence.sql");

        final CohortCharacterizationEntity cohortCharacterization;
        final Source source;
        final UserEntity userEntity;
        final String cohortTable;
        
        private final Long jobId;

        CcTask(final ChunkContext context) {
            Map<String, Object> jobParams = context.getStepContext().getJobParameters();
            this.cohortCharacterization = ccService.findByIdWithLinkedEntities(
                    Long.valueOf(jobParams.get(COHORT_CHARACTERIZATION_ID).toString())
            );
            this.jobId = context.getStepContext().getStepExecution().getJobExecution().getId();
            this.source = sourceService.findBySourceId(
                    Integer.valueOf(jobParams.get(SOURCE_ID).toString())
            );
            this.cohortTable = jobParams.get(TARGET_TABLE).toString();
            this.userEntity = userRepository.findByLogin(jobParams.get(JOB_AUTHOR).toString());
        }
        
        private void run() {

            saveInfo(jobId, new SerializedCcToCcConverter().convertToDatabaseColumn(cohortCharacterization), userEntity);
            cohortCharacterization.getCohortDefinitions()
                    .forEach(definition -> runAnalysisOnCohort(definition.getId()));
        }

        private int[] runAnalysisOnCohort(final Integer cohortDefinitionId) {
            FutureTask<int[]> batchUpdateTask = new FutureTask<>(
                    () -> jdbcTemplate.batchUpdate(
                            getSqlQueriesToRun(
                                    createFeJsonObject(
                                            createDefaultOptions(cohortDefinitionId)
                                    ), 
                                    cohortDefinitionId
                            )
                    )
            );
            taskExecutor.execute(batchUpdateTask);
            return waitForFuture(batchUpdateTask);
        }

        private String renderCustomAnalysisDesign(FeAnalysisWithStringEntity fa, Integer cohortId) {
            Map<String, String> params = cohortCharacterization.getParameters().stream().collect(Collectors.toMap(CcParamEntity::getName, CcParamEntity::getValue));
            params.put("cdm_database_schema", source.getTableQualifier(SourceDaimon.DaimonType.CDM));
            params.put("cohort_table", source.getTableQualifier(SourceDaimon.DaimonType.Results) + "." + cohortTable);
            params.put("cohort_id", cohortId.toString());
            params.put("analysis_id", fa.getId().toString());

            return SqlRender.renderSql(
                    fa.getDesign(),
                    params.keySet().toArray(new String[params.size()]),
                    params.values().toArray(new String[params.size()])
            );
        }

        private List<String> getQueriesForCustomDistributionAnalyses(final Integer cohortId) {
            return cohortCharacterization.getFeatureAnalyses()
                    .stream()
                    .filter(FeAnalysisEntity::isCustom)
                    .filter(v -> v.getStatType() == CcResultType.DISTRIBUTION)
                    .map(v -> SqlRender.renderSql(customDistributionQueryWrapper,
                            CUSTOM_PARAMETERS,
                            new String[] { String.valueOf(v.getId()), org.springframework.util.StringUtils.quote(v.getName()), String.valueOf(cohortId), String.valueOf(jobId), renderCustomAnalysisDesign((FeAnalysisWithStringEntity) v, cohortId)} ))
                    .collect(Collectors.toList());
        }
        
        private List<String> getQueriesForCustomPrevalenceAnalyses(final Integer cohortId) {
            return cohortCharacterization.getFeatureAnalyses()
                    .stream()
                    .filter(FeAnalysisEntity::isCustom)
                    .filter(v -> v.getStatType() == CcResultType.PREVALENCE)
                    .map(v -> SqlRender.renderSql(customPrevalenceQueryWrapper,
                            CUSTOM_PARAMETERS,
                            new String[] { String.valueOf(v.getId()), org.springframework.util.StringUtils.quote(v.getName()), String.valueOf(cohortId), String.valueOf(jobId), renderCustomAnalysisDesign((FeAnalysisWithStringEntity) v, cohortId)} ))
                    .collect(Collectors.toList());
        }

        private List<String> getQueriesForCriteriaAnalyses(Integer cohortDefinitionId) {
            List<String> queries = new ArrayList<>();
            List<FeAnalysisWithCriteriaEntity> analysesWithCriteria = getFeAnalysesWithCriteria();
            if (!analysesWithCriteria.isEmpty()) {
                CohortDefinition cohort = cohortCharacterization.getCohortDefinitions().stream()
                        .filter(cd -> Objects.equals(cd.getId(), cohortDefinitionId))
                        .findFirst().orElseThrow(IllegalArgumentException::new);
                analysesWithCriteria.stream()
                        .map(analysis -> getCohortWithCriteriaFeaturesQueries(cohort, analysis))
                        .flatMap(Collection::stream)
                        .forEach(queries::add);
            }
            return queries;
        }

        private List<FeAnalysisWithCriteriaEntity> getFeAnalysesWithCriteria() {

            return cohortCharacterization.getFeatureAnalyses().stream()
                    .filter(fa -> StandardFeatureAnalysisType.CRITERIA_SET.equals(fa.getType()))
                    .map(fa -> (FeAnalysisWithCriteriaEntity)fa)
                    .collect(Collectors.toList());
        }

        private List<String> getQueriesForPresetAnalyses(final JSONObject jsonObject, final Integer cohortId) {
            final String cohortWrapper = "select %1$d as %2$s from (%3$s) W";

            final String featureRefColumns = "cohort_definition_id, covariate_id, covariate_name, analysis_id, concept_id";
            final String featureRefs = String.format(cohortWrapper, cohortId, featureRefColumns,
                    StringUtils.stripEnd(jsonObject.getString("sqlQueryFeatureRef"), ";"));

            final String analysisRefColumns = "cohort_definition_id, CAST(analysis_id AS INT) analysis_id, analysis_name, domain_id, start_day, end_day, CAST(is_binary AS CHAR(1)) is_binary,CAST(missing_means_zero AS CHAR(1)) missing_means_zero";
            final String analysisRefs = String.format(cohortWrapper, cohortId, analysisRefColumns,
                    StringUtils.stripEnd(jsonObject.getString("sqlQueryAnalysisRef"), ";"));

            final List<String> queries = new ArrayList<>();

            if (ccHasPresetDistributionAnalyses()) {
                final String distColumns = "cohort_definition_id, covariate_id, count_value, min_value, max_value, average_value, "
                        + "standard_deviation, median_value, p10_value, p25_value, p75_value, p90_value";
                final String distFeatures = String.format(cohortWrapper, cohortId, distColumns,
                        StringUtils.stripEnd(jsonObject.getString("sqlQueryContinuousFeatures"), ";"));
                final String query = SqlRender.renderSql(distributionRetrievingQuery, RETRIEVING_PARAMETERS,
                        new String[] { distFeatures, featureRefs, analysisRefs, String.valueOf(cohortId), String.valueOf(jobId) });
                queries.add(query);
            }
            if (ccHasPresetPrevalenceAnalyses()) {
                final String featureColumns = "cohort_definition_id, covariate_id, sum_value, average_value";
                final String features = String.format(cohortWrapper, cohortId, featureColumns,
                        StringUtils.stripEnd(jsonObject.getString("sqlQueryFeatures"), ";"));
                final String query = SqlRender.renderSql(prevalenceRetrievingQuery, RETRIEVING_PARAMETERS,
                        new String[]{ features, featureRefs, analysisRefs, String.valueOf(cohortId), String.valueOf(jobId) });
                queries.add(query);
            }

            return queries;
        }

        private boolean ccHasPresetPrevalenceAnalyses() {
            return cohortCharacterization.getFeatureAnalyses()
                    .stream()
                    .anyMatch(analysis -> analysis.isPreset() && analysis.getStatType() == CcResultType.PREVALENCE);
        }

        private boolean ccHasPresetDistributionAnalyses() {
            return cohortCharacterization.getFeatureAnalyses()
                    .stream()
                    .anyMatch(analysis -> analysis.isPreset() && analysis.getStatType() == CcResultType.DISTRIBUTION);
        }

        private CohortExpressionQueryBuilder.BuildExpressionQueryOptions createDefaultOptions(final Integer id) {
            final CohortExpressionQueryBuilder.BuildExpressionQueryOptions options = new CohortExpressionQueryBuilder.BuildExpressionQueryOptions();
            options.cdmSchema = source.getTableQualifier(SourceDaimon.DaimonType.CDM);
            options.resultSchema = source.getTableQualifier(SourceDaimon.DaimonType.Results);
            options.cohortId = id;
            return options;
        }

        private List<String> getCohortWithCriteriaQueries(CohortDefinition cohortDefinition, FeAnalysisWithCriteriaEntity analysis, FeAnalysisCriteriaEntity feature) {

            CohortExpressionBuilder builder = new CohortExpressionBuilder(cohortDefinition, feature);
            CohortExpressionQueryBuilder.BuildExpressionQueryOptions options = createDefaultOptions(cohortDefinition.getId());
            options.generateStats = true;
            String targetTable = "cohort_" + SessionUtils.sessionId();
            options.targetTable = options.resultSchema + "." + targetTable;
            List<String> queries = new ArrayList<>();
            CriteriaGroup expression = feature.getExpression();

            String createCohortSql = SqlRender.renderSql(CREATE_COHORT_SQL,
                    new String[]{ RESULTS_DATABASE_SCHEMA, TARGET_TABLE },
                    new String[] { source.getTableQualifier(SourceDaimon.DaimonType.Results), targetTable });

            String exprQuery = queryBuilder.buildExpressionQuery(builder.withCriteria(expression), options);
            String statsQuery = getCriteriaStatsQuery(cohortDefinition, analysis, feature, targetTable);
            String dropTableSql = SqlRender.renderSql(DROP_TABLE_SQL, new String[]{ RESULTS_DATABASE_SCHEMA, TARGET_TABLE },
                    new String[] { source.getTableQualifier(SourceDaimon.DaimonType.Results), targetTable });
            queries.add(createCohortSql);
            queries.add(exprQuery);
            queries.add(statsQuery);
            queries.add(dropTableSql);

            return queries;
        }

        private String getCriteriaStatsQuery(CohortDefinition cohortDefinition, FeAnalysisWithCriteriaEntity analysis, FeAnalysisCriteriaEntity feature, String targetTable) {
            Long conceptId = 0L;
            if (feature.getExpression().demographicCriteriaList.length > 0 && feature.getExpression().demographicCriteriaList[0].gender.length > 0) {
                conceptId = feature.getExpression().demographicCriteriaList[0].gender[0].conceptId;
            }
            String resultSchema = source.getTableQualifier(SourceDaimon.DaimonType.Results);
            return SqlRender.renderSql(COHORT_STATS_QUERY,
                    new String[]{ RESULTS_DATABASE_SCHEMA, "cohortId", "executionId", "analysisId", "analysisName", "covariateName", "conceptId",
                            "covariateId", "targetTable", "totalsTable" },
                    new String[]{ source.getTableQualifier(SourceDaimon.DaimonType.Results), String.valueOf(cohortDefinition.getId()),
                        String.valueOf(jobId), String.valueOf(analysis.getId()), analysis.getName(), feature.getName(), String.valueOf(conceptId),
                            String.valueOf(feature.getId()), resultSchema + "." + targetTable, resultSchema + "." + cohortTable }
                    );
        }

        private List<String> getCohortWithCriteriaFeaturesQueries(CohortDefinition cohortDefinition, FeAnalysisWithCriteriaEntity analysis) {

            return analysis.getDesign().stream().map(feature -> getCohortWithCriteriaQueries(cohortDefinition, analysis, feature))
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());
        }

        private String[] getSqlQueriesToRun(final JSONObject jsonObject, final Integer cohortDefinitionId) {
            final StringJoiner joiner = new StringJoiner("\n\n");

            joiner.add(jsonObject.getString("sqlConstruction"));

            getQueriesForPresetAnalyses(jsonObject,cohortDefinitionId).forEach(joiner::add);
            getQueriesForCustomDistributionAnalyses(cohortDefinitionId).forEach(joiner::add);
            getQueriesForCustomPrevalenceAnalyses(cohortDefinitionId).forEach(joiner::add);
            getQueriesForCriteriaAnalyses(cohortDefinitionId).forEach(joiner::add);

            joiner.add(jsonObject.getString("sqlCleanup"));

            final String sql = SqlRender.renderSql(joiner.toString(),
                    new String[]{RESULTS_DATABASE_SCHEMA, CDM_DATABASE_SCHEMA, VOCABULARY_DATABASE_SCHEMA},
                    new String[]{source.getTableQualifier(SourceDaimon.DaimonType.Results), source.getTableQualifier(SourceDaimon.DaimonType.CDM),
                        source.getTableQualifier(SourceDaimon.DaimonType.Vocabulary)});
            final String translatedSql = SqlTranslate.translateSql(sql, source.getSourceDialect(), SessionUtils.sessionId(), null);
            return SqlSplit.splitSql(translatedSql);
        }

        private JSONObject createFeJsonObject(final CohortExpressionQueryBuilder.BuildExpressionQueryOptions options) {
            FeatureExtraction.init(null);
            String settings = buildSettings();
            String sqlJson = FeatureExtraction.createSql(settings, true, options.resultSchema + "." + cohortTable,
                    "subject_id", options.cohortId, options.cdmSchema);
            return new JSONObject(sqlJson);
        }
        
        private String buildSettings() {

            final JSONObject defaultSettings = new JSONObject(FeatureExtraction.getDefaultPrespecAnalyses());

            feAnalysisService.findAllPresetAnalyses().forEach(v -> defaultSettings.remove(v.getDesign()));
            
            cohortCharacterization.getParameters().forEach(param -> defaultSettings.put(param.getName(), param.getValue()));
            cohortCharacterization.getFeatureAnalyses()
                    .stream()
                    .filter(FeAnalysisEntity::isPreset)
                    .forEach(analysis -> defaultSettings.put(((FeAnalysisWithStringEntity) analysis).getDesign(), Boolean.TRUE));
            
            return defaultSettings.toString();
        }

    }
}
