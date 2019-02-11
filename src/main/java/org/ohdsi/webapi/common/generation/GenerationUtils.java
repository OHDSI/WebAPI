package org.ohdsi.webapi.common.generation;

import org.ohdsi.webapi.Constants;
import org.ohdsi.webapi.cohortcharacterization.CreateCohortTableTasklet;
import org.ohdsi.webapi.cohortcharacterization.DropCohortTableListener;
import org.ohdsi.webapi.cohortcharacterization.GenerateLocalCohortTasklet;
import org.ohdsi.webapi.cohortdefinition.CohortDefinition;
import org.ohdsi.webapi.service.AbstractDaoService;
import org.ohdsi.webapi.service.CohortGenerationService;
import org.ohdsi.webapi.service.GenerationTaskExceptionHandler;
import org.ohdsi.webapi.service.JobService;
import org.ohdsi.webapi.service.SourceService;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceDaimon;
import org.ohdsi.webapi.sqlrender.SourceAwareSqlRender;
import org.ohdsi.webapi.util.SessionUtils;
import org.ohdsi.webapi.util.SourceUtils;
import org.ohdsi.webapi.util.TempTableCleanupManager;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.builder.SimpleJobBuilder;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Collection;
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

    public GenerationUtils(StepBuilderFactory stepBuilderFactory,
                           TransactionTemplate transactionTemplate,
                           CohortGenerationService cohortGenerationService,
                           SourceService sourceService,
                           JobBuilderFactory jobBuilders,
                           SourceAwareSqlRender sourceAwareSqlRender,
                           JobService jobService) {

        this.stepBuilderFactory = stepBuilderFactory;
        this.transactionTemplate = transactionTemplate;
        this.cohortGenerationService = cohortGenerationService;
        this.sourceService = sourceService;
        this.jobBuilders = jobBuilders;
        this.sourceAwareSqlRender = sourceAwareSqlRender;
        this.jobService = jobService;
    }

    public static String getTempCohortTableName(String sessionId) {

        return Constants.TEMP_COHORT_TABLE_PREFIX + sessionId;
    }

    public Job buildJobForCohortBasedAnalysisTasklet(
            String analysisTypeName,
            Source source,
            JobParametersBuilder builder,
            JdbcTemplate jdbcTemplate,
            Function<ChunkContext, Collection<CohortDefinition>> cohortGetter,
            CancelableTasklet analysisTasklet
    ) {

        final String sessionId = SessionUtils.sessionId();
        builder.addString(SESSION_ID, sessionId);
        builder.addString(TARGET_TABLE, GenerationUtils.getTempCohortTableName(sessionId));

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
                cohortGenerationService,
                sourceService,
                jobService,
                cohortGetter
        );
        Step generateLocalCohortStep = stepBuilderFactory.get(analysisTypeName + ".generateCohort")
                .tasklet(generateLocalCohortTasklet)
                .build();

        Step generateCohortFeaturesStep = stepBuilderFactory.get(analysisTypeName + ".generate")
                .tasklet(analysisTasklet)
                .exceptionHandler(exceptionHandler)
                .build();

        DropCohortTableListener dropCohortTableListener = new DropCohortTableListener(jdbcTemplate, transactionTemplate, sourceService, sourceAwareSqlRender);

        SimpleJobBuilder generateJobBuilder = jobBuilders.get(analysisTypeName)
                .start(createCohortTableStep)
                .next(generateLocalCohortStep)
                .next(generateCohortFeaturesStep)
                .listener(dropCohortTableListener)
                .listener(new AutoremoveJobListener(jobService));

        return generateJobBuilder.build();
    }

    public Job buildJobForExecutionEngineBasedAnalysisTasklet(String analysisTypeName,
                                                              Source source,
                                                              JobParametersBuilder builder,
                                                              JdbcTemplate jdbcTemplate) {
        return null;
    }
}
