package org.ohdsi.webapi.executionengine.controller;

import static org.ohdsi.webapi.Constants.Params.JOB_NAME;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.persistence.EntityManager;
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
import org.ohdsi.webapi.executionengine.job.CreateAnalysisTasklet;
import org.ohdsi.webapi.executionengine.job.ExecutionEngineCallbackTasklet;
import org.ohdsi.webapi.executionengine.job.RunExecutionEngineTasklet;
import org.ohdsi.webapi.executionengine.repository.AnalysisExecutionRepository;
import org.ohdsi.webapi.executionengine.service.ExecutionEngineStatus;
import org.ohdsi.webapi.executionengine.service.ExecutionEngineStatusService;
import org.ohdsi.webapi.executionengine.service.ScriptExecutionService;
import org.ohdsi.webapi.job.JobExecutionResource;
import org.ohdsi.webapi.job.JobTemplate;
import org.ohdsi.webapi.shiro.management.Security;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Path("/executionservice")
public class ScriptExecutionController {

    private final Log logger = LogFactory.getLog(ScriptExecutionController.class);

    @Value("${executionengine.resultCallback}")
    private String resultCallback;

    @Value("${executionengine.updateStatusCallback}")
    private String updateStatusCallback;

    private final ScriptExecutionService scriptExecutionService;
    private final StepBuilderFactory stepBuilderFactory;
    private final JobBuilderFactory jobBuilders;
    private final JobTemplate jobTemplate;
    private final AnalysisExecutionRepository analysisExecutionRepository;
    private final EntityManager entityManager;
    private final ExecutionEngineStatusService executionEngineStatusService;
    private SourceRepository sourceRepository;

    @Autowired
    public ScriptExecutionController(final ScriptExecutionService scriptExecutionService,
                                     final StepBuilderFactory stepBuilderFactory,
                                     final JobBuilderFactory jobBuilders,
                                     final JobTemplate jobTemplate,
                                     final AnalysisExecutionRepository analysisExecutionRepository,
                                     final EntityManager entityManager,
                                     final ExecutionEngineStatusService executionEngineStatusService,
                                     final SourceRepository sourceRepository) {

        this.scriptExecutionService = scriptExecutionService;
        this.stepBuilderFactory = stepBuilderFactory;
        this.jobBuilders = jobBuilders;
        this.jobTemplate = jobTemplate;
        this.analysisExecutionRepository = analysisExecutionRepository;
        this.entityManager = entityManager;
        this.executionEngineStatusService = executionEngineStatusService;
        this.sourceRepository = sourceRepository;
    }

    @Path("execution/run")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public JobExecutionResource runScript(ExecutionRequestDTO dto) {

        logger.info("Received an execution script to run");
        Source source = sourceRepository.findBySourceKey(dto.sourceKey);

        JobParametersBuilder parametersBuilder = new JobParametersBuilder();
        parametersBuilder.addString(JOB_NAME, String.format("Generate %s %d: %s (%s)", dto.analysisType, dto.cohortId,
                source.getSourceName(), dto.sourceKey));
        final JobParameters jobParameters = parametersBuilder.toJobParameters();

        RunExecutionEngineTasklet runExecutionEngineTasklet = new RunExecutionEngineTasklet(scriptExecutionService, dto);
        ExecutionEngineCallbackTasklet callbackTasklet = new ExecutionEngineCallbackTasklet(analysisExecutionRepository, entityManager);

        CreateAnalysisTasklet createAnalysisTasklet = new CreateAnalysisTasklet(scriptExecutionService, dto);

        Step analysisCreationStep = stepBuilderFactory.get("executionEngine.create-analysis")
                .tasklet(createAnalysisTasklet)
                .build();

        Step runExecutionStep = stepBuilderFactory.get("executionEngine.start")
                .tasklet(runExecutionEngineTasklet)
                .build();

        Step waitCallbackStep = stepBuilderFactory.get("executionEngine.callback")
                .tasklet(callbackTasklet)
                .build();

        Job runExecutionJob = jobBuilders.get("executionEngine")
                .start(analysisCreationStep)
                .next(runExecutionStep)
                .next(waitCallbackStep)
                .build();

        return jobTemplate.launch(runExecutionJob, jobParameters);
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

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("status")
    public StatusResponse getExecutionEngineStatus(){

        return new StatusResponse(executionEngineStatusService.getExecutionEngineStatus());
    }

    private class StatusResponse {
        public StatusResponse(final ExecutionEngineStatus status) {
            this.status = status;
        }
        public ExecutionEngineStatus status;
    }
}
