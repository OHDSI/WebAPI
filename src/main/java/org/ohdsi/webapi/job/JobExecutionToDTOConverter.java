package org.ohdsi.webapi.job;

import org.ohdsi.webapi.Constants;
import org.ohdsi.webapi.converter.BaseConversionServiceAwareConverter;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.stream.Collectors;

@Component
public class JobExecutionToDTOConverter extends BaseConversionServiceAwareConverter<JobExecutionInfo, JobExecutionResource> {
    private final JobExplorer jobExplorer;

    public JobExecutionToDTOConverter(JobExplorer jobExplorer) {
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
        result.setStatus(execution.getStatus().name());
        result.setExitStatus(execution.getExitStatus().getExitCode());
        result.setStartDate(execution.getStartTime() != null ?
            java.util.Date.from(execution.getStartTime().atZone(java.time.ZoneId.systemDefault()).toInstant()) : null);
        result.setEndDate(execution.getEndTime() != null ?
            java.util.Date.from(execution.getEndTime().atZone(java.time.ZoneId.systemDefault()).toInstant()) : null);
        result.setJobParametersResource(
                execution.getJobParameters().getParameters().entrySet()
                .stream()
                .filter(p -> Constants.ALLOWED_JOB_EXECUTION_PARAMETERS.contains(p.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getValue() != null ? e.getValue().getValue() : "null")));
        result.setOwnerType(entity.getOwnerType());
        return result;
    }
}
