package org.ohdsi.webapi.job;

import org.ohdsi.webapi.converter.BaseConversionServiceAwareConverter;
import org.ohdsi.webapi.executionengine.controller.ScriptExecutionController;
import org.ohdsi.webapi.executionengine.job.RunExecutionEngineTasklet;
import org.ohdsi.webapi.executionengine.service.ScriptExecutionService;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.stream.Collectors;

@Component
public class JobExecutionToDTOConverter extends BaseConversionServiceAwareConverter<JobExecution, JobExecutionResource> {
    private final ScriptExecutionService scriptExecutionService;
    private final JobExplorer jobExplorer;

    public JobExecutionToDTOConverter(ScriptExecutionService scriptExecutionService, JobExplorer jobExplorer) {
        this.scriptExecutionService = scriptExecutionService;
        this.jobExplorer = jobExplorer;
    }

    @Override
    protected JobExecutionResource createResultObject(JobExecution entity) {
        final JobInstance instance = entity.getJobInstance();
        final JobInstanceResource instanceResource = new JobInstanceResource(instance.getInstanceId(), instance.getJobName());
        return new JobExecutionResource(instanceResource, entity.getJobId());
    }

    @Override
    public JobExecutionResource convert(JobExecution entity) {
        final JobExecutionResource result = createResultObject(entity);
        final boolean isScriptExecution = entity.getJobParameters().getString(ScriptExecutionController.SCRIPT_TYPE) != null;
        if(isScriptExecution) {
            final JobExecution e = jobExplorer.getJobExecution(entity.getJobId());
            final Object executionId = e.getExecutionContext().get(RunExecutionEngineTasklet.SCRIPT_ID);
            result.setStatus(executionId instanceof Long ? scriptExecutionService.getExecutionStatus((long) executionId) : entity.getStatus().name());
        } else {
            result.setStatus(entity.getStatus().name());
        }
        result.setExitStatus(entity.getExitStatus().getExitCode());
        result.setStartDate(entity.getStartTime());
        result.setEndDate(entity.getEndTime());
        result.setJobParametersResource(
                entity.getJobParameters().getParameters().entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getValue() != null ? e.getValue().getValue() : "null")));
        return result;
    }
}
