package org.ohdsi.webapi.executionengine.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ohdsi.webapi.cohortcomparison.ComparativeCohortAnalysisExecutionRepository;
import org.ohdsi.webapi.executionengine.dto.ExecutionRequestDTO;
import org.ohdsi.webapi.executionengine.entity.AnalysisExecution;
import org.ohdsi.webapi.executionengine.entity.AnalysisExecutionType;
import org.ohdsi.webapi.executionengine.entity.AnalysisResultFile;
import org.ohdsi.webapi.executionengine.job.ExecutionEngineCallbackTasklet;
import org.ohdsi.webapi.executionengine.job.RunExecutionEngineTasklet;
import org.ohdsi.webapi.executionengine.repository.AnalysisExecutionRepository;
import org.ohdsi.webapi.executionengine.service.ScriptExecutionService;
import org.ohdsi.webapi.job.JobExecutionResource;
import org.ohdsi.webapi.job.JobTemplate;
import org.ohdsi.webapi.shiro.PermissionManager;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.listener.ExecutionContextPromotionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Path("/execution_service")
public class ScriptExecutionController {

    private final Log logger = LogFactory.getLog(ScriptExecutionController.class);

    @Value("${executionengine.result_callback}")
    private String resultCallback;

    @Value("${executionengine.update_status_callback}")
    private String updateStatusCallback;

    @Autowired
    private ScriptExecutionService scriptExecutionService;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Autowired
    private JobBuilderFactory jobBuilders;

    @Autowired
    private JobTemplate jobTemplate;

    @Autowired
    private ComparativeCohortAnalysisExecutionRepository ccaRepository;

    @Autowired
    private AnalysisExecutionRepository analysisExecutionRepository;
    
    @Autowired
    private PermissionManager permissionManager;

    @Path("run_script")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public JobExecutionResource runScript(ExecutionRequestDTO dto) throws Exception {

        logger.info("Received an execution script to run");

        JobParametersBuilder parametersBuilder = new JobParametersBuilder();
        parametersBuilder.addString("jobName", "Generate estimation (" + dto.sourceKey + ")");
        final JobParameters jobParameters = parametersBuilder.toJobParameters();
        RunExecutionEngineTasklet runExecutionEngineTasklet = new RunExecutionEngineTasklet(scriptExecutionService, dto, permissionManager.getCurrentUser().getId());
        ExecutionEngineCallbackTasklet callbackTasklet = new ExecutionEngineCallbackTasklet(analysisExecutionRepository);

        Step runExecutionStep = stepBuilderFactory.get("executionEngine.start")
                .listener(executionContextPromotionListener())
                .tasklet(runExecutionEngineTasklet)
                .build();

        Step waitCallbackStep = stepBuilderFactory.get("executionEngine.callback")
                .listener(executionContextPromotionListener())
                .tasklet(callbackTasklet)
                .build();

        Job runExecutionJob = jobBuilders.get("executionEngine")
                .start(runExecutionStep)
                .next(waitCallbackStep)
                .build();

        JobExecutionResource executionResource = jobTemplate.launch(runExecutionJob, jobParameters);
        return executionResource;
    }

    private StepExecutionListener executionContextPromotionListener() {

        ExecutionContextPromotionListener listener = new ExecutionContextPromotionListener();
        listener.setKeys(new String[]{ "engineExecutionId" });
        return listener;
    }

    @Path("execution/status/{executionId}")
    @GET
    public String getStatus(@PathParam("executionId") Long executionId) {

        return scriptExecutionService.getExecutionStatus(executionId);
    }

    @Path("execution/results/{executionId}")
    @GET
    @Produces("application/zip")
    public Response getResults(@PathParam("executionId") Long executionId) throws IOException {

        java.nio.file.Path tempDirectory = Files.createTempDirectory("atlas_ee_arch");
        String fileName = "execution_" + executionId + "_result.zip";
        File archive = tempDirectory.resolve(fileName).toFile();

        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(archive))) {
            List<AnalysisResultFile> outputFiles = scriptExecutionService.getExecutionResultFiles(executionId);
            for (AnalysisResultFile resultFile : outputFiles) {
                ZipEntry entry = new ZipEntry(resultFile.getFileName());
                entry.setSize(resultFile.getContents().length);
                zos.putNextEntry(entry);
                zos.write(resultFile.getContents());
                zos.closeEntry();
            }
        }

        Response.ResponseBuilder response = Response.ok(archive);
        response.header("Content-type", "application/zip");
        response.header("Content-Disposition",
                "attachment; filename=\"" + fileName + "\"");
        return response.build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{type}/{id}/executions")
    public Iterable<AnalysisExecution> getAnalysisExecutions(@PathParam("type") AnalysisExecutionType type,
                                                             @PathParam("id") Integer id){

        return analysisExecutionRepository.findByAnalysisIdAndAnalysisType(id, type);
    }
}
