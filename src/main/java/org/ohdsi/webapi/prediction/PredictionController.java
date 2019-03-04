package org.ohdsi.webapi.prediction;

import com.odysseusinc.arachne.commons.utils.ConverterUtils;
import org.ohdsi.webapi.Constants;
import org.ohdsi.webapi.common.SourceMapKey;
import org.ohdsi.webapi.common.generation.ExecutionBasedGenerationDTO;
import org.ohdsi.webapi.common.sensitiveinfo.CommonGenerationSensitiveInfoService;
import org.ohdsi.webapi.executionengine.service.ScriptExecutionService;
import org.ohdsi.webapi.prediction.domain.PredictionGenerationEntity;
import org.ohdsi.webapi.prediction.dto.PredictionAnalysisDTO;
import org.ohdsi.webapi.prediction.specification.PatientLevelPredictionAnalysisImpl;
import org.ohdsi.webapi.service.SourceService;
import org.ohdsi.webapi.source.SourceInfo;
import org.ohdsi.webapi.util.ExceptionUtils;
import org.ohdsi.webapi.util.UserUtils;
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
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.ohdsi.analysis.Utils;

@Controller
@Path("/prediction/")
public class PredictionController {

  private static final String NO_PREDICTION_ANALYSIS_MESSAGE = "There is no prediction analysis with id = %d.";
  private static final String NO_GENERATION_MESSAGE = "There is no generation with id = %d";

  private final PredictionService service;

  private final GenericConversionService conversionService;

  private final ConverterUtils converterUtils;

  private final CommonGenerationSensitiveInfoService<ExecutionBasedGenerationDTO> sensitiveInfoService;

  private final SourceService sourceService;

  private final ScriptExecutionService executionService;

  @Autowired
  public PredictionController(PredictionService service,
                              GenericConversionService conversionService,
                              ConverterUtils converterUtils,
                              CommonGenerationSensitiveInfoService sensitiveInfoService,
                              SourceService sourceService,
                              ScriptExecutionService executionService) {
    this.service = service;
    this.conversionService = conversionService;
    this.converterUtils = converterUtils;
    this.sensitiveInfoService = sensitiveInfoService;
    this.sourceService = sourceService;
    this.executionService = executionService;
  }

  @GET
  @Path("/")
  @Produces(MediaType.APPLICATION_JSON)
  public List<PredictionListItem> getAnalysisList() {

    return StreamSupport
            .stream(service.getAnalysisList().spliterator(), false)
            .map(pred -> {
              PredictionListItem item = new PredictionListItem();
              item.analysisId = pred.getId();
              item.name = pred.getName();
              item.description = pred.getDescription();
              item.createdBy = UserUtils.nullSafeLogin(pred.getCreatedBy());
              item.createdDate = pred.getCreatedDate();
              item.modifiedBy = UserUtils.nullSafeLogin(pred.getModifiedBy());
              item.modifiedDate = pred.getModifiedDate();
              return item;
            }).collect(Collectors.toList());
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

    return conversionService.convert(service.createAnalysis(pred), PredictionAnalysisDTO.class);
  }

  @PUT
  @Path("{id}")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public PredictionAnalysisDTO updateAnalysis(@PathParam("id") int id, PredictionAnalysis pred) {

    return conversionService.convert(service.updateAnalysis(id, pred), PredictionAnalysisDTO.class);
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/{id}/copy")
  public PredictionAnalysisDTO copy(@PathParam("id") int id) {

    return conversionService.convert(service.copy(id), PredictionAnalysisDTO.class);
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
    PredictionAnalysis importedAnalysis = service.importAnalysis(analysis);
    return conversionService.convert(importedAnalysis, PredictionAnalysisDTO.class);
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
  public void runGeneration(@PathParam("id") Integer predictionAnalysisId,
                     @PathParam("sourceKey") String sourceKey) throws IOException {

    PredictionAnalysis predictionAnalysis = service.getAnalysis(predictionAnalysisId);
    ExceptionUtils.throwNotFoundExceptionIfNull(predictionAnalysis, String.format(NO_PREDICTION_ANALYSIS_MESSAGE, predictionAnalysisId));
    service.runGeneration(predictionAnalysis, sourceKey);
  }

  @GET
  @Path("{id}/generation")
  @Produces(MediaType.APPLICATION_JSON)
  public List<ExecutionBasedGenerationDTO> getGenerations(@PathParam("id") Integer predictionAnalysisId) {

    Map<String, SourceInfo> sourcesMap = sourceService.getSourcesMap(SourceMapKey.BY_SOURCE_KEY);
    return sensitiveInfoService.filterSensitiveInfo(converterUtils.convertList(service.getPredictionGenerations(predictionAnalysisId), ExecutionBasedGenerationDTO.class),
            info -> Collections.singletonMap(Constants.Variables.SOURCE, sourcesMap.get(info.getSourceKey())));
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

}
