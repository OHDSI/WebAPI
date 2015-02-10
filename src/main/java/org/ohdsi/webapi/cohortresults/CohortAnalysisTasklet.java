package org.ohdsi.webapi.cohortresults;

import jersey.repackaged.com.google.common.base.Splitter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.StringUtils;

public class CohortAnalysisTasklet implements Tasklet {
	
	private static final Log log = LogFactory.getLog(CohortAnalysisTasklet.class);
	
	private CohortAnalysisTask task;
	private String sql;
	private JdbcTemplate jdbcTemplate;

	
	public CohortAnalysisTasklet(CohortAnalysisTask task, String sqlString, JdbcTemplate jdbcTemplate) {
		this.task = task;
		this.sql = sqlString;
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	public RepeatStatus execute(StepContribution contribution,
			ChunkContext chunkContext) throws Exception {
		/*
		log.info(String.format("Executing Cohort Analysis Job for \n:%s", task.toString()));
		Iterable<String> statements = Splitter.on(';').trimResults().split(sql);
		boolean hasError = false;
		// TODO all in one transaction
		for (String stmt : statements) {
			if (StringUtils.isEmpty(stmt)) {
				continue;
			}
			if (hasError) {
				log.info(String.format("unable to continue, unable to execute \n%s", stmt));
				break;
			}
			try {
				this.jdbcTemplate.execute(stmt);
			} catch (Exception e) {
				log.error(String.format("error executing %s", stmt), e);
				hasError = true;
			}
		}
		
		*/
		return RepeatStatus.FINISHED;
	}

}
