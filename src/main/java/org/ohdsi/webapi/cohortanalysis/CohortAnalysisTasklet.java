package org.ohdsi.webapi.cohortanalysis;

import static org.ohdsi.webapi.util.SecurityUtils.whitelist;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ohdsi.sql.SqlSplit;
import org.ohdsi.webapi.cohortresults.CohortResultsAnalysisRunner;
import org.ohdsi.webapi.cohortresults.VisualizationDataRepository;
import org.ohdsi.webapi.service.CohortAnalysisService;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.dao.DataAccessException;
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
    
    private final CohortResultsAnalysisRunner analysisRunner;
    
    public CohortAnalysisTasklet(CohortAnalysisTask task, final JdbcTemplate jdbcTemplate,
        final TransactionTemplate transactionTemplate, 
        String sourceDialect, VisualizationDataRepository visualizationDataRepository) {
        this.task = task;
        this.jdbcTemplate = jdbcTemplate;
        this.transactionTemplate = transactionTemplate;
        this.analysisRunner = new CohortResultsAnalysisRunner(sourceDialect, visualizationDataRepository);
    }
    
    @Override
    public RepeatStatus execute(final StepContribution contribution, final ChunkContext chunkContext) throws Exception {
        
        try {
            final int[] ret = this.transactionTemplate.execute(new TransactionCallback<int[]>() {
                
                @Override
                public int[] doInTransaction(final TransactionStatus status) {
                	
                	String cohortSql = CohortAnalysisService.getCohortAnalysisSql(task);
                	
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
            
            log.debug("warm up visualizations");
            final int count = this.analysisRunner.warmupData(jdbcTemplate, task);
            log.debug("warmed up " + count + " visualizations");
        } catch (final TransactionException | DataAccessException e) {
            log.error(whitelist(e));
            throw e;//FAIL job status
        }
        return RepeatStatus.FINISHED;
    }
    
}
