package org.ohdsi.webapi.cohortanalysis;

import jersey.repackaged.com.google.common.base.Joiner;
import static org.ohdsi.webapi.util.SecurityUtils.whitelist;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ohdsi.sql.SqlRender;
import org.ohdsi.sql.SqlSplit;
import org.ohdsi.sql.SqlTranslate;
import org.ohdsi.webapi.cohortresults.CohortResultsAnalysisRunner;
import org.ohdsi.webapi.cohortresults.VisualizationDataRepository;
import org.ohdsi.webapi.service.CohortAnalysisService;
import org.ohdsi.webapi.source.SourceDaimon;
import org.ohdsi.webapi.util.SessionUtils;
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
                	
									String clearResultsSql = "DELETE FROM @results_schema.heracles_results where analysis_id in (@analysis_ids) and cohort_definition_id in (@cohort_definition_ids)";
									String clearResultsDistSql = "DELETE FROM @results_schema.heracles_results_dist where analysis_id in (@analysis_ids) and cohort_definition_id in (@cohort_definition_ids)";
									String resultsTableQualifier = task.getSource().getTableQualifier(SourceDaimon.DaimonType.Results);
									String cohortDefinitionIds = (task.getCohortDefinitionIds() == null ? "" : Joiner.on(",").join(
											task.getCohortDefinitionIds()));
									String analysisIds = (task.getAnalysisIds() == null ? "" : Joiner.on(",").join(task.getAnalysisIds()));

									String[] params = new String[]{"results_schema", "cohort_definition_ids", "analysis_ids"};
									String[] values = new String[]{resultsTableQualifier, cohortDefinitionIds,analysisIds};
									
									clearResultsSql = SqlRender.renderSql(clearResultsSql, params, values);
									clearResultsSql = SqlTranslate.translateSql(clearResultsSql, task.getSource().getSourceDialect(), SessionUtils.sessionId(), resultsTableQualifier);
									CohortAnalysisTasklet.this.jdbcTemplate.execute(clearResultsSql);
									
									clearResultsDistSql = SqlRender.renderSql(clearResultsDistSql, params, values);
									clearResultsDistSql = SqlTranslate.translateSql(clearResultsDistSql, task.getSource().getSourceDialect(), SessionUtils.sessionId(), resultsTableQualifier);
									CohortAnalysisTasklet.this.jdbcTemplate.execute(clearResultsDistSql);
									
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
