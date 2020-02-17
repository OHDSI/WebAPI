package org.ohdsi.webapi.common.generation;

import org.ohdsi.webapi.Constants;
import org.ohdsi.webapi.cohortcharacterization.CreateCohortTableTasklet;
import org.ohdsi.webapi.cohortcharacterization.DropCohortTableListener;
import org.ohdsi.webapi.cohortcharacterization.GenerateLocalCohortTasklet;
import org.ohdsi.webapi.cohortdefinition.CohortDefinition;
import org.ohdsi.webapi.executionengine.entity.AnalysisFile;
import org.ohdsi.webapi.executionengine.job.CreateAnalysisTasklet;
import org.ohdsi.webapi.executionengine.job.ExecutionEngineCallbackTasklet;
import org.ohdsi.webapi.executionengine.job.RunExecutionEngineTasklet;
import org.ohdsi.webapi.executionengine.repository.ExecutionEngineGenerationRepository;
import org.ohdsi.webapi.executionengine.service.ScriptExecutionService;
import org.ohdsi.webapi.generationcache.GenerationCacheHelper;
import org.ohdsi.webapi.service.AbstractDaoService;
import org.ohdsi.webapi.service.CohortGenerationService;
import org.ohdsi.webapi.service.GenerationTaskExceptionHandler;
import org.ohdsi.webapi.service.JobService;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceService;
import org.ohdsi.webapi.sqlrender.SourceAwareSqlRender;
import org.ohdsi.webapi.util.SessionUtils;
import org.ohdsi.webapi.util.SourceUtils;
import org.ohdsi.webapi.util.TempTableCleanupManager;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.builder.SimpleJobBuilder;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import javax.persistence.EntityManager;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import static org.ohdsi.webapi.Constants.Params.SESSION_ID;
import static org.ohdsi.webapi.Constants.Params.TARGET_TABLE;

@Component
public class GenerationUtils extends AbstractDaoService {

    private StepBuilderFactory stepBuilderFactory;
    private TransactionTemplate transactionTemplate;
    private CohortGenerationService cohortGenerationService;
    private SourceService sourceService;
    private JobBuilderFactory jobBuilders;
    private JobService jobService;
    private final SourceAwareSqlRender sourceAwareSqlRender;
    private final ScriptExecutionService executionService;
    private final ExecutionEngineGenerationRepository executionEngineGenerationRepository;
    private final EntityManager entityManager;
    private final GenerationCacheHelper generationCacheHelper;

    @Value("${cache.generation.useAsync:false}")
    private boolean useAsyncCohortGeneration;

    public GenerationUtils(StepBuilderFactory stepBuilderFactory,
                           TransactionTemplate transactionTemplate,
                           CohortGenerationService cohortGenerationService,
                           SourceService sourceService,
                           JobBuilderFactory jobBuilders,
                           SourceAwareSqlRender sourceAwareSqlRender,
                           JobService jobService,
                           ScriptExecutionService executionService,
                           ExecutionEngineGenerationRepository executionEngineGenerationRepository,
                           EntityManager entityManager,
                           GenerationCacheHelper generationCacheHelper) {

        this.stepBuilderFactory = stepBuilderFactory;
        this.transactionTemplate = transactionTemplate;
        this.cohortGenerationService = cohortGenerationService;
        this.sourceService = sourceService;
        this.jobBuilders = jobBuilders;
        this.sourceAwareSqlRender = sourceAwareSqlRender;
        this.jobService = jobService;
        this.executionService = executionService;
        this.executionEngineGenerationRepository = executionEngineGenerationRepository;
        this.entityManager = entityManager;
        this.generationCacheHelper = generationCacheHelper;
    }

    public static String getTempCohortTableName(String sessionId) {

        return Constants.TEMP_COHORT_TABLE_PREFIX + sessionId;
    }

    public SimpleJobBuilder buildJobForCohortBasedAnalysisTasklet(
            String analysisTypeName,
            Source source,
            JobParametersBuilder builder,
            JdbcTemplate jdbcTemplate,
            Function<ChunkContext, Collection<CohortDefinition>> cohortGetter,
            CancelableTasklet analysisTasklet
    ) {

        final String sessionId = SessionUtils.sessionId();
        addSessionParams(builder, sessionId);

        TempTableCleanupManager cleanupManager = new TempTableCleanupManager(
                getSourceJdbcTemplate(source),
                transactionTemplate,
                source.getSourceDialect(),
                sessionId,
                SourceUtils.getTempQualifier(source)
        );

        GenerationTaskExceptionHandler exceptionHandler = new GenerationTaskExceptionHandler(cleanupManager);

        CreateCohortTableTasklet createCohortTableTasklet = new CreateCohortTableTasklet(jdbcTemplate, transactionTemplate, sourceService, sourceAwareSqlRender);
        Step createCohortTableStep = stepBuilderFactory.get(analysisTypeName + ".createCohortTable")
                .tasklet(createCohortTableTasklet)
                .build();

        GenerateLocalCohortTasklet generateLocalCohortTasklet = new GenerateLocalCohortTasklet(
                transactionTemplate,
                getSourceJdbcTemplate(source),
                cohortGenerationService,
                sourceService,
                cohortGetter,
                generationCacheHelper,
                useAsyncCohortGeneration
        );
        Step generateLocalCohortStep = stepBuilderFactory.get(analysisTypeName + ".generateCohort")
                .tasklet(generateLocalCohortTasklet)
                .build();

        Step generateAnalysisStep = stepBuilderFactory.get(analysisTypeName + ".generate")
                .tasklet(analysisTasklet)
                .exceptionHandler(exceptionHandler)
                .build();

        DropCohortTableListener dropCohortTableListener = new DropCohortTableListener(jdbcTemplate, transactionTemplate, sourceService, sourceAwareSqlRender);

        SimpleJobBuilder generateJobBuilder = jobBuilders.get(analysisTypeName)
                .start(createCohortTableStep)
                .next(generateLocalCohortStep)
                .next(generateAnalysisStep)
                .listener(dropCohortTableListener)
                .listener(new AutoremoveJobListener(jobService));

        return generateJobBuilder;
    }

    protected void addSessionParams(JobParametersBuilder builder, String sessionId) {
        builder.addString(SESSION_ID, sessionId);
        builder.addString(TARGET_TABLE, GenerationUtils.getTempCohortTableName(sessionId));
    }

    public SimpleJobBuilder buildJobForExecutionEngineBasedAnalysisTasklet(String analysisTypeName,
                                                              Source source,
                                                              JobParametersBuilder builder,
                                                              List<AnalysisFile> analysisFiles) {

        final String sessionId = SessionUtils.sessionId();
        addSessionParams(builder, sessionId);

        CreateAnalysisTasklet createAnalysisTasklet = new CreateAnalysisTasklet(executionService, source.getSourceKey(), analysisFiles);
        RunExecutionEngineTasklet runExecutionEngineTasklet = new RunExecutionEngineTasklet(executionService, source, analysisFiles);
        ExecutionEngineCallbackTasklet callbackTasklet = new ExecutionEngineCallbackTasklet(executionEngineGenerationRepository, entityManager);

        Step createAnalysisExecutionStep = stepBuilderFactory.get(analysisTypeName + ".createAnalysisExecution")
                .tasklet(createAnalysisTasklet)
                .build();

        Step runExecutionStep = stepBuilderFactory.get(analysisTypeName + ".startExecutionEngine")
                .tasklet(runExecutionEngineTasklet)
                .build();

        Step waitCallbackStep = stepBuilderFactory.get(analysisTypeName + ".waitForCallback")
                .tasklet(callbackTasklet)
                .build();
        
        DropCohortTableListener dropCohortTableListener = new DropCohortTableListener(getSourceJdbcTemplate(source),
                transactionTemplate, sourceService, sourceAwareSqlRender);

        return jobBuilders.get(analysisTypeName)
                .start(createAnalysisExecutionStep)
                .next(runExecutionStep)
                .next(waitCallbackStep)
                .listener(dropCohortTableListener)
                .listener(new AutoremoveJobListener(jobService));
    }
}
