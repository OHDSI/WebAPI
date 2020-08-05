package org.ohdsi.webapi.prediction;

import com.odysseusinc.arachne.commons.utils.ConverterUtils;
import org.ohdsi.analysis.Utils;
import org.ohdsi.webapi.Constants;
import org.ohdsi.webapi.check.CheckResult;
import org.ohdsi.webapi.check.checker.prediction.PredictionChecker;
import org.ohdsi.webapi.common.SourceMapKey;
import org.ohdsi.webapi.common.analyses.CommonAnalysisDTO;
import org.ohdsi.webapi.common.generation.ExecutionBasedGenerationDTO;
import org.ohdsi.webapi.common.sensitiveinfo.CommonGenerationSensitiveInfoService;
import org.ohdsi.webapi.executionengine.service.ScriptExecutionService;
import org.ohdsi.webapi.job.JobExecutionResource;
import org.ohdsi.webapi.prediction.domain.PredictionGenerationEntity;
import org.ohdsi.webapi.prediction.dto.PredictionAnalysisDTO;
import org.ohdsi.webapi.prediction.specification.PatientLevelPredictionAnalysisImpl;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceService;
import org.ohdsi.webapi.util.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.stereotype.Controller;

import javax.ws.rs.*;
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

@Controller
@Path("/prediction/")
public class PredictionController {

  private static final Logger LOGGER = LoggerFactory.getLogger(PredictionController.class);

  private static final String NO_PREDICTION_ANALYSIS_MESSAGE = "There is no prediction analysis with id = %d.";
  private static final String NO_GENERATION_MESSAGE = "There is no generation with id = %d";

  private final PredictionService service;

  private final GenericConversionService conversionService;

  private final ConverterUtils converterUtils;

  private final CommonGenerationSensitiveInfoService<ExecutionBasedGenerationDTO> sensitiveInfoService;

  private final SourceService sourceService;

  private final ScriptExecutionService executionService;
  private final PredictionChecker checker;

  @Autowired
  public PredictionController(PredictionService service,
                              GenericConversionService conversionService,
                              ConverterUtils converterUtils,
                              CommonGenerationSensitiveInfoService sensitiveInfoService,
                              SourceService sourceService,
                              ScriptExecutionService executionService, PredictionChecker checker) {
    this.service = service;
    this.conversionService = conversionService;
    this.converterUtils = converterUtils;
    this.sensitiveInfoService = sensitiveInfoService;
    this.sourceService = sourceService;
    this.executionService = executionService;
    this.checker = checker;
  }

  @GET
  @Path("/")
  @Produces(MediaType.APPLICATION_JSON)
  public List<CommonAnalysisDTO> getAnalysisList() {

    return StreamSupport
            .stream(service.getAnalysisList().spliterator(), false)
            .map(analysis -> conversionService.convert(analysis, CommonAnalysisDTO.class))
            .collect(Collectors.toList());
  }

  @GET
  @Path("/{id}/exists")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public int getCountPredictionWithSameName(@PathParam("id") @DefaultValue("0") final int id, @QueryParam("name") String name) {
    return service.getCountPredictionWithSameName(id, name);
  }

  @DELETE
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/{id}")
  public void delete(@PathParam("id") int id) {
    service.delete(id);
  }

  @POST
  @Path("/")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public PredictionAnalysisDTO createAnalysis(PredictionAnalysis pred) {
    PredictionAnalysis analysis = service.createAnalysis(pred);
    return reloadAndConvert(analysis.getId());
  }

  @PUT
  @Path("{id}")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public PredictionAnalysisDTO updateAnalysis(@PathParam("id") int id, PredictionAnalysis pred) {
    service.updateAnalysis(id, pred);
    return reloadAndConvert(id);
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/{id}/copy")
  public PredictionAnalysisDTO copy(@PathParam("id") int id) {
    PredictionAnalysis analysis = service.copy(id);
    return reloadAndConvert(analysis.getId());
  }

  @GET
  @Path("{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public PredictionAnalysisDTO getAnalysis(@PathParam("id") int id) {

    PredictionAnalysis analysis = service.getAnalysis(id);
    ExceptionUtils.throwNotFoundExceptionIfNull(analysis, String.format(NO_PREDICTION_ANALYSIS_MESSAGE, id));
    return conversionService.convert(analysis, PredictionAnalysisDTO.class);
  }

  @GET
  @Path("{id}/export")
  @Produces(MediaType.APPLICATION_JSON)
  public PatientLevelPredictionAnalysisImpl exportAnalysis(@PathParam("id") int id) {

    return service.exportAnalysis(id);
  }
  
  @POST
  @Path("import")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public PredictionAnalysisDTO importAnalysis(PatientLevelPredictionAnalysisImpl analysis) throws Exception {

    if (Objects.isNull(analysis)) {
      LOGGER.error("Failed to import Prediction, empty or not valid source JSON");
      throw new InternalServerErrorException();
    }
    PredictionAnalysis importedAnalysis = service.importAnalysis(analysis);
    return reloadAndConvert(importedAnalysis.getId());
  }  

  @GET
  @Path("{id}/download")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  public Response downloadPackage(@PathParam("id") int id, @QueryParam("packageName") String packageName) throws IOException {
    if (packageName == null) {
        packageName = "prediction" + String.valueOf(id);
    }
    if (!Utils.isAlphaNumeric(packageName)) {
        throw new IllegalArgumentException("The package name must be alphanumeric only.");
    }      

    PatientLevelPredictionAnalysisImpl plpa = service.exportAnalysis(id);
    ByteArrayOutputStream baos = new ByteArrayOutputStream();

    service.hydrateAnalysis(plpa, packageName, baos);

    Response response = Response
            .ok(baos)
            .type(MediaType.APPLICATION_OCTET_STREAM)
            .header("Content-Disposition", String.format("attachment; filename=\"prediction_study_%d_export.zip\"", id))
            .build();

    return response;
  }

  @POST
  @Path("{id}/generation/{sourceKey}")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public JobExecutionResource runGeneration(@PathParam("id") Integer predictionAnalysisId,
                                            @PathParam("sourceKey") String sourceKey) throws IOException {

    PredictionAnalysis predictionAnalysis = service.getAnalysis(predictionAnalysisId);
    ExceptionUtils.throwNotFoundExceptionIfNull(predictionAnalysis, String.format(NO_PREDICTION_ANALYSIS_MESSAGE, predictionAnalysisId));
    PredictionAnalysisDTO predictionAnalysisDTO = conversionService.convert(predictionAnalysis, PredictionAnalysisDTO.class);
    CheckResult checkResult = runDiagnostics(predictionAnalysisDTO);
    if (checkResult.hasCriticalErrors()) {
      throw new RuntimeException("Cannot be generated due to critical errors in design. Call 'check' service for further details");
    }
    return service.runGeneration(predictionAnalysis, sourceKey);
  }

  @GET
  @Path("{id}/generation")
  @Produces(MediaType.APPLICATION_JSON)
  public List<ExecutionBasedGenerationDTO> getGenerations(@PathParam("id") Integer predictionAnalysisId) {

    Map<String, Source> sourcesMap = sourceService.getSourcesMap(SourceMapKey.BY_SOURCE_KEY);
    List<PredictionGenerationEntity> predictionGenerations = service.getPredictionGenerations(predictionAnalysisId);
    List<ExecutionBasedGenerationDTO> dtos = converterUtils.convertList(predictionGenerations, ExecutionBasedGenerationDTO.class);
    return sensitiveInfoService.filterSensitiveInfo(dtos, info -> Collections.singletonMap(Constants.Variables.SOURCE, sourcesMap.get(info.getSourceKey())));
  }

  @GET
  @Path("/generation/{generationId}")
  @Produces(MediaType.APPLICATION_JSON)
  public ExecutionBasedGenerationDTO getGeneration(@PathParam("generationId") Long generationId) {

    PredictionGenerationEntity generationEntity = service.getGeneration(generationId);
    ExceptionUtils.throwNotFoundExceptionIfNull(generationEntity, String.format(NO_GENERATION_MESSAGE, generationId));
    return sensitiveInfoService.filterSensitiveInfo(conversionService.convert(generationEntity, ExecutionBasedGenerationDTO.class),
            Collections.singletonMap(Constants.Variables.SOURCE, generationEntity.getSource()));
  }

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

    private PredictionAnalysisDTO reloadAndConvert(Integer id) {
        // Before conversion entity must be refreshed to apply entity graphs
        PredictionAnalysis analysis = service.getById(id);
        return conversionService.convert(analysis, PredictionAnalysisDTO.class);
    }

    @POST
    @Path("/check")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public CheckResult runDiagnostics(PredictionAnalysisDTO predictionAnalysisDTO){

        return new CheckResult(checker.check(predictionAnalysisDTO));
    }
}
