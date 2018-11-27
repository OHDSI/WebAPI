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

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.collections.impl.factory.Lists;
import org.json.JSONObject;
import org.ohdsi.analysis.cohortcharacterization.design.StandardFeatureAnalysisType;
import org.ohdsi.circe.cohortdefinition.*;
import org.ohdsi.circe.helper.ResourceHelper;
import org.ohdsi.featureExtraction.FeatureExtraction;
import org.ohdsi.sql.SqlRender;
import org.ohdsi.sql.SqlSplit;
import org.ohdsi.webapi.cohortcharacterization.converter.SerializedCcToCcConverter;
import org.ohdsi.webapi.cohortcharacterization.domain.CcParamEntity;
import org.ohdsi.webapi.cohortcharacterization.domain.CcStrataEntity;
import org.ohdsi.webapi.cohortcharacterization.domain.CohortCharacterizationEntity;
import org.ohdsi.webapi.cohortcharacterization.repository.AnalysisGenerationInfoEntityRepository;
import org.ohdsi.webapi.common.generation.AnalysisTasklet;
import org.ohdsi.webapi.feanalysis.FeAnalysisService;
import org.ohdsi.webapi.feanalysis.domain.*;
import org.ohdsi.webapi.service.SourceService;
import org.ohdsi.webapi.shiro.Entities.UserEntity;
import org.ohdsi.webapi.shiro.Entities.UserRepository;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.util.*;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.SqlProvider;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.ohdsi.webapi.Constants.Params.*;

public class GenerateCohortCharacterizationTasklet extends AnalysisTasklet {
    private static final String[] CUSTOM_PARAMETERS = {"analysisId", "analysisName", "cohortId", "jobId", "design"};
    private static final String[] RETRIEVING_PARAMETERS = {"features", "featureRefs", "analysisRefs", "cohortId", "executionId"};
    private static final String[] DAIMONS = {RESULTS_DATABASE_SCHEMA, CDM_DATABASE_SCHEMA, TEMP_DATABASE_SCHEMA, VOCABULARY_DATABASE_SCHEMA};

    private static final String COHORT_STATS_QUERY = ResourceHelper.GetResourceAsString("/resources/cohortcharacterizations/sql/prevalenceWithCriteria.sql");
    private static final String COHORT_DIST_QUERY = ResourceHelper.GetResourceAsString("/resources/cohortcharacterizations/sql/distributionWithCriteria.sql");

    private static final String COHORT_STRATA_QUERY = ResourceHelper.GetResourceAsString("/resources/cohortcharacterizations/sql/strataWithCriteria.sql");

    private static final String[] CRITERIA_REGEXES = new String[] { "groupQuery", "indexId", "targetTable", "totalsTable" };
    private static final String[] STRATA_REGEXES = new String[] { "strataQuery", "indexId", "targetTable", "strataCohortTable" };

    private static final Collection<String> CRITERIA_PARAM_NAMES = ImmutableList.<String>builder()
            .add("cohortId", "executionId", "analysisId", "analysisName", "covariateName", "conceptId", "covariateId", "strataId", "strataName")
            .build();

    private static final Collection<String> STRATA_PARAM_NAMES = ImmutableList.<String>builder()
            .add("cohortId")
            .add("strataId")
            .build();

    private static final Function<String, String> COMPLETE_DOTCOMMA = s -> s.trim().endsWith(";") ? s : s + ";";

    private final CcService ccService;
    private final FeAnalysisService feAnalysisService;
    private final SourceService sourceService;
    private final UserRepository userRepository;
    private final CohortExpressionQueryBuilder queryBuilder;

    public GenerateCohortCharacterizationTasklet(
            final CancelableJdbcTemplate jdbcTemplate,
            final TransactionTemplate transactionTemplate,
            final CcService ccService,
            final FeAnalysisService feAnalysisService,
            final AnalysisGenerationInfoEntityRepository analysisGenerationInfoEntityRepository,
            final SourceService sourceService,
            final UserRepository userRepository
    ) {
        super(LoggerFactory.getLogger(GenerateCohortCharacterizationTasklet.class), jdbcTemplate, transactionTemplate, analysisGenerationInfoEntityRepository);
        this.ccService = ccService;
        this.feAnalysisService = feAnalysisService;
        this.sourceService = sourceService;
        this.userRepository = userRepository;
        this.queryBuilder = new CohortExpressionQueryBuilder();
    }

    @Override
    protected void doBefore(ChunkContext chunkContext) {
        initTx();
    }

    @Override
    protected List<PreparedStatementCreator> prepareStatementCreators(ChunkContext chunkContext, CancelableJdbcTemplate jdbcTemplate) {
        return new CcTask(chunkContext).run();
    }

    private void initTx() {
        DefaultTransactionDefinition txDefinition = new DefaultTransactionDefinition();
        txDefinition.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        TransactionStatus initStatus = this.transactionTemplate.getTransactionManager().getTransaction(txDefinition);
        this.transactionTemplate.getTransactionManager().commit(initStatus);
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
        final String sessionId;

        private final Long jobId;
        private final Integer sourceId;

        CcTask(final ChunkContext context) {
            Map<String, Object> jobParams = context.getStepContext().getJobParameters();
            this.cohortCharacterization = ccService.findByIdWithLinkedEntities(
                    Long.valueOf(jobParams.get(COHORT_CHARACTERIZATION_ID).toString())
            );
            this.jobId = context.getStepContext().getStepExecution().getJobExecution().getId();
            sourceId = Integer.valueOf(jobParams.get(SOURCE_ID).toString());
            this.source = sourceService.findBySourceId(sourceId);
            this.cohortTable = jobParams.get(TARGET_TABLE).toString();
            this.userEntity = userRepository.findByLogin(jobParams.get(JOB_AUTHOR).toString());
            this.sessionId = SessionUtils.sessionId();
        }
        
        private List<PreparedStatementCreator> run() {

            saveInfo(jobId, new SerializedCcToCcConverter().convertToDatabaseColumn(cohortCharacterization), userEntity);
            return cohortCharacterization.getCohortDefinitions()
                    .stream()
                    .map(def -> getAnalysisQueriesOnCohort(def.getId()))
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());
        }

        private List<PreparedStatementCreator> getAnalysisQueriesOnCohort(final Integer cohortDefinitionId) {

            final CohortExpressionQueryBuilder.BuildExpressionQueryOptions options = createDefaultOptions(cohortDefinitionId);
            return getSqlQueriesToRun(createFeJsonObject(options, options.resultSchema + "." + cohortTable), cohortDefinitionId);
        }

        private String renderCustomAnalysisDesign(FeAnalysisWithStringEntity fa, Integer cohortId) {
            Map<String, String> params = cohortCharacterization.getParameters().stream().collect(Collectors.toMap(CcParamEntity::getName, CcParamEntity::getValue));
            params.put("cdm_database_schema", SourceUtils.getCdmQualifier(source));
            params.put("cohort_table", SourceUtils.getTempQualifier(source) + "." + cohortTable);
            params.put("cohort_id", cohortId.toString());
            params.put("analysis_id", fa.getId().toString());

            return SqlRender.renderSql(
                    fa.getDesign(),
                    params.keySet().toArray(new String[params.size()]),
                    params.values().toArray(new String[params.size()])
            );
        }

        private List<PreparedStatementCreator> getQueriesForCustomDistributionAnalyses(final Integer cohortId) {
            return cohortCharacterization.getFeatureAnalyses()
                    .stream()
                    .filter(FeAnalysisEntity::isCustom)
                    .filter(v -> v.getStatType() == CcResultType.DISTRIBUTION)
                    .map(v -> prepareStatement(customDistributionQueryWrapper, sessionId,
                            CUSTOM_PARAMETERS,
                            new String[] { String.valueOf(v.getId()), org.springframework.util.StringUtils.quote(v.getName()), String.valueOf(cohortId), String.valueOf(jobId), renderCustomAnalysisDesign((FeAnalysisWithStringEntity) v, cohortId)}))
                    .collect(Collectors.toList());
        }
        
        private List<PreparedStatementCreator> getQueriesForCustomPrevalenceAnalyses(final Integer cohortId) {
            return cohortCharacterization.getFeatureAnalyses()
                    .stream()
                    .filter(FeAnalysisEntity::isCustom)
                    .filter(v -> v.getStatType() == CcResultType.PREVALENCE)
                    .map(v -> prepareStatement(customPrevalenceQueryWrapper, sessionId,
                            CUSTOM_PARAMETERS,
                            new String[] { String.valueOf(v.getId()), org.springframework.util.StringUtils.quote(v.getName()), String.valueOf(cohortId), String.valueOf(jobId), renderCustomAnalysisDesign((FeAnalysisWithStringEntity) v, cohortId)}))
                    .collect(Collectors.toList());
        }

        private List<PreparedStatementCreator> getQueriesForCriteriaAnalyses(Integer cohortDefinitionId, CcStrataEntity strata) {
            List<PreparedStatementCreator> queries = new ArrayList<>();
            List<FeAnalysisWithCriteriaEntity> analysesWithCriteria = getFeAnalysesWithCriteria();
            if (!analysesWithCriteria.isEmpty()) {
                String cohortTable = Objects.nonNull(strata) ? getStrataCohortTable(strata) : SourceUtils.getTempQualifier(source) + "." + this.cohortTable;
                analysesWithCriteria.stream()
                        .map(analysis -> getCriteriaFeaturesQueries(cohortDefinitionId, analysis, cohortTable, strata))
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

        private List<PreparedStatementCreator> getQueriesForPresetAnalyses(final JSONObject jsonObject, final Integer cohortId, final CcStrataEntity strata) {
            final String cohortWrapper = "select %1$d as %2$s from (%3$s) W";

            final String featureRefColumns = "cohort_definition_id, covariate_id, covariate_name, analysis_id, concept_id";
            final String featureRefs = String.format(cohortWrapper, cohortId, featureRefColumns,
                    StringUtils.stripEnd(jsonObject.getString("sqlQueryFeatureRef"), ";"));

            final String analysisRefColumns = "cohort_definition_id, CAST(analysis_id AS INT) analysis_id, analysis_name, domain_id, start_day, end_day, CAST(is_binary AS CHAR(1)) is_binary,CAST(missing_means_zero AS CHAR(1)) missing_means_zero";
            final String analysisRefs = String.format(cohortWrapper, cohortId, analysisRefColumns,
                    StringUtils.stripEnd(jsonObject.getString("sqlQueryAnalysisRef"), ";"));

            final List<PreparedStatementCreator> queries = new ArrayList<>();

            Long strataId = Objects.nonNull(strata) ? strata.getId() : null;
            String strataName = Objects.nonNull(strata) ? strata.getName() : null;

            String[] sqlParamNames = new String[]{ "strataId", "strataName" };
            Object[] sqlParamVars = new Object[]{ strataId, strataName };

            if (ccHasPresetDistributionAnalyses()) {
                final String distColumns = "cohort_definition_id, covariate_id, count_value, min_value, max_value, average_value, "
                        + "standard_deviation, median_value, p10_value, p25_value, p75_value, p90_value";
                final String distFeatures = String.format(cohortWrapper, cohortId, distColumns,
                        StringUtils.stripEnd(jsonObject.getString("sqlQueryContinuousFeatures"), ";"));
                queries.add(prepareStatement(distributionRetrievingQuery, sessionId, RETRIEVING_PARAMETERS,
                        new String[] { distFeatures, featureRefs, analysisRefs, String.valueOf(cohortId), String.valueOf(jobId) }, sqlParamNames, sqlParamVars));
            }
            if (ccHasPresetPrevalenceAnalyses()) {
                final String featureColumns = "cohort_definition_id, covariate_id, sum_value, average_value";
                final String features = String.format(cohortWrapper, cohortId, featureColumns,
                        StringUtils.stripEnd(jsonObject.getString("sqlQueryFeatures"), ";"));
                String[] paramValues = new String[]{ features, featureRefs, analysisRefs, String.valueOf(cohortId), String.valueOf(jobId) };
                queries.add(prepareStatement(prevalenceRetrievingQuery, sessionId, RETRIEVING_PARAMETERS, paramValues, sqlParamNames, sqlParamVars));
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
            options.cdmSchema = SourceUtils.getCdmQualifier(source);
            // Target schema
            options.resultSchema = SourceUtils.getTempQualifier(source);
            options.cohortId = id;
            options.generateStats = false;
            return options;
        }

        private List<PreparedStatementCreator> getCriteriaFeatureQuery(Integer cohortDefinitionId, FeAnalysisWithCriteriaEntity analysis, FeAnalysisCriteriaEntity feature, String targetTable, CcStrataEntity strata) {

            Long conceptId = 0L;
            String queryFile;
            String groupQuery = getCriteriaGroupQuery(analysis, feature);
            String[] paramNames = CRITERIA_PARAM_NAMES.toArray(new String[0]);

            if (CcResultType.PREVALENCE.equals(analysis.getStatType())) {
                queryFile = COHORT_STATS_QUERY;
            } else if (CcResultType.DISTRIBUTION.equals(analysis.getStatType())) {
                queryFile = COHORT_DIST_QUERY;
            } else {
                throw new IllegalArgumentException(String.format("Stat type %s is not supported", analysis.getStatType()));
            }
            Long strataId = Objects.nonNull(strata) ? strata.getId() : 0L;
            String strataName = Objects.nonNull(strata) ? strata.getName() : null;
            Collection<Object> paramValues = Lists.mutable.with(cohortDefinitionId, jobId, analysis.getId(), analysis.getName(), feature.getName(), conceptId,
                    feature.getId(), strataId, strataName);
            String[] criteriaValues = new String[]{ groupQuery, "0", targetTable, cohortTable };

            return Arrays.stream(SqlSplit.splitSql(queryFile))
                    .map(COMPLETE_DOTCOMMA)
                    .map(sql -> prepareStatement(sql, sessionId, CRITERIA_REGEXES, criteriaValues,
                            paramNames, paramValues.toArray(new Object[0])))
                    .collect(Collectors.toList());
        }

        private String getCriteriaGroupQuery(FeAnalysisWithCriteriaEntity analysis, FeAnalysisCriteriaEntity feature) {
            String groupQuery;
            if (CcResultType.PREVALENCE.equals(analysis.getStatType())) {
              groupQuery = queryBuilder.getCriteriaGroupQuery(((FeAnalysisCriteriaGroupEntity)feature).getExpression(), "#qualified_events");
            } else if (CcResultType.DISTRIBUTION.equals(analysis.getStatType())) {
              if (feature instanceof FeAnalysisWindowedCriteriaEntity) {
                WindowedCriteria criteria = ((FeAnalysisWindowedCriteriaEntity) feature).getExpression();
                criteria.ignoreObservationPeriod = true;
                groupQuery = queryBuilder.getWindowedCriteriaQuery(criteria, "#qualified_events");
              } else if (feature instanceof FeAnalysisDemographicCriteriaEntity) {
                DemographicCriteria criteria = ((FeAnalysisDemographicCriteriaEntity)feature).getExpression();
                groupQuery = queryBuilder.getDemographicCriteriaQuery(criteria, "#qualified_events");
              } else {
                throw new IllegalArgumentException(String.format("Feature class %s is not supported", feature.getClass()));
              }
            } else {
              throw new IllegalArgumentException(String.format("Stat type %s is not supported", analysis.getStatType()));
            }
            return groupQuery;
        }

        private List<PreparedStatementCreator> getQueriesForStratifiedCriteriaAnalyses(Integer cohortDefinitionId) {

            List<PreparedStatementCreator> queriesToRun = new ArrayList<>();
            queriesToRun.addAll(getCodesetQuery(cohortCharacterization.getConceptSets()));

            //Generate stratified cohorts
            queriesToRun.addAll(cohortCharacterization.getStratas().stream()
                    .flatMap(strata -> getStrataQuery(cohortDefinitionId, strata).stream())
                    .collect(Collectors.toList()));

            queriesToRun.add(prepareStatement("DROP TABLE #Codesets;", sessionId));

            //Extract features from stratified cohorts
            queriesToRun.addAll(cohortCharacterization.getStratas().stream()
                    .flatMap(strata -> {
                      JSONObject jsonObject = createFeJsonObject(createDefaultOptions(cohortDefinitionId), getStrataCohortTable(strata));
                      List<PreparedStatementCreator> queries = new ArrayList<>();
                      queries.addAll(getCreateQueries(jsonObject));
                      queries.addAll(getFeatureAnalysesQueries(jsonObject, cohortDefinitionId, strata));
                      queries.addAll(getCleanupQueries(jsonObject));
                      return queries.stream();
                    })
                    .collect(Collectors.toList()));

            //Cleanup stratified cohorts tables
            queriesToRun.addAll(cohortCharacterization.getStratas().stream()
                    .map(strata -> prepareStatement("DROP TABLE " + getStrataCohortTable(strata) + ";", sessionId))
                    .collect(Collectors.toList()));

            return queriesToRun;
        }

        private List<PreparedStatementCreator> getStrataQuery(Integer cohortDefinitionId, CcStrataEntity strata) {
            List<PreparedStatementCreator> queries = new ArrayList<>();
            String strataQuery = queryBuilder.getCriteriaGroupQuery(strata.getCriteria(), "#qualified_events");
            String[] paramNames = STRATA_PARAM_NAMES.toArray(new String[0]);
            String[] replacements = new String[]{ strataQuery, "0", cohortTable, getStrataCohortTable(strata) };
            Object[] paramValues = new Object[]{ cohortDefinitionId, strata.getId() };
            queries.add(prepareStatement("CREATE TABLE " + getStrataCohortTable(strata)
                    + "(cohort_definition_id INTEGER, strata_id BIGINT, subject_id BIGINT, cohort_start_date DATE, cohort_end_date DATE);", sessionId));
            queries.add(prepareStatement(COHORT_STRATA_QUERY, sessionId, STRATA_REGEXES, replacements, paramNames, paramValues));
            return queries;
        }

        private String getStrataCohortTable(CcStrataEntity strata) {

          return "#strata_cohort_" + strata.getId();
        }

        private List<PreparedStatementCreator> getCodesetQuery(Collection<ConceptSet> conceptSets) {

            String codesetQuery = queryBuilder.getCodesetQuery(conceptSets.toArray(new ConceptSet[0]));
            return Arrays.stream(SqlSplit.splitSql(codesetQuery))
                    .map(sql -> prepareStatement(sql, sessionId))
                    .collect(Collectors.toList());
        }

        private List<PreparedStatementCreator> getCriteriaFeaturesQueries(Integer cohortDefinitionId, FeAnalysisWithCriteriaEntity<?> analysis, String targetTable, CcStrataEntity strata) {

            List<PreparedStatementCreator> queriesToRun = new ArrayList<>();
            queriesToRun.addAll(getCodesetQuery(analysis.getConceptSets()));

            queriesToRun.addAll(analysis.getDesign().stream()
                    .map(feature -> getCriteriaFeatureQuery(cohortDefinitionId, analysis, feature, targetTable, strata))
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList())); // statistics queries
            queriesToRun.add(prepareStatement("DROP TABLE #Codesets;", sessionId));
            return queriesToRun;
        }

        private PreparedStatementRendererCreator prepareStatement(String query, String sessionId) {

          return prepareStatement(query, sessionId, new String[0], new String[0]);
        }

        private PreparedStatementRendererCreator prepareStatement(String query, String sessionId, String[] regexes, String[] variables) {

            return prepareStatement(query, sessionId, regexes, variables, new String[0], new Object[0]);
        }

        private PreparedStatementRendererCreator prepareStatement(String query, String sessionId, String[] regexes, String[] variables, String[] paramNames, Object[] paramValues) {
            final String resultsQualifier = SourceUtils.getResultsQualifier(source);
            final String cdmQualifier = SourceUtils.getCdmQualifier(source);
            final String tempQualifier = SourceUtils.getTempQualifier(source, resultsQualifier);
            final String vocabularyQualifier = SourceUtils.getVocabularyQualifier(source);
            final String[] tmpRegexes = ArrayUtils.addAll(regexes, DAIMONS);
            final String[] tmpValues = ArrayUtils.addAll(variables, resultsQualifier, cdmQualifier, tempQualifier, vocabularyQualifier);
            final String sql = SqlRender.renderSql(query, tmpRegexes, tmpValues);
            return new PreparedStatementRendererCreator(
                    new PreparedStatementRenderer(
                            source,
                            sql,
                            tmpRegexes,
                            tmpValues,
                            paramNames,
                            paramValues,
                            sessionId
                    ));
        }

        private List<PreparedStatementCreator> getSqlQueriesToRun(final JSONObject jsonObject, final Integer cohortDefinitionId) {
            List<PreparedStatementCreator> queriesToRun = new LinkedList<>();

            queriesToRun.addAll(getCreateQueries(jsonObject));

            queriesToRun.addAll(getFeatureAnalysesQueries(jsonObject, cohortDefinitionId, null));

            queriesToRun.addAll(getCleanupQueries(jsonObject));

            if (!cohortCharacterization.getStratas().isEmpty()) {
              queriesToRun.addAll(getQueriesForStratifiedCriteriaAnalyses(cohortDefinitionId));
            }

            String sql = queriesToRun.stream().map(q -> ((SqlProvider)q).getSql()).collect(Collectors.joining("\n"));
            log.debug(sql);

            return queriesToRun;
        }

        private List<PreparedStatementCreator> getCreateQueries(final JSONObject jsonObject) {

            return Arrays.stream(SqlSplit.splitSql(jsonObject.getString("sqlConstruction")))
                    .map(COMPLETE_DOTCOMMA)
                    .map(sql -> prepareStatement(sql, sessionId))
                    .collect(Collectors.toList());
        }

        private List<PreparedStatementCreator> getCleanupQueries(final JSONObject jsonObject) {

            return Arrays.stream(SqlSplit.splitSql(jsonObject.getString("sqlCleanup")))
                    .map(COMPLETE_DOTCOMMA)
                    .map(sql -> prepareStatement(sql, sessionId))
                    .collect(Collectors.toList());
        }

        private List<PreparedStatementCreator> getFeatureAnalysesQueries(final JSONObject jsonObject, final Integer cohortDefinitionId, final CcStrataEntity strata) {

            List<PreparedStatementCreator> queriesToRun = new ArrayList<>();
            queriesToRun.addAll(getQueriesForPresetAnalyses(jsonObject,cohortDefinitionId, strata));
            queriesToRun.addAll(getQueriesForCustomDistributionAnalyses(cohortDefinitionId));
            queriesToRun.addAll(getQueriesForCustomPrevalenceAnalyses(cohortDefinitionId));
            queriesToRun.addAll(getQueriesForCriteriaAnalyses(cohortDefinitionId, strata));
            return queriesToRun;
        }

         private JSONObject createFeJsonObject(final CohortExpressionQueryBuilder.BuildExpressionQueryOptions options, final String cohortTable) {
            FeatureExtraction.init(null);
            String settings = buildSettings();
            String sqlJson = FeatureExtraction.createSql(settings, true, cohortTable,
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
