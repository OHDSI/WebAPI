package org.ohdsi.webapi.cohortanalysis;

import java.util.Calendar;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import static org.ohdsi.webapi.util.SecurityUtils.whitelist;

import org.ohdsi.sql.SqlSplit;
import org.ohdsi.webapi.cohortdefinition.CohortDefinition;
import org.ohdsi.webapi.cohortdefinition.CohortDefinitionRepository;
import org.ohdsi.webapi.cohortresults.CohortResultsAnalysisRunner;
import org.ohdsi.webapi.cohortresults.VisualizationDataRepository;
import org.ohdsi.webapi.service.CohortAnalysisService;
import org.ohdsi.webapi.util.BatchStatementExecutorWithProgress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.support.TransactionTemplate;

import javax.ws.rs.NotFoundException;

public class CohortAnalysisTasklet implements Tasklet {
    
    private static final Logger log = LoggerFactory.getLogger(CohortAnalysisTasklet.class);
    
    private final CohortAnalysisTask task;
       
    private final JdbcTemplate jdbcTemplate;
    
    private final TransactionTemplate transactionTemplate;

    private final TransactionTemplate transactionTemplateRequiresNew;
    
    private final CohortResultsAnalysisRunner analysisRunner;
		
		private final CohortDefinitionRepository cohortDefinitionRepository;

		private final HeraclesQueryBuilder heraclesQueryBuilder;

	public CohortAnalysisTasklet(CohortAnalysisTask task
					, final JdbcTemplate jdbcTemplate
					, final TransactionTemplate transactionTemplate
					, final TransactionTemplate transactionTemplateRequiresNew
					, String sourceDialect
					, VisualizationDataRepository visualizationDataRepository
					, CohortDefinitionRepository cohortDefinitionRepository
					, final ObjectMapper objectMapper
					, HeraclesQueryBuilder heraclesQueryBuilder) {
        this.task = task;
        this.jdbcTemplate = jdbcTemplate;
        this.transactionTemplate = transactionTemplate;
        this.transactionTemplateRequiresNew = transactionTemplateRequiresNew;
		this.heraclesQueryBuilder = heraclesQueryBuilder;
		this.analysisRunner = new CohortResultsAnalysisRunner(sourceDialect, visualizationDataRepository, objectMapper);
				this.cohortDefinitionRepository = cohortDefinitionRepository;
	}
    
    @Override
    public RepeatStatus execute(final StepContribution contribution, final ChunkContext chunkContext) throws Exception {
        boolean successful = false;
				String failMessage = null;
				Integer cohortDefinitionId = Integer.parseInt(task.getCohortDefinitionIds().get(0));
				this.transactionTemplate.execute(status -> {
					CohortDefinition cohortDef = cohortDefinitionRepository.findOne(cohortDefinitionId);
					CohortAnalysisGenerationInfo gi = cohortDef.getCohortAnalysisGenerationInfoList().stream()
													.filter(a -> a.getSourceId() == task.getSource().getSourceId())
													.findFirst()
													.orElseGet(() -> {
														CohortAnalysisGenerationInfo genInfo = new CohortAnalysisGenerationInfo();
														genInfo.setSourceId(task.getSource().getSourceId());
														genInfo.setCohortDefinition(cohortDef);
														cohortDef.getCohortAnalysisGenerationInfoList().add(genInfo);
														return genInfo;
													});
					gi.setProgress(0);
					cohortDefinitionRepository.save(cohortDef);
					return gi;
				});
        try {
						final String cohortSql = heraclesQueryBuilder.buildHeraclesAnalysisQuery(task);
						BatchStatementExecutorWithProgress executor = new BatchStatementExecutorWithProgress(
										SqlSplit.splitSql(cohortSql),
										transactionTemplate,
										jdbcTemplate);
						int[] ret = executor.execute(progress -> {

							transactionTemplateRequiresNew.execute(status -> {
								CohortDefinition cohortDef = cohortDefinitionRepository.findOne(cohortDefinitionId);
								CohortAnalysisGenerationInfo info = cohortDef.getCohortAnalysisGenerationInfoList().stream()
												.filter(a -> a.getSourceId() == task.getSource().getSourceId())
												.findFirst().orElseThrow(NotFoundException::new);
								info.setProgress(progress);
								cohortDefinitionRepository.save(cohortDef);
								return null;
							});
						});
						if (log.isDebugEnabled()) {
							log.debug("Update count: {}", ret.length);
							log.debug("Warming up visualizations");
						}
						final int count = this.analysisRunner.warmupData(jdbcTemplate, task);
						if (log.isDebugEnabled()) {
							log.debug("Warmed up {} visualizations", count);
						}
						successful = true;
        } catch (final TransactionException | DataAccessException e) {
            log.error(whitelist(e));
						failMessage = StringUtils.left(e.getMessage(),2000);
            throw e;//FAIL job status
        } finally {
						// add generated analysis IDs to  cohort analysis generation info
						final String f_failMessage = failMessage; // assign final var to pass into lambda
						final boolean f_successful = successful; // assign final var to pass into lambda
						
						this.transactionTemplateRequiresNew.execute(status -> {

							CohortDefinition cohortDef = cohortDefinitionRepository.findOne(cohortDefinitionId);
							CohortAnalysisGenerationInfo info = cohortDef.getCohortAnalysisGenerationInfoList().stream()
											.filter(a -> a.getSourceId() == task.getSource().getSourceId())
											.findFirst().orElseThrow(NotFoundException::new);
							info.setExecutionDuration((int)(Calendar.getInstance().getTime().getTime()- info.getLastExecution().getTime()));

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
