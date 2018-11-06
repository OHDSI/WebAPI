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
import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.eclipse.collections.impl.factory.Lists;
import org.json.JSONObject;
import org.ohdsi.analysis.Utils;
import org.ohdsi.analysis.cohortcharacterization.design.StandardFeatureAnalysisDomain;
import org.ohdsi.analysis.cohortcharacterization.design.StandardFeatureAnalysisType;
import org.ohdsi.circe.cohortdefinition.CohortExpression;
import org.ohdsi.circe.cohortdefinition.CohortExpressionQueryBuilder;
import org.ohdsi.circe.cohortdefinition.ConceptSet;
import org.ohdsi.circe.cohortdefinition.Criteria;
import org.ohdsi.circe.cohortdefinition.CriteriaGroup;
import org.ohdsi.circe.cohortdefinition.InclusionRule;
import org.ohdsi.circe.cohortdefinition.ObservationPeriod;
import org.ohdsi.circe.cohortdefinition.PayerPlanPeriod;
import org.ohdsi.circe.helper.ResourceHelper;
import org.ohdsi.featureExtraction.FeatureExtraction;
import org.ohdsi.sql.SqlRender;
import org.ohdsi.sql.SqlSplit;
import org.ohdsi.sql.SqlTranslate;
import org.ohdsi.webapi.Constants;
import org.ohdsi.webapi.cohortcharacterization.converter.SerializedCcToCcConverter;
import org.ohdsi.webapi.cohortcharacterization.domain.CcParamEntity;
import org.ohdsi.webapi.cohortcharacterization.domain.CohortCharacterizationEntity;
import org.ohdsi.webapi.cohortcharacterization.repository.AnalysisGenerationInfoEntityRepository;
import org.ohdsi.webapi.cohortdefinition.CohortDefinition;
import org.ohdsi.webapi.common.generation.AnalysisTasklet;
import org.ohdsi.webapi.feanalysis.FeAnalysisService;
import org.ohdsi.webapi.feanalysis.domain.FeAnalysisCriteriaEntity;
import org.ohdsi.webapi.feanalysis.domain.FeAnalysisEntity;
import org.ohdsi.webapi.feanalysis.domain.FeAnalysisWithCriteriaEntity;
import org.ohdsi.webapi.feanalysis.domain.FeAnalysisWithStringEntity;
import org.ohdsi.webapi.service.SourceService;
import org.ohdsi.webapi.shiro.Entities.UserEntity;
import org.ohdsi.webapi.shiro.Entities.UserRepository;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.sqlrender.SourceAwareSqlRender;
import org.ohdsi.webapi.util.CancelableJdbcTemplate;
import org.ohdsi.webapi.util.SessionUtils;
import org.ohdsi.webapi.util.SourceUtils;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.ohdsi.webapi.Constants.Params.*;

public class GenerateCohortCharacterizationTasklet extends AnalysisTasklet {
    private static final String[] CUSTOM_PARAMETERS = {"analysisId", "analysisName", "cohortId", "jobId", "design"};
    private static final String[] RETRIEVING_PARAMETERS = {"features", "featureRefs", "analysisRefs", "cohortId", "executionId"};

    private static final String COHORT_STATS_QUERY = ResourceHelper.GetResourceAsString("/resources/cohortcharacterizations/sql/prevalenceWithCriteria.sql");
    private static final String COHORT_DIST_QUERY = ResourceHelper.GetResourceAsString("/resources/cohortcharacterizations/sql/distributionWithCriteria.sql");
    private static final String CREATE_COHORT_SQL = ResourceHelper.GetResourceAsString("/resources/cohortcharacterizations/sql/createCohortTable.sql");
    private static final String DROP_TABLE_SQL = ResourceHelper.GetResourceAsString("/resources/cohortcharacterizations/sql/dropCohortTable.sql");

    private static final Collection<String> PREVALENCE_PARAM_NAMES = ImmutableList.<String>builder()
            .add("groupQuery", "indexId", "cohortId", "executionId", "analysisId", "analysisName", "covariateName", "conceptId", "covariateId", "targetTable", "totalsTable")
            .build();
    private static final Collection<String> DISTRIBUTION_PARAM_NAMES = ImmutableList.<String>builder()
            .addAll(PREVALENCE_PARAM_NAMES)
            .add("domain_table")
            .add("domain_id_field")
            .add("domain_start_date")
            .add("domain_end_date")
            .add("events_criteria")
            .add("indexId")
            .build();

    private final CcService ccService;
    private final FeAnalysisService feAnalysisService;
    private final SourceService sourceService;
    private final UserRepository userRepository;
    private final CohortExpressionQueryBuilder queryBuilder;
    private final SourceAwareSqlRender sourceAwareSqlRender;

    public GenerateCohortCharacterizationTasklet(
            final CancelableJdbcTemplate jdbcTemplate,
            final TransactionTemplate transactionTemplate,
            final CcService ccService,
            final FeAnalysisService feAnalysisService,
            final AnalysisGenerationInfoEntityRepository analysisGenerationInfoEntityRepository,
            final SourceService sourceService,
            final UserRepository userRepository,
            final SourceAwareSqlRender sourceAwareSqlRender
    ) {
        super(LoggerFactory.getLogger(GenerateCohortCharacterizationTasklet.class), jdbcTemplate, transactionTemplate, analysisGenerationInfoEntityRepository);
        this.ccService = ccService;
        this.feAnalysisService = feAnalysisService;
        this.sourceService = sourceService;
        this.userRepository = userRepository;
        this.sourceAwareSqlRender = sourceAwareSqlRender;
        this.queryBuilder = new CohortExpressionQueryBuilder();
    }

    @Override
    protected void doBefore(ChunkContext chunkContext) {
        initTx();
    }

    @Override
    protected String[] prepareQueries(ChunkContext chunkContext, CancelableJdbcTemplate jdbcTemplate) {
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
        }
        
        private String[] run() {

            saveInfo(jobId, new SerializedCcToCcConverter().convertToDatabaseColumn(cohortCharacterization), userEntity);
            return cohortCharacterization.getCohortDefinitions()
                    .stream()
                    .map(def -> getAnalysisQueriesOnCohort(def.getId()))
                    .flatMap(Arrays::stream)
                    .toArray(String[]::new);
        }

        private String[] getAnalysisQueriesOnCohort(final Integer cohortDefinitionId) {

            return getSqlQueriesToRun(createFeJsonObject(createDefaultOptions(cohortDefinitionId)), cohortDefinitionId);
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
            options.cdmSchema = SourceUtils.getCdmQualifier(source);
            // Target schema
            options.resultSchema = SourceUtils.getTempQualifier(source);
            options.cohortId = id;
            return options;
        }

        private String getStatQueryFile(FeAnalysisWithCriteriaEntity analysis) {
            String result;
            switch (analysis.getStatType()) {
                case PREVALENCE:
                    result = COHORT_STATS_QUERY;
                    break;
                case DISTRIBUTION:
                    result = COHORT_DIST_QUERY;
                    break;
                default:
                    throw new IllegalArgumentException(String.format("Stat type %s is not supported", analysis.getStatType()));
            }
            return result;
        }

        private Supplier<? extends RuntimeException> raiseDomainNotSupported(StandardFeatureAnalysisDomain domain) {
            return () -> new IllegalArgumentException(String.format("Domain %s is not supported", domain));
        }

        private Constants.DomainMetadata getDomainMetadata(StandardFeatureAnalysisDomain domain) {

            return Optional.ofNullable(Constants.DOMAIN_METADATA.get(domain))
                    .orElseThrow(raiseDomainNotSupported(domain));
        }

        private String getCriteriaStatsQuery(String queryFile, CohortDefinition cohortDefinition, FeAnalysisWithCriteriaEntity analysis, FeAnalysisCriteriaEntity feature, String targetTable) {

            Long conceptId = 0L;
            String[] paramNames = (CcResultType.PREVALENCE.equals(analysis.getStatType()) ? PREVALENCE_PARAM_NAMES : DISTRIBUTION_PARAM_NAMES).toArray(new String[0]);
            String groupQuery = queryBuilder.getCriteriaGroupQuery(feature.getExpression(), "#qualified_events");
            Collection<String> paramValues = Lists.mutable.with(groupQuery, "0", String.valueOf(cohortDefinition.getId()),
                    String.valueOf(jobId), String.valueOf(analysis.getId()), analysis.getName(), feature.getName(), String.valueOf(conceptId),
                    String.valueOf(feature.getId()), targetTable, cohortTable);
            if (CcResultType.DISTRIBUTION.equals(analysis.getStatType())) {
                Constants.DomainMetadata domainMetadata = getDomainMetadata(analysis.getDomain());
                String domainTable = domainMetadata.getTableName();
                String domainIdField = domainMetadata.getIdField();
                paramValues.add(domainTable);
                paramValues.add(domainIdField);
                paramValues.add(domainMetadata.getStartDateField());
                paramValues.add(domainMetadata.getEndDateField());
                paramValues.add(queryBuilder.getCriteriaGroupQuery(feature.getExpression(), "#people_events"));
                paramValues.add("0");
            }
            return sourceAwareSqlRender.renderSql(sourceId, queryFile, paramNames, paramValues.toArray(new String[0]));
        }

        private List<String> getCohortWithCriteriaFeaturesQueries(CohortDefinition cohortDefinition, FeAnalysisWithCriteriaEntity analysis) {

            final String targetTable = "cohort_" + SessionUtils.sessionId();
            CohortExpressionQueryBuilder.BuildExpressionQueryOptions options = createDefaultOptions(cohortDefinition.getId());
            options.generateStats = true;
            options.targetTable = options.resultSchema + "." + targetTable;
            List<String> queries = new ArrayList<>();
            String createCohortSql = sourceAwareSqlRender.renderSql(sourceId, CREATE_COHORT_SQL, TARGET_TABLE, targetTable);
            String exprQuery = queryBuilder.buildExpressionQuery(cohortDefinition.getExpression(), options);
            String codesetQuery = queryBuilder.getCodesetQuery(analysis.getConceptSets().toArray(new ConceptSet[0]));
            String dropTableSql = sourceAwareSqlRender.renderSql(sourceId, DROP_TABLE_SQL, TARGET_TABLE, targetTable);

            queries.add(createCohortSql);
            queries.add(exprQuery);
            queries.add(codesetQuery);
            queries.addAll(analysis.getDesign().stream()
                    .map(feature -> getCriteriaStatsQuery(getStatQueryFile(analysis), cohortDefinition, analysis, feature, targetTable))
                    .collect(Collectors.toList())); // statistics queries
            queries.add(dropTableSql);
            return queries;
        }

        private String[] getSqlQueriesToRun(final JSONObject jsonObject, final Integer cohortDefinitionId) {
            final StringJoiner joiner = new StringJoiner("\n\n");

            joiner.add(jsonObject.getString("sqlConstruction"));

            getQueriesForPresetAnalyses(jsonObject,cohortDefinitionId).forEach(joiner::add);
            getQueriesForCustomDistributionAnalyses(cohortDefinitionId).forEach(joiner::add);
            getQueriesForCustomPrevalenceAnalyses(cohortDefinitionId).forEach(joiner::add);
            getQueriesForCriteriaAnalyses(cohortDefinitionId).forEach(joiner::add);

            joiner.add(jsonObject.getString("sqlCleanup"));

            final String sql = sourceAwareSqlRender.renderSql(sourceId, joiner.toString(), new String[]{}, new String[]{});
            final String tempQualifier = SourceUtils.getTempQualifier(source);
            final String translatedSql = SqlTranslate.translateSql(sql, source.getSourceDialect(), SessionUtils.sessionId(), tempQualifier);
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
