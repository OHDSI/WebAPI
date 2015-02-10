package org.ohdsi.webapi.cohortresults;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.jdbc.core.JdbcTemplate;

public class CohortAnalysisTasklet implements Tasklet {
    
    private static final Log log = LogFactory.getLog(CohortAnalysisTasklet.class);
    
    private final String[] sql;
    
    private final JdbcTemplate jdbcTemplate;
    
    public CohortAnalysisTasklet(final String[] taskSql, final JdbcTemplate jdbcTemplate) {
        this.sql = taskSql;
        this.jdbcTemplate = jdbcTemplate;
    }
    
    @Override
    public RepeatStatus execute(final StepContribution contribution, final ChunkContext chunkContext) throws Exception {
        
        try {
            final int[] ret = this.jdbcTemplate.batchUpdate(this.sql);
            log.debug("Update count: " + ret);
        } catch (final Exception e) {
            log.error(e.getMessage(), e);
            throw e;//FAIL job status
        }
        return RepeatStatus.FINISHED;
    }
    
};
