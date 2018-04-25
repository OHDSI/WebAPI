package org.ohdsi.webapi.cohortanalysis;

import java.util.Calendar;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import static org.ohdsi.webapi.util.SecurityUtils.whitelist;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ohdsi.sql.SqlSplit;
import org.ohdsi.webapi.cohortdefinition.CohortDefinition;
import org.ohdsi.webapi.cohortdefinition.CohortDefinitionRepository;
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
		
		private final CohortDefinitionRepository cohortDefinitionRepository;
    
    public CohortAnalysisTasklet(CohortAnalysisTask task
				, final JdbcTemplate jdbcTemplate
				, final TransactionTemplate transactionTemplate
				, String sourceDialect
				, VisualizationDataRepository visualizationDataRepository
				, CohortDefinitionRepository cohortDefinitionRepository) {
        this.task = task;
        this.jdbcTemplate = jdbcTemplate;
        this.transactionTemplate = transactionTemplate;
        this.analysisRunner = new CohortResultsAnalysisRunner(sourceDialect, visualizationDataRepository);
				this.cohortDefinitionRepository = cohortDefinitionRepository;
    }
    
    @Override
    public RepeatStatus execute(final StepContribution contribution, final ChunkContext chunkContext) throws Exception {
        boolean successful = false;
				String failMessage = null;
        try {
            final int[] ret = this.transactionTemplate.execute(new TransactionCallback<int[]>() {
                
                @Override
                public int[] doInTransaction(final TransactionStatus status) {
                	String cohortSql = CohortAnalysisService.getCohortAnalysisSql(task);
									String[] stmts = SqlSplit.splitSql(cohortSql);
									return CohortAnalysisTasklet.this.jdbcTemplate.batchUpdate(stmts);
							 }
            });
            log.debug("Update count: " + ret.length);
            log.debug("warm up visualizations");
            final int count = this.analysisRunner.warmupData(jdbcTemplate, task);
            log.debug("warmed up " + count + " visualizations");
						successful = true;

        } catch (final TransactionException | DataAccessException e) {
            log.error(whitelist(e));
						failMessage = StringUtils.left(e.getMessage(),2000);
            throw e;//FAIL job status
        } finally {
						// add genrated analysis IDs to  cohort analysis generation info
						final String f_failMessage = failMessage; // assign final var to pass into lambda
						final boolean f_successful = successful; // assign final var to pass into lambda
						
						this.transactionTemplate.execute(status -> { 
							CohortDefinition cohortDef = this.cohortDefinitionRepository.findOne(Integer.parseInt(task.getCohortDefinitionIds().get(0)));
							CohortAnalysisGenerationInfo info = cohortDef.getCohortAnalysisGenerationInfoList().stream()
								.filter(a -> a.getSourceId() == task.getSource().getSourceId())
								.findFirst()
								.orElse(null);

							if (info == null) {
								// initialize new info object
								info = new CohortAnalysisGenerationInfo();
								info.setSourceId(task.getSource().getSourceId());
								info.setCohortDefinition(cohortDef);
								cohortDef.getCohortAnalysisGenerationInfoList().add(info);
							} else {
								info.setExecutionDuration((int)(Calendar.getInstance().getTime().getTime()- info.getLastExecution().getTime()));
							}
							
							if (f_successful) {
								// merge existing analysisIds with analysis Ids generated.
								Set<Integer> generatedIds = Stream.concat(
									task.getAnalysisIds().stream().map(Integer::parseInt), 
									info.getAnalysisIds().stream())
								.collect(Collectors.toSet());
								info.setAnalysisIds(generatedIds);
							} else {
								info.setFailMessage(f_failMessage);
							}
							this.cohortDefinitionRepository.save(cohortDef);
							return null;								
						});					
				}
        return RepeatStatus.FINISHED;
    }    
}
