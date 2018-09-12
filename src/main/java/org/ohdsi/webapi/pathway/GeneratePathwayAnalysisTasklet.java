package org.ohdsi.webapi.pathway;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ohdsi.sql.SqlSplit;
import org.ohdsi.webapi.pathway.converter.SerializedPathwayAnalysisToPathwayAnalysisConverter;
import org.ohdsi.webapi.pathway.domain.PathwayAnalysisEntity;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.StoppableTasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import static org.ohdsi.webapi.Constants.Params.DESIGN;
import static org.ohdsi.webapi.Constants.Params.SOURCE_ID;

public class GeneratePathwayAnalysisTasklet implements StoppableTasklet {

    private static final Log log = LogFactory.getLog(GeneratePathwayAnalysisTasklet.class);
    private final long checkInterval = 1000L;

    private volatile boolean stopped = false;

    private final SerializedPathwayAnalysisToPathwayAnalysisConverter designConverter = new SerializedPathwayAnalysisToPathwayAnalysisConverter();
    private final JdbcTemplate jdbcTemplate;
    private final TransactionTemplate transactionTemplate;
    private final PathwayService pathwayService;
    private final ExecutorService taskExecutor;

    public GeneratePathwayAnalysisTasklet(JdbcTemplate jdbcTemplate, TransactionTemplate transactionTemplate, PathwayService pathwayService) {

        this.transactionTemplate = transactionTemplate;
        this.jdbcTemplate = jdbcTemplate;
        this.pathwayService = pathwayService;

        this.taskExecutor = Executors.newSingleThreadExecutor();
    }

    @Override
    public void stop() {

        this.stopped = true;
    }

    @Override
    public RepeatStatus execute(final StepContribution contribution, final ChunkContext chunkContext) throws Exception {
        try {
            this.transactionTemplate.execute(status -> doTask(chunkContext));
        } catch (final TransactionException e) {
            log.error(e.getMessage(), e);
            throw e;
        } finally {
            taskExecutor.shutdown();
        }
        return RepeatStatus.FINISHED;
    }

    private void initTx() {
        DefaultTransactionDefinition txDefinition = new DefaultTransactionDefinition();
        txDefinition.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        TransactionStatus initStatus = this.transactionTemplate.getTransactionManager().getTransaction(txDefinition);
        this.transactionTemplate.getTransactionManager().commit(initStatus);
    }

    private int[] doTask(ChunkContext chunkContext) {
        initTx();

        Map<String, Object> jobParams = chunkContext.getStepContext().getJobParameters();

        Integer sourceId = Integer.parseInt(jobParams.get(SOURCE_ID).toString());
        PathwayAnalysisEntity pathwayAnalysis = designConverter.convertToEntityAttribute(jobParams.get(DESIGN).toString());
        Long jobId = chunkContext.getStepContext().getStepExecution().getJobExecution().getId();

        String analysisSql = pathwayService.buildAnalysisSql(jobId, pathwayAnalysis, sourceId);

        String[] queries = SqlSplit.splitSql(analysisSql);

        FutureTask<int[]> batchUpdateTask = new FutureTask<>(
                () -> jdbcTemplate.batchUpdate(queries)
        );
        taskExecutor.execute(batchUpdateTask);
        try {
            while (true) {
                Thread.sleep(checkInterval);
                if (batchUpdateTask.isDone()) {
                    return batchUpdateTask.get();
                } else if (stopped) {
                    batchUpdateTask.cancel(true);
                    return null;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
