package org.ohdsi.webapi.common.generation;

import org.ohdsi.webapi.cohortcharacterization.repository.AnalysisGenerationInfoEntityRepository;
import org.ohdsi.webapi.shiro.Entities.UserEntity;
import org.ohdsi.webapi.util.CancelableJdbcTemplate;
import org.slf4j.Logger;
import org.springframework.batch.core.step.tasklet.StoppableTasklet;
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

    protected void saveInfo(Long jobId, String serializedDesign, UserEntity userEntity) {

        AnalysisGenerationInfoEntity generationInfoEntity = new AnalysisGenerationInfoEntity();
        generationInfoEntity.setId(jobId);
        generationInfoEntity.setDesign(serializedDesign);
        generationInfoEntity.setCreatedBy(userEntity);
        analysisGenerationInfoEntityRepository.save(generationInfoEntity);
    }
}
