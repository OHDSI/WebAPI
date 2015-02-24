package org.ohdsi.webapi.cohortanalysis;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

public class CohortAnalysisTasklet implements Tasklet {
    
    private static final Log log = LogFactory.getLog(CohortAnalysisTasklet.class);
    
    private final String[] sql;
    
    private final JdbcTemplate jdbcTemplate;
    
    private final TransactionTemplate transactionTemplate;
    
    public CohortAnalysisTasklet(final String[] taskSql, final JdbcTemplate jdbcTemplate,
        final TransactionTemplate transactionTemplate) {
        this.sql = taskSql;
        this.jdbcTemplate = jdbcTemplate;
        this.transactionTemplate = transactionTemplate;
    }
    
    @Override
    public RepeatStatus execute(final StepContribution contribution, final ChunkContext chunkContext) throws Exception {
        
        try {
            final int[] ret = this.transactionTemplate.execute(new TransactionCallback<int[]>() {
                
                @Override
                public int[] doInTransaction(final TransactionStatus status) {
                    return CohortAnalysisTasklet.this.jdbcTemplate.batchUpdate(CohortAnalysisTasklet.this.sql);
                }
            });
            log.debug("Update count: " + ret.length);
        } catch (final TransactionException e) {
            log.error(e.getMessage(), e);
            throw e;//FAIL job status
        }
        return RepeatStatus.FINISHED;
    }
    
};
