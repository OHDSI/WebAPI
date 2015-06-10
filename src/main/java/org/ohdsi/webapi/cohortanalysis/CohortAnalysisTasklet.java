package org.ohdsi.webapi.cohortanalysis;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ohdsi.sql.SqlSplit;
import org.ohdsi.webapi.service.CohortAnalysisService;
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
    
    private final CohortAnalysisTask task;
       
    private final JdbcTemplate jdbcTemplate;
    
    private final TransactionTemplate transactionTemplate;
    
    public CohortAnalysisTasklet(CohortAnalysisTask task, final JdbcTemplate jdbcTemplate,
        final TransactionTemplate transactionTemplate) {
        this.task = task;
        this.jdbcTemplate = jdbcTemplate;
        this.transactionTemplate = transactionTemplate;
    }
    
    @Override
    public RepeatStatus execute(final StepContribution contribution, final ChunkContext chunkContext) throws Exception {
        
        try {
            final int[] ret = this.transactionTemplate.execute(new TransactionCallback<int[]>() {
                
                @Override
                public int[] doInTransaction(final TransactionStatus status) {
                  CohortAnalysisService cas = new CohortAnalysisService();
                	String cohortSql = cas.getCohortAnalysisSql(task);
                	
                	String[] stmts = null;
                	if (cohortSql != null) {
                		if (log.isDebugEnabled()) {
                			
                			stmts = SqlSplit.splitSql(cohortSql);
                            for (int x = 0; x < stmts.length; x++) {
                                log.debug(String.format("Split SQL %s : %s", x, stmts[x]));
                            }
                        }
                	}
                    return CohortAnalysisTasklet.this.jdbcTemplate.batchUpdate(stmts);
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
