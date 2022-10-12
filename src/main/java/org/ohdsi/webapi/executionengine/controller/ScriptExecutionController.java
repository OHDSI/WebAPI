package org.ohdsi.webapi.executionengine.controller;

import org.ohdsi.webapi.executionengine.repository.AnalysisExecutionRepository;
import org.ohdsi.webapi.executionengine.service.ExecutionEngineStatus;
import org.ohdsi.webapi.executionengine.service.ExecutionEngineStatusService;
import org.ohdsi.webapi.executionengine.service.ScriptExecutionService;
import org.ohdsi.webapi.job.GeneratesNotification;
import org.ohdsi.webapi.job.JobTemplate;
import org.ohdsi.webapi.source.SourceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * REST Services related to working with Arachne Execution Engine
 * Services
 * 
 * @summary Arachne Execution Engine
 */
@Component
@Path("/executionservice")
public class ScriptExecutionController implements GeneratesNotification {

    public static final String SCRIPT_TYPE = "scriptType";
    private static final String FOLDING_KEY = "foldingKey";
    private static final String NAME = "executionEngine";
    private static final String COHORT_ID = "cohortId";
    private final Logger logger = LoggerFactory.getLogger(ScriptExecutionController.class);

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

    /**
     * Get the execution status by execution ID
     * 
     * @summary Get an execution status by ID
     * @param executionId The execution ID
     * @return The status
     */
    @Path("execution/status/{executionId}")
    @GET
    public String getStatus(@PathParam("executionId") Long executionId) {

        return scriptExecutionService.getExecutionStatus(executionId);
    }

    /**
     * Get the execution status of the Arachne Execution Engine
     * 
     * @summary Get Arachne Execution Engine status
     * @return The StatusResponse
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("status")
    public StatusResponse getExecutionEngineStatus(){

        return new StatusResponse(executionEngineStatusService.getExecutionEngineStatus());
    }

    @Override
    public String getJobName() {
        return NAME;
    }

    @Override
    public String getExecutionFoldingKey() {
        return FOLDING_KEY;
    }

    private class StatusResponse {
        public StatusResponse(final ExecutionEngineStatus status) {
            this.status = status;
        }
        public ExecutionEngineStatus status;
    }
}
