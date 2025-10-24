package org.ohdsi.webapi.common.generation;

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

    public AnalysisTasklet(Logger log,
                           CancelableJdbcTemplate jdbcTemplate,
                           TransactionTemplate transactionTemplate) {

        super(log, jdbcTemplate, transactionTemplate);
    }

    protected void saveInfoWithinTheSeparateTransaction(Long jobId, String serializedDesign, UserEntity userEntity) {
        // Method body removed - analysis generation info repository no longer available
        // This method will be removed when remaining analysis modules are deleted
        log.warn("saveInfoWithinTheSeparateTransaction called but analysis modules have been removed");
    }
}
