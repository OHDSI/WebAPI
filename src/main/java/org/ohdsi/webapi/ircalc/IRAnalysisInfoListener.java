package org.ohdsi.webapi.ircalc;

import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraph;
import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraphUtils;
import org.ohdsi.webapi.Constants;
import org.ohdsi.webapi.GenerationStatus;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.JobParameters;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;

public class IRAnalysisInfoListener implements JobExecutionListener {

    private static final int MAX_MESSAGE_LENGTH = 2000;
    private static final EntityGraph IR_WITH_EXECUTION_INFOS_ENTITY_GRAPH = EntityGraphUtils.fromName("IncidenceRateAnalysis.withExecutionInfoList");

    private final TransactionTemplate transactionTemplate;
    private final IncidenceRateAnalysisRepository incidenceRateAnalysisRepository;
    private Date startTime;

    public IRAnalysisInfoListener(TransactionTemplate transactionTemplate, IncidenceRateAnalysisRepository incidenceRateAnalysisRepository) {

        this.transactionTemplate = transactionTemplate;
        this.incidenceRateAnalysisRepository = incidenceRateAnalysisRepository;
    }

    @Override
    public void beforeJob(JobExecution je) {

        startTime = Calendar.getInstance().getTime();
        JobParameters jobParams = je.getJobParameters();
        Integer analysisId = Integer.valueOf(jobParams.getString("analysis_id"));
        Integer sourceId = Integer.valueOf(jobParams.getString("source_id"));

        DefaultTransactionDefinition requresNewTx = new DefaultTransactionDefinition();
        requresNewTx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        TransactionStatus initStatus = this.transactionTemplate.getTransactionManager().getTransaction(requresNewTx);
        IncidenceRateAnalysis analysis = this.incidenceRateAnalysisRepository.findOneWithExecutionsOnExistingSources(analysisId,
                IR_WITH_EXECUTION_INFOS_ENTITY_GRAPH);

        findExecutionInfoBySourceId(analysis.getExecutionInfoList(), sourceId).ifPresent(analysisInfo -> {
            analysisInfo.setIsValid(false);
            analysisInfo.setStartTime(startTime);
            analysisInfo.setStatus(GenerationStatus.RUNNING);
        });

        this.incidenceRateAnalysisRepository.save(analysis);
        this.transactionTemplate.getTransactionManager().commit(initStatus);
    }

    @Override
    public void afterJob(JobExecution je) {

        boolean isValid = !(je.getStatus() == BatchStatus.FAILED || je.getStatus() == BatchStatus.STOPPED);
        String statusMessage = je.getExitStatus().getExitDescription();

        JobParameters jobParams = je.getJobParameters();
        Integer analysisId = Integer.valueOf(jobParams.getString("analysis_id"));
        Integer sourceId = Integer.valueOf(jobParams.getString("source_id"));

        DefaultTransactionDefinition requresNewTx = new DefaultTransactionDefinition();
        requresNewTx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        TransactionStatus completeStatus = this.transactionTemplate.getTransactionManager().getTransaction(requresNewTx);
        Date endTime = Calendar.getInstance().getTime();
        IncidenceRateAnalysis analysis = this.incidenceRateAnalysisRepository.findOneWithExecutionsOnExistingSources(analysisId,
                IR_WITH_EXECUTION_INFOS_ENTITY_GRAPH);

        findExecutionInfoBySourceId(analysis.getExecutionInfoList(), sourceId).ifPresent(analysisInfo -> {
            analysisInfo.setIsValid(isValid);
			analysisInfo.setCanceled(je.getStatus() == BatchStatus.STOPPED || je.getStepExecutions().stream().anyMatch(se -> Objects.equals(Constants.CANCELED, se.getExitStatus().getExitCode())));
            analysisInfo.setExecutionDuration((int) (endTime.getTime() - startTime.getTime()));
            analysisInfo.setStatus(GenerationStatus.COMPLETE);
            analysisInfo.setMessage(statusMessage.substring(0, Math.min(MAX_MESSAGE_LENGTH, statusMessage.length())));
        });

        this.incidenceRateAnalysisRepository.save(analysis);
        this.transactionTemplate.getTransactionManager().commit(completeStatus);
    }

    private Optional<ExecutionInfo> findExecutionInfoBySourceId(Collection<ExecutionInfo> infoList, Integer sourceId) {

        return infoList.stream()
                .filter(info -> Objects.equals(info.getId().getSourceId(), sourceId))
                .findFirst();
    }
}
