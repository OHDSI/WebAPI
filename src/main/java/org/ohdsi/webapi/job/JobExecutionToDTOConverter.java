package org.ohdsi.webapi.job;

import org.ohdsi.webapi.Constants;
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
public class JobExecutionToDTOConverter extends BaseConversionServiceAwareConverter<JobExecutionInfo, JobExecutionResource> {
    private final ScriptExecutionService scriptExecutionService;
    private final JobExplorer jobExplorer;

    public JobExecutionToDTOConverter(ScriptExecutionService scriptExecutionService, JobExplorer jobExplorer) {
        this.scriptExecutionService = scriptExecutionService;
        this.jobExplorer = jobExplorer;
    }

    @Override
    protected JobExecutionResource createResultObject(JobExecutionInfo entity) {
        final JobExecution execution = entity.getJobExecution();
        final JobInstance instance = execution.getJobInstance();
        final JobInstanceResource instanceResource = new JobInstanceResource(instance.getInstanceId(), instance.getJobName());
        return new JobExecutionResource(instanceResource, entity.getJobExecution().getId());
    }

    @Override
    public JobExecutionResource convert(JobExecutionInfo entity) {
        final JobExecutionResource result = createResultObject(entity);
        final JobExecution execution = entity.getJobExecution();
        final boolean isScriptExecution = execution.getJobParameters().getString(ScriptExecutionController.SCRIPT_TYPE) != null;
        if(isScriptExecution) {
            final JobExecution e = jobExplorer.getJobExecution(execution.getJobId());
            final Object executionId = e.getExecutionContext().get(RunExecutionEngineTasklet.SCRIPT_ID);
            result.setStatus(executionId instanceof Long ? scriptExecutionService.getExecutionStatus((long) executionId) : execution.getStatus().name());
        } else {
            result.setStatus(execution.getStatus().name());
        }
        result.setExitStatus(execution.getExitStatus().getExitCode());
        result.setStartDate(execution.getStartTime());
        result.setEndDate(execution.getEndTime());
        result.setJobParametersResource(
                execution.getJobParameters().getParameters().entrySet()
                .stream()
                .filter(p -> Constants.ALLOWED_JOB_EXECUTION_PARAMETERS.contains(p.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getValue() != null ? e.getValue().getValue() : "null")));
        result.setOwnerType(entity.getOwnerType());
        return result;
    }
}
