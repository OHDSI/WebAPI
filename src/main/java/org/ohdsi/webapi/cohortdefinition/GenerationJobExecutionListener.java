/*
 * Copyright 2017 cknoll1.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ohdsi.webapi.cohortdefinition;

import java.util.*;

import org.apache.commons.lang3.StringUtils;
import org.ohdsi.sql.SqlRender;
import org.ohdsi.sql.SqlTranslate;
import org.ohdsi.webapi.Constants;
import org.ohdsi.webapi.GenerationStatus;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceService;
import org.ohdsi.webapi.util.SourceUtils;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.JobParameters;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import static org.ohdsi.webapi.Constants.Params.TARGET_DATABASE_SCHEMA;
import static org.ohdsi.webapi.Constants.Params.TARGET_TABLE;

/**
 *
 * @author cknoll1
 */
public class GenerationJobExecutionListener implements JobExecutionListener {

	private final SourceService sourceService;
	private final CohortDefinitionRepository cohortDefinitionRepository;
  private final TransactionTemplate transactionTemplate;
	private final JdbcTemplate sourceTemplate;
	
	public GenerationJobExecutionListener(SourceService sourceService,
																				CohortDefinitionRepository cohortDefinitionRepository,
																				TransactionTemplate transactionTemplate,
																				JdbcTemplate sourceTemplate)
	{
		this.sourceService = sourceService;
		this.cohortDefinitionRepository = cohortDefinitionRepository;
		this.transactionTemplate = transactionTemplate;
		this.sourceTemplate = sourceTemplate;
	}
	
  private CohortGenerationInfo findBySourceId(Collection<CohortGenerationInfo> infoList, Integer sourceId)
  {
    for (CohortGenerationInfo info : infoList) {
      if (info.getId().getSourceId().equals(sourceId))
        return info;
    }
    return null;
  }	
	
	@Override
	public void afterJob(JobExecution je) {
		
    JobParameters jobParams = je.getJobParameters();
    Integer defId = Integer.valueOf(jobParams.getString("cohort_definition_id"));
    Integer sourceId = Integer.valueOf(jobParams.getString("source_id"));
    Source source = sourceService.findBySourceId(sourceId);
    String cohortTable = jobParams.getString(TARGET_DATABASE_SCHEMA) + "." + jobParams.getString(TARGET_TABLE);
		
		DefaultTransactionDefinition completeTx = new DefaultTransactionDefinition();
		completeTx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
		TransactionStatus completeStatus = this.transactionTemplate.getTransactionManager().getTransaction(completeTx);      
		CohortDefinition df = this.cohortDefinitionRepository.findOne(defId);
		CohortGenerationInfo info = findBySourceId(df.getGenerationInfoList(), sourceId);
		info.setExecutionDuration((int)(je.getEndTime().getTime() - je.getStartTime().getTime()));
		info.setStatus(GenerationStatus.COMPLETE);

		if (je.getStatus() == BatchStatus.FAILED || je.getStatus() == BatchStatus.STOPPED) {
			info.setIsValid(false);
			info.setRecordCount(null);
			info.setPersonCount(null);
			info.setCanceled(je.getStepExecutions().stream().anyMatch(se -> Objects.equals(Constants.CANCELED, se.getExitStatus().getExitCode())));
			info.setFailMessage(StringUtils.abbreviateMiddle(je.getAllFailureExceptions().get(0).getMessage(),"... [truncated] ...",2000));
		} else {
			info.setIsValid(true);
			info.setFailMessage(null);

			// query summary results from source
			String statsQuery = "SELECT count(distinct subject_id) as person_count, count(*) as record_count from @cohort_table where cohort_definition_id = @cohort_definition_id";
			String statsSql = SqlTranslate.translateSql(statsQuery, source.getSourceDialect(), null, null);
			String renderedSql = SqlRender.renderSql(statsSql, new String[]{"cohort_table", "cohort_definition_id"}, new String[]{cohortTable, defId.toString()});
      Map<String, Object> stats = this.sourceTemplate.queryForMap(renderedSql);
			info.setPersonCount(Long.parseLong(stats.get("person_count").toString()));
			info.setRecordCount(Long.parseLong(stats.get("record_count").toString()));
		}
		
		this.cohortDefinitionRepository.save(df);
		this.transactionTemplate.getTransactionManager().commit(completeStatus);
	}

	@Override
	public void beforeJob(JobExecution je) { 
    Date startTime = Calendar.getInstance().getTime();
    JobParameters jobParams = je.getJobParameters();
    Integer defId = Integer.valueOf(jobParams.getString("cohort_definition_id"));
    Integer sourceId = Integer.valueOf(jobParams.getString("source_id"));
		
    DefaultTransactionDefinition initTx = new DefaultTransactionDefinition();
    initTx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    TransactionStatus initStatus = this.transactionTemplate.getTransactionManager().getTransaction(initTx);
    CohortDefinition df = this.cohortDefinitionRepository.findOne(defId);
    CohortGenerationInfo info = findBySourceId(df.getGenerationInfoList(), sourceId);
    info.setIsValid(false);
    info.setStartTime(startTime);
    info.setStatus(GenerationStatus.RUNNING);    
    this.cohortDefinitionRepository.save(df);
    this.transactionTemplate.getTransactionManager().commit(initStatus);	
	}
}
