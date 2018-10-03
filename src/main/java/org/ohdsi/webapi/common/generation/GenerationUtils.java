package org.ohdsi.webapi.common.generation;

import org.ohdsi.webapi.cohortcharacterization.CreateCohortTableTasklet;
import org.ohdsi.webapi.cohortcharacterization.DropCohortTableListener;
import org.ohdsi.webapi.cohortcharacterization.GenerateLocalCohortTasklet;
import org.ohdsi.webapi.cohortdefinition.CohortDefinition;
import org.ohdsi.webapi.service.CohortGenerationService;
import org.ohdsi.webapi.service.SourceService;
import org.ohdsi.webapi.util.SessionUtils;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.builder.SimpleJobBuilder;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.function.Function;

@Component
public class GenerationUtils {

    private StepBuilderFactory stepBuilderFactory;
    private TransactionTemplate transactionTemplate;
    private CohortGenerationService cohortGenerationService;
    private SourceService sourceService;
    private JobBuilderFactory jobBuilders;

    public GenerationUtils(StepBuilderFactory stepBuilderFactory, TransactionTemplate transactionTemplate, CohortGenerationService cohortGenerationService, SourceService sourceService, JobBuilderFactory jobBuilders) {

        this.stepBuilderFactory = stepBuilderFactory;
        this.transactionTemplate = transactionTemplate;
        this.cohortGenerationService = cohortGenerationService;
        this.sourceService = sourceService;
        this.jobBuilders = jobBuilders;
    }

    public static String getTempCohortTableName() {

        return "cohort_" + SessionUtils.sessionId();
    }

    public Job buildJobForCohortBasedAnalysisTasklet(
            String analysisTypeName,
            JdbcTemplate jdbcTemplate,
            Function<ChunkContext, List<CohortDefinition>> cohortGetter,
            AnalysisTasklet analysisTasklet
    ) {

        CreateCohortTableTasklet createCohortTableTasklet = new CreateCohortTableTasklet(jdbcTemplate, transactionTemplate, sourceService);
        Step createCohortTableStep = stepBuilderFactory.get(analysisTypeName + ".createCohortTable")
                .tasklet(createCohortTableTasklet)
                .build();

        GenerateLocalCohortTasklet generateLocalCohortTasklet = new GenerateLocalCohortTasklet(
                transactionTemplate,
                cohortGenerationService,
                sourceService,
                cohortGetter
        );
        Step generateLocalCohortStep = stepBuilderFactory.get(analysisTypeName + ".generateCohort")
                .tasklet(generateLocalCohortTasklet)
                .build();

        Step generateCohortFeaturesStep = stepBuilderFactory.get(analysisTypeName + ".generate")
                .tasklet(analysisTasklet)
                .build();

        DropCohortTableListener dropCohortTableListener = new DropCohortTableListener(jdbcTemplate, transactionTemplate, sourceService);

        SimpleJobBuilder generateJobBuilder = jobBuilders.get(analysisTypeName)
                .start(createCohortTableStep)
                .next(generateLocalCohortStep)
                .next(generateCohortFeaturesStep)
                .listener(dropCohortTableListener);

        return generateJobBuilder.build();
    }
}
