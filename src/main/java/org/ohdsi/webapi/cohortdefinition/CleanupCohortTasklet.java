/*
 * Copyright 2018 cknoll1.
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

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ohdsi.circe.helper.ResourceHelper;
import org.ohdsi.sql.SqlRender;
import org.ohdsi.sql.SqlTranslate;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceDaimon;
import org.ohdsi.webapi.source.SourceRepository;
import org.ohdsi.webapi.util.SessionUtils;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

/**
 *
 * @author cknoll1
 */
public class CleanupCohortTasklet implements Tasklet {


  private static final Log log = LogFactory.getLog(CleanupCohortTasklet.class);
	
	private final TransactionTemplate transactionTemplate;
	private final SourceRepository sourceRepository;

	private final String CLEANUP_TEMPLATE = ResourceHelper.GetResourceAsString("/resources/cohortdefinition/sql/cleanupResults.sql"); 

	public CleanupCohortTasklet(final TransactionTemplate transactionTemplate,
		final SourceRepository sourceRepository) {
		this.transactionTemplate = transactionTemplate;
		this.sourceRepository = sourceRepository;
	}
	
  private JdbcTemplate getSourceJdbcTemplate(Source source) {
    DriverManagerDataSource dataSource = new DriverManagerDataSource(source.getSourceConnection());
    JdbcTemplate template = new JdbcTemplate(dataSource);
    return template;
  }
	
  private Integer doTask(ChunkContext chunkContext) {
    int sourcesUpdated = 0;
    
    Map<String, Object> jobParams = chunkContext.getStepContext().getJobParameters();
    Integer cohortId = Integer.valueOf(jobParams.get("cohort_definition_id").toString());
    String sessionId = SessionUtils.sessionId();

		List<Source> resultsSources = StreamSupport.stream(this.sourceRepository.findAll().spliterator(), false)
			.filter(source->source.getDaimons().stream().filter(daimon->daimon.getDaimonType() == SourceDaimon.DaimonType.Results).findAny().isPresent())
			.collect(Collectors.toList());		
		
		for (Source source : resultsSources) {
			try {
				String resultSchema = source.getTableQualifier(SourceDaimon.DaimonType.Results);
				String deleteSql = SqlRender.renderSql(CLEANUP_TEMPLATE, new String[]{"results_database_schema", "cohort_definition_id"}, new String[]{resultSchema, cohortId.toString()});
				deleteSql = SqlTranslate.translateSql(deleteSql, source.getSourceDialect(), sessionId, null);
				
				getSourceJdbcTemplate(source).batchUpdate(deleteSql.split(";")); // use batch update since SQL translation may produce multiple statements
				sourcesUpdated++;
			} catch (Exception e) {
				log.error(String.format("Error deleting results for cohort: %d", cohortId));
			}
		}
    return sourcesUpdated;
  }

  @Override
  public RepeatStatus execute(final StepContribution contribution, final ChunkContext chunkContext) throws Exception {
   
		final Integer ret = this.transactionTemplate.execute(new TransactionCallback<Integer>() {
			@Override
			public Integer doInTransaction(final TransactionStatus status) {
				return doTask(chunkContext);
			}
		});

    return RepeatStatus.FINISHED;
  }
	
}
