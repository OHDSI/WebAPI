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
										String evidenceSchema = source.getTableQualifierOrNull(SourceDaimon.DaimonType.Evidence);
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
										
										// Retrieve the results
										String getNegativeControlResultsSql = EvidenceService.getNegativeControlsFromEvidenceSource(task, evidenceJobId);
										log.debug("Retrieving negative controls from evidence base: \n\t" + getNegativeControlResultsSql);
                    final List<NegativeControlRecord> recs = NegativeControlTasklet.this.evidenceJdbcTemplate.query(getNegativeControlResultsSql, new NegativeControlMapper());
                    
                    // Remove any results that exist for the concept set
                    String deleteSql = EvidenceService.getNegativeControlDeleteStatementSql(task);
                    Object[] params = { task.getConceptSetId(), task.getSource().getSourceId() };
                    int[] types = { Types.INTEGER, Types.INTEGER };
                    int rows = NegativeControlTasklet.this.ohdsiJdbcTemplate.update(deleteSql, params, types);
                    
                    log.debug("rows deleted: " + rows);
                    
                    // Insert the results
                    String insertSql = EvidenceService.getNegativeControlInsertStatementSql(task);
                    int[] recCount = NegativeControlTasklet.this.ohdsiJdbcTemplate.batchUpdate(insertSql, new BatchPreparedStatementSetter() {
                        @Override
                        public void setValues(PreparedStatement ps, int i)
                            throws SQLException {

                            NegativeControlRecord ncr = recs.get(i);
														ps.setLong(1, ncr.getEvidenceJobId());
                            ps.setInt(2, ncr.getSourceId());
                            ps.setInt(3, ncr.getConceptSetId());
                            ps.setString(4, ncr.getConceptSetName());
														ps.setInt(5, ncr.getNegativeControl());
                            ps.setInt(6, ncr.getConceptId());
                            ps.setString(7, ncr.getConceptName());
                            ps.setString(8, ncr.getDomainId());
                            ps.setLong(9, ncr.getSortOrder());
                            ps.setLong(10, ncr.getDescendantPmidCount());
                            ps.setLong(11, ncr.getExactPmidCount());
                            ps.setLong(12, ncr.getParentPmidCount());
                            ps.setLong(13, ncr.getAncestorPmidCount());
                            ps.setInt(14, ncr.getIndCi());
														ps.setInt(15, ncr.getTooBroad());
                            ps.setInt(16, ncr.getDrugInduced());
														ps.setInt(17, ncr.getPregnancy());
                            ps.setLong(18, ncr.getDescendantSplicerCount());
														ps.setLong(19, ncr.getExactSplicerCount());
														ps.setLong(20, ncr.getParentSplicerCount());
														ps.setLong(21, ncr.getAncestorSplicerCount());
														ps.setLong(22,ncr.getDescendantFaersCount());
														ps.setLong(23, ncr.getExactFaersCount());
														ps.setLong(24, ncr.getParentFaersCount());
														ps.setLong(25, ncr.getAncestorFaersCount());
                            ps.setInt(26, ncr.getUserExcluded());
                            ps.setInt(27, ncr.getUserIncluded());
														ps.setInt(28, ncr.getOptimizedOut());
														ps.setInt(29, ncr.getNotPrevalent());
                        }

                        @Override
                        public int getBatchSize() {
                            return recs.size();
                        }
                    });
                    
                    // Clean up the results from the evidence daimon
                    String deleteJobSql = EvidenceService.getJobResultsDeleteStatementSql(evidenceSchema, evidenceJobId);
                    NegativeControlTasklet.this.evidenceJdbcTemplate.execute(deleteJobSql);
                    
                    return recCount;
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
