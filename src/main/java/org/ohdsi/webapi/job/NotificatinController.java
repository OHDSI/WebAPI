package org.ohdsi.webapi.job;

import org.ohdsi.webapi.executionengine.service.ScriptExecutionService;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Path("/notifications")
@Controller
@Transactional
public class NotificatinController {

    private final NotificationService service;
    private final ScriptExecutionService scriptExecutionService;
    private final JobExplorer jobExplorer;

    NotificatinController(final NotificationService service, ScriptExecutionService scriptExecutionService, JobExplorer jobExplorer) {
        this.service = service;
        this.scriptExecutionService = scriptExecutionService;
        this.jobExplorer = jobExplorer;
    }

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public List<JobExecutionResource> list() {
        return service.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    private JobExecutionResource toDTO(JobExecution entity) {
        final JobInstance instance = entity.getJobInstance();
        final JobInstanceResource instanceResource = new JobInstanceResource(instance.getInstanceId(), instance.getJobName());
        final JobExecutionResource result = new JobExecutionResource(instanceResource, entity.getJobId());
        final boolean isScriptExecution = entity.getJobParameters().getString("scriptType") != null;
        if(isScriptExecution) {
            final JobExecution e = jobExplorer.getJobExecution(entity.getJobId());
            final Object executionId = e.getExecutionContext().get("scriptId");
            assert executionId instanceof Long;
            result.setStatus(scriptExecutionService.getExecutionStatus((long) executionId));
        } else {
            result.setStatus(entity.getStatus().name());
        }
        result.setExitStatus(entity.getExitStatus().getExitCode());
        result.setStartDate(entity.getStartTime());
        result.setEndDate(entity.getEndTime());
        result.setJobParametersResource(entity.getJobParameters().getParameters().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getValue())));
        return result;
    }


}
