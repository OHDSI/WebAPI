package org.ohdsi.webapi.evidence.negativecontrols;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import org.ohdsi.sql.SqlSplit;
import org.ohdsi.webapi.GenerationStatus;
import org.ohdsi.webapi.conceptset.ConceptSetGenerationInfo;
import org.ohdsi.webapi.conceptset.ConceptSetGenerationInfoRepository;
import org.ohdsi.webapi.conceptset.ConceptSetGenerationType;
import org.ohdsi.webapi.service.EvidenceService;
import org.ohdsi.webapi.util.CancelableJdbcTemplate;
import org.ohdsi.webapi.util.StatementCancel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

public class NegativeControlTasklet implements Tasklet {

    private static final Logger log = LoggerFactory.getLogger(NegativeControlTasklet.class);

    private final NegativeControlTaskParameters task;

    private final CancelableJdbcTemplate evidenceJdbcTemplate;

    private final org.springframework.jdbc.core.JdbcTemplate ohdsiJdbcTemplate;

    private final TransactionTemplate transactionTemplate;
    
    private final TransactionTemplate transactionTemplateRequiresNew;

    private final ConceptSetGenerationInfoRepository conceptSetGenerationInfoRepository;

    public NegativeControlTasklet(NegativeControlTaskParameters task,
            final CancelableJdbcTemplate evidenceJdbcTemplate,
            final org.springframework.jdbc.core.JdbcTemplate ohdsiJdbcTemplate,
            final TransactionTemplate transactionTemplate,
            final TransactionTemplate transactionTemplateRequiresNew,
            final ConceptSetGenerationInfoRepository repository,
            String sourceDialect) {
        this.task = task;
        this.evidenceJdbcTemplate = evidenceJdbcTemplate;
        this.ohdsiJdbcTemplate = ohdsiJdbcTemplate;
        this.transactionTemplate = transactionTemplate;
        this.transactionTemplateRequiresNew = transactionTemplateRequiresNew;
        this.conceptSetGenerationInfoRepository = repository;
        //this.analysisRunner = new CohortResultsAnalysisRunner(sourceDialect, visualizationDataRepository);
    }

    private ConceptSetGenerationInfo findBySourceId(Collection<ConceptSetGenerationInfo> infoList, Integer sourceId) {
        for (ConceptSetGenerationInfo info : infoList) {
            if (info.getSourceId().equals(sourceId)) {
                return info;
            }
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

        // Save initial status in separate REQUIRES_NEW transaction
        this.transactionTemplateRequiresNew.execute(status -> {
            ConceptSetGenerationInfo info = findBySourceId(this.conceptSetGenerationInfoRepository.findAllByConceptSetId(conceptSetId), sourceId);
            if (info == null) {
                info = new ConceptSetGenerationInfo();
                info.setConceptSetId(conceptSetId);
                info.setSourceId(sourceId);
                info.setGenerationType(ConceptSetGenerationType.NEGATIVE_CONTROLS);
            }
            info.setParams(jobParams.get("params").toString());
            info.setIsValid(false);
            info.setStartTime(startTime);
            info.setStatus(GenerationStatus.RUNNING);
            this.conceptSetGenerationInfoRepository.save(info);
            return null;
        });

        try {
            final int[] ret = this.transactionTemplate.execute(new TransactionCallback<int[]>() {

                @Override
                public int[] doInTransaction(final TransactionStatus status) {
                    int[] result = new int[0];
                    log.debug("Entering tasklet");

                    String negativeControlSql = EvidenceService.getNegativeControlSql(task);
                    log.debug("Processing negative controls with: {}", negativeControlSql);
                    
                    // Split SQL statements and execute them sequentially
                    // This is necessary for databases like Databricks that don't support multiple statements in a single execute call
                    String[] sqlStatements = SqlSplit.splitSql(negativeControlSql);
                    StatementCancel stmtCancel = new StatementCancel();
                    NegativeControlTasklet.this.evidenceJdbcTemplate.batchUpdate(stmtCancel, sqlStatements);

                    return result;
                }
            });
            isValid = true;
        } catch (final TransactionException e) {
            log.error(e.getMessage(), e);
            throw e;//FAIL job status
        } finally {
            // Save completion status in separate REQUIRES_NEW transaction
            final boolean finalIsValid = isValid;
            this.transactionTemplateRequiresNew.execute(status -> {
                ConceptSetGenerationInfo info = findBySourceId(this.conceptSetGenerationInfoRepository.findAllByConceptSetId(conceptSetId), sourceId);
                Date endTime = Calendar.getInstance().getTime();
                info.setExecutionDuration(new Integer((int) (endTime.getTime() - startTime.getTime())));
                info.setIsValid(finalIsValid);
                GenerationStatus genStatus = finalIsValid ? GenerationStatus.COMPLETE : GenerationStatus.ERROR;
                info.setStatus(genStatus);
                this.conceptSetGenerationInfoRepository.save(info);
                return null;
            });
        }
        return RepeatStatus.FINISHED;
    }
}
