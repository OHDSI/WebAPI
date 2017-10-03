package org.ohdsi.webapi.evidence;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ohdsi.webapi.GenerationStatus;
import org.ohdsi.webapi.conceptset.ConceptSetGenerationInfo;
import org.ohdsi.webapi.conceptset.ConceptSetGenerationInfoRepository;
import org.ohdsi.webapi.conceptset.ConceptSetGenerationType;
import org.ohdsi.webapi.service.EvidenceService;
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
    
    private final NegativeControl task;
       
    private final JdbcTemplate evidenceJdbcTemplate;
    
    private final JdbcTemplate ohdsiJdbcTemplate;
    
    private final TransactionTemplate transactionTemplate;
    
    private final ConceptSetGenerationInfoRepository conceptSetGenerationInfoRepository;
    
    //private final CohortResultsAnalysisRunner analysisRunner;
    
    public NegativeControlTasklet(NegativeControl task, 
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
                    String negativeControlSql = EvidenceService.getNegativeControlSql(task);
                    log.debug("negative control sql to execute: " + negativeControlSql);
                    final List<NegativeControlRecord> recs = NegativeControlTasklet.this.evidenceJdbcTemplate.query(negativeControlSql, new NegativeControlMapper());
                    
                    // Remove any results that exist for the concept set
                    String deleteSql = EvidenceService.getNegativeControlDeleteStatementSql(task);
                    Object[] params = { task.getConceptSetId(), task.getSource().getSourceId() };
                    int[] types = { Types.INTEGER, Types.INTEGER };
                    int rows = NegativeControlTasklet.this.ohdsiJdbcTemplate.update(deleteSql, params, types);
                    
                    log.debug("rows deleted: " + rows);
                    
                    // Insert the results
                    String insertSql = EvidenceService.getNegativeControlInsertStatementSql(task);
                    return NegativeControlTasklet.this.ohdsiJdbcTemplate.batchUpdate(insertSql, new BatchPreparedStatementSetter() {
                        @Override
                        public void setValues(PreparedStatement ps, int i)
                            throws SQLException {

                            NegativeControlRecord ncr = recs.get(i);
                            ps.setInt(1, ncr.getSourceId());
                            ps.setInt(2, ncr.getConceptSetId());
                            ps.setString(3, ncr.getConceptSetName());
                            ps.setInt(4, ncr.getConceptId());
                            ps.setString(5, ncr.getConceptName());
                            ps.setString(6, ncr.getDomainId());
                            ps.setDouble(7, ncr.getMedlineCt());
                            ps.setDouble(8, ncr.getMedlineCase());
                            ps.setDouble(9, ncr.getMedlineOther());
                            ps.setDouble(10, ncr.getSemmeddbCtT());
                            ps.setDouble(11, ncr.getSemmeddbCaseT());
                            ps.setDouble(12, ncr.getSemmeddbOtherT());
                            ps.setDouble(13, ncr.getSemmeddbCtF());
                            ps.setDouble(14, ncr.getSemmeddbCaseF());
                            ps.setDouble(15, ncr.getSemmeddbOtherF());
                            ps.setDouble(16, ncr.getEu_spc());
                            ps.setDouble(17, ncr.getSplADR());
                            ps.setDouble(18, ncr.getAers());
                            ps.setDouble(19, ncr.getAersPRR());
                            ps.setDouble(20, ncr.getMedlineCtScaled());
                            ps.setDouble(21, ncr.getMedlineCaseScaled());
                            ps.setDouble(22, ncr.getMedlineOtherScaled());
                            ps.setDouble(23, ncr.getSemmeddbCtTScaled());
                            ps.setDouble(24, ncr.getSemmeddbCaseTScaled());
                            ps.setDouble(25, ncr.getSemmeddbOtherTScaled());
                            ps.setDouble(26, ncr.getSemmeddbCtFScaled());
                            ps.setDouble(27, ncr.getSemmeddbCaseFScaled());
                            ps.setDouble(28, ncr.getSemmeddbOtherFScaled());
                            ps.setDouble(29, ncr.getEuSPCScaled());
                            ps.setDouble(30, ncr.getSplADRScaled());
                            ps.setDouble(31, ncr.getAersScaled());
                            ps.setDouble(32, ncr.getAersPRRScaled());
                            ps.setDouble(33, ncr.getMedlineCtBeta());
                            ps.setDouble(34, ncr.getMedlineCaseBeta());
                            ps.setDouble(35, ncr.getMedlineOtherBeta());
                            ps.setDouble(36, ncr.getSemmeddbCtTBeta());
                            ps.setDouble(37, ncr.getSemmeddbCaseTBeta());
                            ps.setDouble(38, ncr.getSemmeddbOtherFBeta());
                            ps.setDouble(39, ncr.getSemmeddbCtFBeta());
                            ps.setDouble(40, ncr.getSemmeddbCaseFBeta());
                            ps.setDouble(41, ncr.getSemmeddbOtherFBeta());
                            ps.setDouble(42, ncr.getEuSPCBeta());
                            ps.setDouble(43, ncr.getSplADRBeta());
                            ps.setDouble(44, ncr.getAersBeta());
                            ps.setDouble(45, ncr.getAersPRRBeta());
                            ps.setDouble(46, ncr.getRawPrediction());
                            ps.setDouble(47, ncr.getPrediction());
                        }

                        @Override
                        public int getBatchSize() {
                            return recs.size();
                        }
                    });
                }
            });
            log.debug("Update count: " + ret.length);
            isValid = true;
            /*
            log.debug("warm up visualizations");
            final int count = this.analysisRunner.warmupData(evidenceJdbcTemplate, task);
            log.debug("warmed up " + count + " visualizations");
            */
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
