package org.ohdsi.webapi.cohortcharacterization;

import org.ohdsi.webapi.cohortdefinition.CohortDefinition;
import org.ohdsi.webapi.cohortdefinition.CohortGenerationRequestBuilder;
import org.ohdsi.webapi.cohortdefinition.CohortGenerationUtils;
import org.ohdsi.webapi.generationcache.GenerationCacheHelper;
import org.ohdsi.webapi.service.CohortGenerationService;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceService;
import org.ohdsi.webapi.util.CancelableJdbcTemplate;
import org.ohdsi.webapi.util.SessionUtils;
import org.ohdsi.webapi.util.SourceUtils;
import org.ohdsi.webapi.util.StatementCancel;
import org.ohdsi.webapi.util.StatementCancelException;
import org.ohdsi.webapi.util.TempTableCleanupManager;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.StoppableTasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.ohdsi.webapi.Constants.Params.SOURCE_ID;
import static org.ohdsi.webapi.Constants.Params.TARGET_TABLE;

public class GenerateLocalCohortTasklet implements StoppableTasklet {

    private static final String COPY_CACHED_RESULTS = "INSERT INTO %s.%s (cohort_definition_id, subject_id, cohort_start_date, cohort_end_date) SELECT %s as cohort_definition_id, subject_id, cohort_start_date, cohort_end_date FROM (%s) r";

    protected TransactionTemplate transactionTemplate;
    private final CancelableJdbcTemplate cancelableJdbcTemplate;
    protected final CohortGenerationService cohortGenerationService;
    protected final SourceService sourceService;
    protected final Function<ChunkContext, Collection<CohortDefinition>> cohortGetter;
    private final GenerationCacheHelper generationCacheHelper;
    private boolean useAsyncCohortGeneration;
    private Set<StatementCancel> statementCancels = ConcurrentHashMap.newKeySet();
    private volatile boolean stopped = false;

    public GenerateLocalCohortTasklet(TransactionTemplate transactionTemplate,
                                      CancelableJdbcTemplate cancelableJdbcTemplate,
                                      CohortGenerationService cohortGenerationService,
                                      SourceService sourceService,
                                      Function<ChunkContext, Collection<CohortDefinition>> cohortGetter,
                                      GenerationCacheHelper generationCacheHelper,
                                      boolean useAsyncCohortGeneration) {

        this.transactionTemplate = transactionTemplate;
        this.cancelableJdbcTemplate = cancelableJdbcTemplate;
        this.cohortGenerationService = cohortGenerationService;
        this.sourceService = sourceService;
        this.cohortGetter = cohortGetter;
        this.generationCacheHelper = generationCacheHelper;
        this.useAsyncCohortGeneration = useAsyncCohortGeneration;
    }

    @Override
    public void stop() {

        try {
            stopped = true;
            for (StatementCancel statementCancel: statementCancels) {
                statementCancel.cancel();
            }
        } catch (SQLException ignored) {
        }
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {

        Map<String, Object> jobParameters = chunkContext.getStepContext().getJobParameters();
        Source source = sourceService.findBySourceId(Integer.valueOf(jobParameters.get(SOURCE_ID).toString()));
        String resultSchema = SourceUtils.getResultsQualifier(source);
        String targetTable = jobParameters.get(TARGET_TABLE).toString();

        Collection<CohortDefinition> cohortDefinitions = cohortGetter.apply(chunkContext);

        if (useAsyncCohortGeneration) {
            List<CompletableFuture> executions = cohortDefinitions.stream()
                    .map(cd ->
                            CompletableFuture.supplyAsync(() -> generateCohort(cd, source, resultSchema, targetTable),
                                    Executors.newSingleThreadExecutor()
                            )
                    ).collect(Collectors.toList());
            CompletableFuture.allOf(executions.toArray(new CompletableFuture[]{})).join();
        } else {
            CompletableFuture.runAsync(() ->
                            cohortDefinitions.stream().forEach(cd -> generateCohort(cd, source, resultSchema, targetTable)),
                    Executors.newSingleThreadExecutor()
            ).join();
        }

        return RepeatStatus.FINISHED;
    }

    private Object generateCohort(CohortDefinition cd, Source source, String resultSchema, String targetTable) {
        if (stopped) {
            return null;
        }
        String sessionId = SessionUtils.sessionId();
        CohortGenerationRequestBuilder generationRequestBuilder = new CohortGenerationRequestBuilder(
                sessionId,
                resultSchema
        );

        int designHash = this.generationCacheHelper.computeHash(cd.getDetails().getExpression());
        CohortGenerationUtils.insertInclusionRules(cd, source, designHash, resultSchema, sessionId, cancelableJdbcTemplate);

        try {
            StatementCancel stmtCancel = new StatementCancel();
            statementCancels.add(stmtCancel);
            GenerationCacheHelper.CacheResult res = generationCacheHelper.computeCacheIfAbsent(cd, source, generationRequestBuilder, (resId, sqls) -> {
                try {
                    generationCacheHelper.runCancelableCohortGeneration(cancelableJdbcTemplate, stmtCancel, sqls);
                } finally {
                    // Usage of the same sessionId for all cohorts would cause issues in databases w/o real temp tables support
                    // And we cannot postfix existing sessionId with some index because SqlRender requires sessionId to be only 8 symbols long
                    // So, relying on TempTableCleanupManager.removeTempTables from GenerationTaskExceptionHandler is not an option
                    // That's why explicit TempTableCleanupManager call is defined
                    TempTableCleanupManager cleanupManager = new TempTableCleanupManager(
                            cancelableJdbcTemplate,
                            transactionTemplate,
                            source.getSourceDialect(),
                            sessionId,
                            SourceUtils.getTempQualifier(source)
                    );
                    cleanupManager.cleanupTempTables();
                }
            });
            String sql = String.format(COPY_CACHED_RESULTS, SourceUtils.getTempQualifier(source), targetTable, cd.getId(), res.getSql());
            cancelableJdbcTemplate.batchUpdate(stmtCancel, sql);
            statementCancels.remove(stmtCancel);
        } catch (StatementCancelException ignored) {
            // this exception must be caught to prevent "FAIL" status of the job
        }
        return null;
    }
}
