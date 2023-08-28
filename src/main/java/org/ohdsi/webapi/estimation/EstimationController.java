package org.ohdsi.webapi.estimation;

import com.odysseusinc.arachne.commons.utils.ConverterUtils;
import com.qmino.miredot.annotations.MireDotIgnore;
import com.qmino.miredot.annotations.ReturnType;
import org.ohdsi.webapi.Constants;
import org.ohdsi.webapi.check.CheckResult;
import org.ohdsi.webapi.check.checker.estimation.EstimationChecker;
import org.ohdsi.webapi.common.SourceMapKey;
import org.ohdsi.webapi.common.generation.ExecutionBasedGenerationDTO;
import org.ohdsi.webapi.common.sensitiveinfo.CommonGenerationSensitiveInfoService;
import org.ohdsi.webapi.estimation.domain.EstimationGenerationEntity;
import org.ohdsi.webapi.estimation.dto.EstimationDTO;
import org.ohdsi.webapi.estimation.dto.EstimationShortDTO;
import org.ohdsi.webapi.estimation.specification.EstimationAnalysisImpl;
import org.ohdsi.webapi.executionengine.service.ScriptExecutionService;
import org.ohdsi.webapi.job.JobExecutionResource;
import org.ohdsi.webapi.security.PermissionService;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceService;
import org.ohdsi.webapi.util.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.stereotype.Controller;

import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

 /**
  * Provides REST services for working with
  * population-level estimation designs.
  * 
  * @summary Estimation
  */
@Controller
@Path("/estimation/")
public class EstimationController {

  private static final Logger LOGGER = LoggerFactory.getLogger(EstimationController.class);
  private static final String NO_ESTIMATION_MESSAGE = "There is no estimation with id = %d.";
  private static final String NO_GENERATION_MESSAGE = "There is no generation with id = %d";
  private final EstimationService service;
  private final GenericConversionService conversionService;
  private final CommonGenerationSensitiveInfoService<ExecutionBasedGenerationDTO> sensitiveInfoService;
  private final SourceService sourceService;
  private final ConverterUtils converterUtils;
  private final ScriptExecutionService executionService;
  private EstimationChecker checker;
  private PermissionService permissionService;
  
  @Value("${security.defaultGlobalReadPermissions}")
  private boolean defaultGlobalReadPermissions;
  
  public EstimationController(EstimationService service,
                              GenericConversionService conversionService,
                              CommonGenerationSensitiveInfoService sensitiveInfoService,
                              SourceService sourceService,
                              ConverterUtils converterUtils,
                              ScriptExecutionService executionService, EstimationChecker checker,
                              PermissionService permissionService) {
    this.service = service;
    this.conversionService = conversionService;
    this.sensitiveInfoService = sensitiveInfoService;
    this.sourceService = sourceService;
    this.converterUtils = converterUtils;
    this.executionService = executionService;
    this.checker = checker;
    this.permissionService = permissionService;
  }

  /**
   * Used to retrieve all estimation designs in the WebAPI database.
   * @summary Get all estimation designs
   * @return A list of all estimation design names and identifiers
   */
  @GET
  @Path("/")
  @Produces(MediaType.APPLICATION_JSON)
  public List<EstimationShortDTO> getAnalysisList() {
    return StreamSupport.stream(service.getAnalysisList().spliterator(), false)
            .filter(!defaultGlobalReadPermissions ? entity -> permissionService.hasReadAccess(entity) : entity -> true)
            .map(analysis -> {
              EstimationShortDTO dto = conversionService.convert(analysis, EstimationShortDTO.class);
              permissionService.fillWriteAccess(analysis, dto);
              permissionService.fillReadAccess(analysis, dto);
              return dto;
            })
            .collect(Collectors.toList());
  }

  /**
   * Check to see if an estimation design exists by name
   * 
   * @summary Estimation design exists by name
   * @param id The estimation design id
   * @param name The estimation design name
   * @return 1 if an estimation design with the given name and id exist in WebAPI and 0 otherwise
   */
  @GET
  @Path("/{id}/exists")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public int getCountEstimationWithSameName(@PathParam("id") @DefaultValue("0") final int id, @QueryParam("name") String name) {
    return service.getCountEstimationWithSameName(id, name);
  }

  /**
   * Used to delete a selected estimation design by ID.
   * 
   * @summary Delete an estimation designs
   * @param id The identifier of the estimation design
   * @return None
   */
  @DELETE
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/{id}")
  public void delete(@PathParam("id") final int id) {

    service.delete(id);
  }

  /**
   * Used to add a new estimation design to the database
   * 
   * @summary Save a new estimation design
   * @param est The estimation design object
   * @return An EstimationDTO which contains the identifier assigned to the estimation design.
   */
  @POST
  @Path("/")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public EstimationDTO createEstimation(Estimation est) throws Exception {

    Estimation estWithId = service.createEstimation(est);
    return reloadAndConvert(estWithId.getId());
  }

  /**
   * Used to save changes to an existing estimation design by ID.
   * 
   * @summary Update an estimation design
   * @param id The ID of the estimation design
   * @param est The estimation design object
   * @return An EstimationDTO which contains the updated estimation design.
   */
  @PUT
  @Path("{id}")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public EstimationDTO updateEstimation(@PathParam("id") final int id, Estimation est) throws Exception {

    service.updateEstimation(id, est);
    return reloadAndConvert(id);
  }

  /**
   * Used to create a copy of an existing existing estimation design by ID.
   * 
   * @summary Copy an estimation design
   * @param id The ID of the estimation design
   * @return An EstimationDTO which contains the newly copied estimation design.
   */
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/{id}/copy")
  @Transactional
  public EstimationDTO copy(@PathParam("id") final int id) throws Exception {

    Estimation est = service.copy(id);
    return reloadAndConvert(est.getId());
  }

  /**
   * Used to retrieve an existing existing estimation design by ID.
   * 
   * @summary Get an estimation design by ID
   * @param id The ID of the estimation design
   * @return An EstimationDTO which contains the estimation design.
   */
  @GET
  @Path("{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public EstimationDTO getAnalysis(@PathParam("id") int id) {

    Estimation est = service.getAnalysis(id);
    ExceptionUtils.throwNotFoundExceptionIfNull(est, String.format(NO_ESTIMATION_MESSAGE, id));
    return conversionService.convert(est, EstimationDTO.class);
  }

  /**
   * Used to export an existing existing estimation design by ID. This is used
   * when transferring the object outside of WebAPI.
   * 
   * @summary Export an estimation design
   * @param id The ID of the estimation design
   * @return An EstimationAnalysisImpl which resolves all references to cohorts, concept sets, etc
   * and contains the full estimation design for export.
   */
  @GET
  @Path("{id}/export")
  @Produces(MediaType.APPLICATION_JSON)
  @ReturnType("java.lang.Object")
  public EstimationAnalysisImpl exportAnalysis(@PathParam("id") int id) {

    Estimation estimation = service.getAnalysis(id);
    ExceptionUtils.throwNotFoundExceptionIfNull(estimation, String.format(NO_ESTIMATION_MESSAGE, id));
    return service.exportAnalysis(estimation);
  }
  
  /**
   * Import a full estimation design
   * 
   * @summary Import an estimation design
   * @param analysis The full estimation design
   * @return The newly imported estimation
   */
  @POST
  @Path("import")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @MireDotIgnore // @BodyType("java.lang.Object") doesn't fix the issue
  public EstimationDTO importAnalysis(EstimationAnalysisImpl analysis) throws Exception {

      if (Objects.isNull(analysis)) {
          LOGGER.error("Failed to import Estimation, empty or not valid source JSON");
          throw new InternalServerErrorException();
      }
      Estimation importedEstimation = service.importAnalysis(analysis);
      return reloadAndConvert(importedEstimation.getId());
  }

  /**
   * Download an R package to execute the estimation study
   * 
   * @summary Download an estimation R package
   * @param id The id for the estimation study
   * @param packageName The R package name for the study
   * @return Binary zip file containing the full R package
   * @throws IOException
   */
  @GET
  @Path("{id}/download")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  public Response download(@PathParam("id") int id, @QueryParam("packageName") String packageName) throws IOException {
    if (packageName == null) {
        packageName = "estimation" + String.valueOf(id);
    }

    EstimationAnalysisImpl analysis = this.exportAnalysis(id);
    ByteArrayOutputStream baos = new ByteArrayOutputStream();

    service.hydrateAnalysis(analysis, packageName, baos);

    return Response
            .ok(baos)
            .type(MediaType.APPLICATION_OCTET_STREAM)
            .header("Content-Disposition", String.format("attachment; filename=\"estimation_%d.zip\"", id))
            .build();
  }

  /**
   * Generate an estimation design by ID on a specific sourceKey. Please note 
   * this requires configuration of the Arachne Execution Engine.
   * 
   * @summary Generate an estimation on a selected source
   * @param id The id for the estimation study
   * @param sourceKey The CDM source key
   * @return JobExecutionResource The job information
   * @throws IOException
   */
  @POST
  @Path("{id}/generation/{sourceKey}")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public JobExecutionResource runGeneration(@PathParam("id") Integer analysisId,
                                            @PathParam("sourceKey") String sourceKey) throws IOException {

    Estimation analysis = service.getAnalysis(analysisId);
    ExceptionUtils.throwNotFoundExceptionIfNull(analysis, String.format(NO_ESTIMATION_MESSAGE, analysisId));
    EstimationDTO estimationDTO = conversionService.convert(analysis, EstimationDTO.class);
    CheckResult checkResult = runDiagnostics(estimationDTO);
    if (checkResult.hasCriticalErrors()) {
      throw new RuntimeException("Cannot be generated due to critical errors in design. Call 'check' service for further details");
    }
    return service.runGeneration(analysis, sourceKey);
  }

  /**
   * Get a list of generations for the selected estimation design. 
   * 
   * @summary Get generations for an estimation design
   * @param id The id for the estimation design
   * @return List<ExecutionBasedGenerationDTO> The list of generations
   */
  @GET
  @Path("{id}/generation")
  @Produces(MediaType.APPLICATION_JSON)
  public List<ExecutionBasedGenerationDTO> getGenerations(@PathParam("id") Integer analysisId) {

    Map<String, Source> sourcesMap = sourceService.getSourcesMap(SourceMapKey.BY_SOURCE_KEY);
    return sensitiveInfoService.filterSensitiveInfo(converterUtils.convertList(service.getEstimationGenerations(analysisId), ExecutionBasedGenerationDTO.class),
            info -> Collections.singletonMap(Constants.Variables.SOURCE, sourcesMap.get(info.getSourceKey())));
  }

  /**
   * Get an estimation design generation info.
   * 
   * @summary Get estimation design generation info
   * @param generationId The id for the estimation generation
   * @return ExecutionBasedGenerationDTO The generation information
   */
  @GET
  @Path("/generation/{generationId}")
  @Produces(MediaType.APPLICATION_JSON)
  public ExecutionBasedGenerationDTO getGeneration(@PathParam("generationId") Long generationId) {

    EstimationGenerationEntity generationEntity = service.getGeneration(generationId);
    ExceptionUtils.throwNotFoundExceptionIfNull(generationEntity, String.format(NO_GENERATION_MESSAGE, generationId));
    return sensitiveInfoService.filterSensitiveInfo(conversionService.convert(generationEntity, ExecutionBasedGenerationDTO.class),
            Collections.singletonMap(Constants.Variables.SOURCE, generationEntity.getSource()));
  }

  /**
   * Get an estimation design generation result.
   * 
   * @summary Get estimation design generation result
   * @param generationId The id for the estimation generation
   * @return Response Streams a binary ZIP file with the results
   */
  @GET
  @Path("/generation/{generationId}/result")
  @Produces("application/zip")
  public Response downloadResults(@PathParam("generationId") Long generationId) throws IOException {

    File archive = executionService.getExecutionResult(generationId);
    return Response.ok(archive)
            .header("Content-type", "application/zip")
            .header("Content-Disposition", "attachment; filename=\"" + archive.getName() + "\"")
            .build();
  }

    private EstimationDTO reloadAndConvert(Integer id) {
        // Before conversion entity must be refreshed to apply entity graphs
        Estimation estimation = service.getById(id);
        return conversionService.convert(estimation, EstimationDTO.class);
    }

  /**
   * Performs a series of checks of the estimation design to ensure it will
   * properly execute.
   * 
   * @summary Check an estimation design for logic flaws
   * @param estimationDTO The estimation design
   * @return CheckResult The results of performing all checks
   */
    @POST
    @Path("/check")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public CheckResult runDiagnostics(EstimationDTO estimationDTO){

        return new CheckResult(this.checker.check(estimationDTO));
    }
}
