package org.ohdsi.webapi.cohortcharacterization;

import org.ohdsi.webapi.cohortdefinition.CohortDefinition;
import org.ohdsi.webapi.service.CohortGenerationService;
import org.ohdsi.webapi.source.SourceService;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.util.CancelableJdbcTemplate;
import org.ohdsi.webapi.util.SourceUtils;
import org.ohdsi.webapi.util.StatementCancel;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.StoppableTasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.ohdsi.webapi.Constants.Params.*;

public class GenerateLocalCohortTasklet implements StoppableTasklet {

    protected TransactionTemplate transactionTemplate;
    private final CancelableJdbcTemplate cancelableJdbcTemplate;
    protected final CohortGenerationService cohortGenerationService;
    protected final SourceService sourceService;
    protected final Function<ChunkContext, Collection<CohortDefinition>> cohortGetter;

    private StatementCancel stmtCancel = new StatementCancel();

    public GenerateLocalCohortTasklet(TransactionTemplate transactionTemplate,
                                      CancelableJdbcTemplate cancelableJdbcTemplate,
                                      CohortGenerationService cohortGenerationService,
                                      SourceService sourceService,
                                      Function<ChunkContext, Collection<CohortDefinition>> cohortGetter) {

        this.transactionTemplate = transactionTemplate;
        this.cancelableJdbcTemplate = cancelableJdbcTemplate;
        this.cohortGenerationService = cohortGenerationService;
        this.sourceService = sourceService;
        this.cohortGetter = cohortGetter;
    }

    @Override
    public void stop() {

        try {
            this.stmtCancel.cancel();
        } catch (SQLException ignored) {
        }
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {

        Map<String, Object> jobParameters = chunkContext.getStepContext().getJobParameters();
        Source source = sourceService.findBySourceId(Integer.valueOf(jobParameters.get(SOURCE_ID).toString()));
        String targetTable = jobParameters.get(TARGET_TABLE).toString();
        String sessionId = jobParameters.get(SESSION_ID).toString();

        Collection<CohortDefinition> cohortDefinitions = cohortGetter.apply(chunkContext);

        List<CompletableFuture> executions = cohortDefinitions.stream()
                .map(cd ->
                        CompletableFuture.supplyAsync(
                                () -> {
                                    String[] sqls = cohortGenerationService.buildGenerationSql(
                                            cd.getId(),
                                            source.getSourceId(),
                                            sessionId,
                                            SourceUtils.getTempQualifier(source),
                                            targetTable,
                                            false
                                    );
                                    cancelableJdbcTemplate.batchUpdate(stmtCancel, sqls);
                                    return null;
                                },
                                Executors.newSingleThreadExecutor()
                        )
                ).collect(Collectors.toList());

        CompletableFuture.allOf(executions.toArray(new CompletableFuture[]{})).join();

        return RepeatStatus.FINISHED;
    }
}
