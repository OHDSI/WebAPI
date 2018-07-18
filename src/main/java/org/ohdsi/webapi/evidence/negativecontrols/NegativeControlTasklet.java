package org.ohdsi.webapi.evidence.negativecontrols;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ohdsi.webapi.GenerationStatus;
import org.ohdsi.webapi.conceptset.ConceptSetGenerationInfo;
import org.ohdsi.webapi.conceptset.ConceptSetGenerationInfoRepository;
import org.ohdsi.webapi.conceptset.ConceptSetGenerationType;
import org.ohdsi.webapi.service.EvidenceService;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceDaimon;
import org.ohdsi.webapi.util.PreparedStatementRenderer;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

public class NegativeControlTasklet implements Tasklet {
    private static final Log log = LogFactory.getLog(NegativeControlTasklet.class);
    
    private final NegativeControlTaskParameters task;
       
    private final JdbcTemplate evidenceJdbcTemplate;
    
    private final JdbcTemplate ohdsiJdbcTemplate;
    
    private final TransactionTemplate transactionTemplate;
    
    private final ConceptSetGenerationInfoRepository conceptSetGenerationInfoRepository;
    
    //private final CohortResultsAnalysisRunner analysisRunner;
    
    public NegativeControlTasklet(NegativeControlTaskParameters task, 
            final JdbcTemplate evidenceJdbcTemplate, 
            final JdbcTemplate ohdsiJdbcTemplate,
            final TransactionTemplate transactionTemplate, 
            final ConceptSetGenerationInfoRepository repository,
        String sourceDialect) 
    {
        this.task = task;
        this.evidenceJdbcTemplate = evidenceJdbcTemplate;
        this.ohdsiJdbcTemplate = ohdsiJdbcTemplate;
        this.transactionTemplate = transactionTemplate;
        this.conceptSetGenerationInfoRepository = repository;
        //this.analysisRunner = new CohortResultsAnalysisRunner(sourceDialect, visualizationDataRepository);
    }
    
    private ConceptSetGenerationInfo findBySourceId(Collection<ConceptSetGenerationInfo> infoList, Integer sourceId)
    {
      for (ConceptSetGenerationInfo info : infoList) {
        if (info.getSourceId().equals(sourceId))
          return info;
      }
      return null;
    }    
    
    @Override
    public RepeatStatus execute(final StepContribution contribution, final ChunkContext chunkContext) throws Exception {
        Date startTime = Calendar.getInstance().getTime();
        Map<String, Object> jobParams = chunkContext.getStepContext().getJobParameters();
        Integer conceptSetId = Integer.valueOf(jobParams.get("concept_set_id").toString());
        final Integer sourceId = Integer.valueOf(jobParams.get("source_id").toString());
        boolean isValid = false;
        
        DefaultTransactionDefinition initTx = new DefaultTransactionDefinition();
        initTx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        TransactionStatus initStatus = this.transactionTemplate.getTransactionManager().getTransaction(initTx);
        ConceptSetGenerationInfo info = findBySourceId(this.conceptSetGenerationInfoRepository.findAllByConceptSetId(conceptSetId), sourceId);
        if (info == null) {
            info = new ConceptSetGenerationInfo();
            info.setConceptSetId(conceptSetId);
            info.setSourceId(sourceId);
            info.setGenerationType(ConceptSetGenerationType.NEGATIVE_CONTROLS);
        }
				info.setParams(jobParams.get("params").toString());
        info.setIsValid(isValid);
        info.setStartTime(startTime);
        info.setStatus(GenerationStatus.RUNNING);
        this.conceptSetGenerationInfoRepository.save(info);
        this.transactionTemplate.getTransactionManager().commit(initStatus);
        
        try {
            final int[] ret = this.transactionTemplate.execute(new TransactionCallback<int[]>() {
                
                @Override
                public int[] doInTransaction(final TransactionStatus status) {	
                    log.debug("entering tasklet");
                    log.debug("creating ID for job");
                    Long evidenceJobId = null;
                    Source source = task.getSource();
                    String resultsSchema = source.getTableQualifierOrNull(SourceDaimon.DaimonType.Results);
                    String sql = EvidenceService.getEvidenceJobIdSql(task);
                    try {
                            Connection conn = NegativeControlTasklet.this.evidenceJdbcTemplate.getDataSource().getConnection();
                            PreparedStatement ps = conn.prepareStatement(sql, new String[] { "id"});
                            if (ps.executeUpdate() > 0) {
                                    ResultSet generatedKeys = ps.getGeneratedKeys();
                                    if (generatedKeys != null && generatedKeys.next()) {
                                            evidenceJobId = generatedKeys.getLong(1);
                                    }
                            }
                            log.debug("evidenceJobId: " + evidenceJobId);
                    } catch (SQLException ex) {
                            Logger.getLogger(NegativeControlTasklet.class.getName()).log(Level.SEVERE, null, ex);
                    }
										
                    String negativeControlSql = EvidenceService.getNegativeControlSql(task, evidenceJobId);
                    log.debug("process negative controls with: \n\t" + negativeControlSql);
                    NegativeControlTasklet.this.evidenceJdbcTemplate.execute(negativeControlSql);

                    // Remove any results that exist for the concept set
                    String deleteSql = EvidenceService.getNegativeControlDeleteStatementSql(task);
                    Object[] params = { task.getConceptSetId(), task.getSource().getSourceId() };
                    int[] types = { Types.INTEGER, Types.INTEGER };
                    int rows = NegativeControlTasklet.this.ohdsiJdbcTemplate.update(deleteSql, params, types);
                    
                    log.debug("rows deleted: " + rows);
                    
                    // Insert the results
                    String insertSql = EvidenceService.getNegativeControlInsertStatementSql(task);
                    params = new Object[] { evidenceJobId, task.getSource().getSourceId(), task.getConceptSetId() };
                    types = new int[] { Types.BIGINT, Types.INTEGER, Types.INTEGER };
                    rows = NegativeControlTasklet.this.ohdsiJdbcTemplate.update(insertSql, params, types);
                    
                    log.debug("rows inserted: " + rows);
                    
                    // Clean up the results from any previous runs
                    if (task.getPreviousJobId() > 0) {
                        String deleteJobSql = EvidenceService.getJobResultsDeleteStatementSql(resultsSchema, task.getPreviousJobId());
                        NegativeControlTasklet.this.evidenceJdbcTemplate.execute(deleteJobSql);
                    }
                    
                    return null;
                }
            });
            isValid = true;
        } catch (final TransactionException e) {
            log.error(e.getMessage(), e);
            throw e;//FAIL job status
        } finally {
            DefaultTransactionDefinition completeTx = new DefaultTransactionDefinition();
            completeTx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
            TransactionStatus completeStatus = this.transactionTemplate.getTransactionManager().getTransaction(completeTx);      
            info = findBySourceId(this.conceptSetGenerationInfoRepository.findAllByConceptSetId(conceptSetId), sourceId);
            Date endTime = Calendar.getInstance().getTime();
            info.setExecutionDuration(new Integer((int)(endTime.getTime() - startTime.getTime())));
            info.setIsValid(isValid);
            GenerationStatus status = isValid ? GenerationStatus.COMPLETE : GenerationStatus.ERROR;
            info.setStatus(status);
            this.conceptSetGenerationInfoRepository.save(info);
            this.transactionTemplate.getTransactionManager().commit(completeStatus);            
        }
        return RepeatStatus.FINISHED;
    }    
}
