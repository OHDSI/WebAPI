package org.ohdsi.webapi.common.generation;

import static org.ohdsi.webapi.Constants.Params.SESSION_ID;
import static org.ohdsi.webapi.Constants.Params.TARGET_TABLE;

import java.util.Collection;
import java.util.function.Function;

import org.ohdsi.webapi.Constants;
import org.ohdsi.webapi.cohortcharacterization.CreateCohortTableTasklet;
import org.ohdsi.webapi.cohortcharacterization.DropCohortTableListener;
import org.ohdsi.webapi.cohortcharacterization.GenerateLocalCohortTasklet;
import org.ohdsi.webapi.cohortdefinition.CohortDefinitionEntity;
import org.ohdsi.webapi.cohortdefinition.CohortGenerationService;
import org.ohdsi.webapi.generationcache.GenerationCacheHelper;
import org.ohdsi.webapi.service.AbstractDaoService;
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
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.builder.SimpleJobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import jakarta.persistence.EntityManager;

@Component
public class GenerationUtils extends AbstractDaoService {

    private JobRepository jobRepository;
    private PlatformTransactionManager transactionManager;
    private TransactionTemplate transactionTemplate;
    private TransactionTemplate batchTransactionTemplate;
    private CohortGenerationService cohortGenerationService;
    private SourceService sourceService;
    private JobService jobService;
    private final SourceAwareSqlRender sourceAwareSqlRender;
    private final EntityManager entityManager;
    private final GenerationCacheHelper generationCacheHelper;

    @Value("${cache.generation.useAsync:false}")
    private boolean useAsyncCohortGeneration;

    public GenerationUtils(JobRepository jobRepository,
                           PlatformTransactionManager transactionManager,
                           @Qualifier("transactionTemplate") TransactionTemplate transactionTemplate,
                           @Qualifier("batchTransactionTemplate") TransactionTemplate batchTransactionTemplate,
                           CohortGenerationService cohortGenerationService,
                           SourceService sourceService,
                           SourceAwareSqlRender sourceAwareSqlRender,
                           JobService jobService,
                           EntityManager entityManager,
                           GenerationCacheHelper generationCacheHelper) {

        this.jobRepository = jobRepository;
        this.transactionManager = transactionManager;
        this.transactionTemplate = transactionTemplate;
        this.batchTransactionTemplate = batchTransactionTemplate;
        this.cohortGenerationService = cohortGenerationService;
        this.sourceService = sourceService;
        this.sourceAwareSqlRender = sourceAwareSqlRender;
        this.jobService = jobService;
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
            Function<ChunkContext, Collection<CohortDefinitionEntity>> cohortGetter,
            CancelableTasklet analysisTasklet
    ) {

        final String sessionId = SessionUtils.sessionId();
        addSessionParams(builder, sessionId);

        TempTableCleanupManager cleanupManager = new TempTableCleanupManager(
                getSourceJdbcTemplate(source),
                batchTransactionTemplate,
                source.getSourceDialect(),
                sessionId,
                SourceUtils.getTempQualifier(source)
        );

        GenerationTaskExceptionHandler exceptionHandler = new GenerationTaskExceptionHandler(cleanupManager);

        CreateCohortTableTasklet createCohortTableTasklet = new CreateCohortTableTasklet(jdbcTemplate, batchTransactionTemplate, sourceService, sourceAwareSqlRender);
        Step createCohortTableStep = new StepBuilder(analysisTypeName + ".createCohortTable", jobRepository)
                .tasklet(createCohortTableTasklet, transactionManager)
                .build();

        GenerateLocalCohortTasklet generateLocalCohortTasklet = new GenerateLocalCohortTasklet(
                batchTransactionTemplate,
                getSourceJdbcTemplate(source),
                cohortGenerationService,
                sourceService,
                cohortGetter,
                generationCacheHelper,
                useAsyncCohortGeneration
        );
        Step generateLocalCohortStep = new StepBuilder(analysisTypeName + ".generateCohort", jobRepository)
                .tasklet(generateLocalCohortTasklet, transactionManager)
                .build();

        Step generateAnalysisStep = new StepBuilder(analysisTypeName + ".generate", jobRepository)
                .tasklet(analysisTasklet, transactionManager)
                .exceptionHandler(exceptionHandler)
                .build();

        DropCohortTableListener dropCohortTableListener = new DropCohortTableListener(jdbcTemplate, sourceService, sourceAwareSqlRender);

        SimpleJobBuilder generateJobBuilder =  new JobBuilder(analysisTypeName, jobRepository)
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

}
