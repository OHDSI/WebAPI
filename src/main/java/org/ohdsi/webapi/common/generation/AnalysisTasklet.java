package org.ohdsi.webapi.common.generation;

import org.ohdsi.webapi.cohortcharacterization.repository.AnalysisGenerationInfoEntityRepository;
import org.ohdsi.webapi.exception.AtlasException;
import org.ohdsi.webapi.shiro.Entities.UserEntity;
import org.ohdsi.webapi.util.CancelableJdbcTemplate;
import org.slf4j.Logger;
import org.springframework.batch.core.step.tasklet.StoppableTasklet;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

public abstract class AnalysisTasklet extends CancelableTasklet implements StoppableTasklet {

    protected final AnalysisGenerationInfoEntityRepository analysisGenerationInfoEntityRepository;

    public AnalysisTasklet(Logger log,
                           CancelableJdbcTemplate jdbcTemplate,
                           TransactionTemplate transactionTemplate,
                           AnalysisGenerationInfoEntityRepository analysisGenerationInfoEntityRepository) {

        super(log, jdbcTemplate, transactionTemplate);
        this.analysisGenerationInfoEntityRepository = analysisGenerationInfoEntityRepository;
    }

    protected void saveInfoWithinTheSeparateTransaction(Long jobId, String serializedDesign, UserEntity userEntity) {
        DefaultTransactionDefinition txDefinition = new DefaultTransactionDefinition();
        txDefinition.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        TransactionStatus infoSaveTx = null;
        try {
            infoSaveTx = this.transactionTemplate.getTransactionManager().getTransaction(txDefinition);
            saveInfo(jobId, serializedDesign, userEntity);
            this.transactionTemplate.getTransactionManager().commit(infoSaveTx);
        } catch (Exception ex) {
            log.error("Cannot save sourceInfo for the job: {} ", jobId, ex);
            this.transactionTemplate.getTransactionManager().rollback(infoSaveTx);
            throw new AtlasException(ex);
        }
    }

    private void saveInfo(Long jobId, String serializedDesign, UserEntity userEntity) {
        AnalysisGenerationInfoEntity generationInfoEntity = new AnalysisGenerationInfoEntity();
        generationInfoEntity.setId(jobId);
        generationInfoEntity.setDesign(serializedDesign);
        generationInfoEntity.setCreatedBy(userEntity);
        analysisGenerationInfoEntityRepository.save(generationInfoEntity);
    }
}
