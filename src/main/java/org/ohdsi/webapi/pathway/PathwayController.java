package org.ohdsi.webapi.pathway;

import com.odysseusinc.arachne.commons.utils.ConverterUtils;
import org.ohdsi.webapi.Constants;
import org.ohdsi.webapi.Pagination;
import org.ohdsi.webapi.common.SourceMapKey;
import org.ohdsi.webapi.common.generation.CommonGenerationDTO;
import org.ohdsi.webapi.common.sensitiveinfo.CommonGenerationSensitiveInfoService;
import org.ohdsi.webapi.job.JobExecutionResource;
import org.ohdsi.webapi.pathway.converter.SerializedPathwayAnalysisToPathwayAnalysisConverter;
import org.ohdsi.webapi.pathway.domain.PathwayAnalysisEntity;
import org.ohdsi.webapi.pathway.domain.PathwayAnalysisGenerationEntity;
import org.ohdsi.webapi.pathway.dto.PathwayAnalysisDTO;
import org.ohdsi.webapi.pathway.dto.PathwayAnalysisExportDTO;
import org.ohdsi.webapi.pathway.dto.PathwayCodeDTO;
import org.ohdsi.webapi.pathway.dto.PathwayPopulationEventDTO;
import org.ohdsi.webapi.pathway.dto.PathwayPopulationResultsDTO;
import org.ohdsi.webapi.pathway.dto.TargetCohortPathwaysDTO;
import org.ohdsi.webapi.pathway.dto.internal.PathwayAnalysisResult;
import org.ohdsi.webapi.source.SourceService;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.util.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Path("/pathway-analysis")
@Controller
public class PathwayController {

    private ConversionService conversionService;
    private ConverterUtils converterUtils;
    private PathwayService pathwayService;
    private final SourceService sourceService;
    private final CommonGenerationSensitiveInfoService<CommonGenerationDTO> sensitiveInfoService;

    @Autowired
    public PathwayController(ConversionService conversionService, ConverterUtils converterUtils, PathwayService pathwayService, SourceService sourceService, CommonGenerationSensitiveInfoService sensitiveInfoService) {

        this.conversionService = conversionService;
        this.converterUtils = converterUtils;
        this.pathwayService = pathwayService;
        this.sourceService = sourceService;
        this.sensitiveInfoService = sensitiveInfoService;
    }

    @POST
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public PathwayAnalysisDTO create(final PathwayAnalysisDTO dto) {

        PathwayAnalysisEntity pathwayAnalysis = conversionService.convert(dto, PathwayAnalysisEntity.class);
        PathwayAnalysisEntity saved = pathwayService.create(pathwayAnalysis);
        return reloadAndConvert(saved.getId());
    }

    @POST
    @Path("/{id}")
    public PathwayAnalysisDTO copy(@PathParam("id") final Integer id) {

        PathwayAnalysisDTO dto = get(id);
        dto.setId(null);
        dto.setName(pathwayService.getNameForCopy(dto.getName()));
        return create(dto);
    }

    @POST
    @Path("/import")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public PathwayAnalysisDTO importAnalysis(final PathwayAnalysisExportDTO dto) {

        dto.setName(pathwayService.getNameWithSuffix(dto.getName()));
        PathwayAnalysisEntity pathwayAnalysis = conversionService.convert(dto, PathwayAnalysisEntity.class);
        PathwayAnalysisEntity imported = pathwayService.importAnalysis(pathwayAnalysis);
        return reloadAndConvert(imported.getId());
    }

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Page<PathwayAnalysisDTO> list(@Pagination Pageable pageable) {

        return pathwayService.getPage(pageable).map(pa -> conversionService.convert(pa, PathwayAnalysisDTO.class));
    }

    @GET
    @Path("/{id}/exists")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public int getCountPAWithSameName(@PathParam("id") @DefaultValue("0") final int id, @QueryParam("name") String name) {
        
        return pathwayService.getCountPAWithSameName(id, name);
    }

    @PUT
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public PathwayAnalysisDTO update(@PathParam("id") final Integer id, @RequestBody final PathwayAnalysisDTO dto) {

        PathwayAnalysisEntity pathwayAnalysis = conversionService.convert(dto, PathwayAnalysisEntity.class);
        pathwayAnalysis.setId(id);
        pathwayService.update(pathwayAnalysis);
        return reloadAndConvert(id);
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public PathwayAnalysisDTO get(@PathParam("id") final Integer id) {

        PathwayAnalysisEntity pathwayAnalysis = pathwayService.getById(id);
        ExceptionUtils.throwNotFoundExceptionIfNull(pathwayAnalysis, String.format("There is no pathway analysis with id = %d.", id));
        Map<Integer, Integer> eventCodes = pathwayService.getEventCohortCodes(pathwayAnalysis);

        PathwayAnalysisDTO dto = conversionService.convert(pathwayAnalysis, PathwayAnalysisDTO.class);
        dto.getEventCohorts().forEach(ec -> ec.setCode(eventCodes.get(ec.getId())));

        return dto;
    }

    @GET
    @Path("/{id}/export")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public String export(@PathParam("id") final Integer id) {

        PathwayAnalysisEntity pathwayAnalysis = pathwayService.getById(id);
        return new SerializedPathwayAnalysisToPathwayAnalysisConverter().convertToDatabaseColumn(pathwayAnalysis);
    }

    @GET
    @Path("/{id}/sql/{sourceKey}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Consumes(MediaType.APPLICATION_JSON)
    public String getAnalysisSql(@PathParam("id") final Integer id, @PathParam("sourceKey") final String sourceKey) {

        Source source = sourceService.findBySourceKey(sourceKey);
        return pathwayService.buildAnalysisSql(-1L, pathwayService.getById(id), source.getSourceId());
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public void delete(@PathParam("id") final Integer id) {

        pathwayService.delete(id);
    }

    @POST
    @Path("/{id}/generation/{sourceKey}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public JobExecutionResource generatePathways(
            @PathParam("id") final Integer pathwayAnalysisId,
            @PathParam("sourceKey") final String sourceKey
    ) {

        Source source = sourceService.findBySourceKey(sourceKey);
        return pathwayService.generatePathways(pathwayAnalysisId, source.getSourceId());
    }

    @DELETE
    @Path("/{id}/generation/{sourceKey}")
    public void cancelPathwaysGeneration(
            @PathParam("id") final Integer pathwayAnalysisId,
            @PathParam("sourceKey") final String sourceKey
    ){

        Source source = sourceService.findBySourceKey(sourceKey);
        pathwayService.cancelGeneration(pathwayAnalysisId, source.getSourceId());
    }

    @GET
    @Path("/{id}/generation")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public List<CommonGenerationDTO> getPathwayGenerations(
            @PathParam("id") final Integer pathwayAnalysisId
    ) {

        Map<String, Source> sourcesMap = sourceService.getSourcesMap(SourceMapKey.BY_SOURCE_KEY);
        return sensitiveInfoService.filterSensitiveInfo(converterUtils.convertList(pathwayService.getPathwayGenerations(pathwayAnalysisId), CommonGenerationDTO.class),
                info -> Collections.singletonMap(Constants.Variables.SOURCE, sourcesMap.get(info.getSourceKey())));
    }

    @GET
    @Path("/generation/{generationId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public CommonGenerationDTO getPathwayGenerations(
            @PathParam("generationId") final Long generationId
    ) {

        PathwayAnalysisGenerationEntity generationEntity = pathwayService.getGeneration(generationId);
        return sensitiveInfoService.filterSensitiveInfo(conversionService.convert(generationEntity, CommonGenerationDTO.class),
                Collections.singletonMap(Constants.Variables.SOURCE, generationEntity.getSource()));
    }

    @GET
    @Path("/generation/{generationId}/design")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public String getGenerationDesign(
            @PathParam("generationId") final Long generationId
    ) {

        return pathwayService.findDesignByGenerationId(generationId);

    }

    @GET
    @Path("/generation/{generationId}/result")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public PathwayPopulationResultsDTO getGenerationResults(
            @PathParam("generationId") final Long generationId
    ) {

        PathwayAnalysisResult resultingPathways = pathwayService.getResultingPathways(generationId);

        List<PathwayCodeDTO> eventCodeDtos = resultingPathways.getCodes()
                .stream()
                .map(entry -> {
                    PathwayCodeDTO dto = new PathwayCodeDTO();
                    dto.setCode(entry.getCode());
                    dto.setName(entry.getName());
                    dto.setIsCombo(entry.isCombo());
                    return dto;
                })
                .collect(Collectors.toList());

        List<TargetCohortPathwaysDTO> pathwayDtos = resultingPathways.getCohortPathwaysList()
                .stream()
                .map(cohortResults -> {
                    if (cohortResults.getPathwaysCounts() == null) {
                        return null;
                    }

                    List<PathwayPopulationEventDTO> eventDTOs = cohortResults.getPathwaysCounts()
                            .entrySet()
                            .stream()
                            .map(entry -> new PathwayPopulationEventDTO(entry.getKey(), entry.getValue()))
                            .collect(Collectors.toList());
                    return new TargetCohortPathwaysDTO(cohortResults.getCohortId(), cohortResults.getTargetCohortCount(), cohortResults.getTotalPathwaysCount(), eventDTOs);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return new PathwayPopulationResultsDTO(eventCodeDtos, pathwayDtos);
    }

    private PathwayAnalysisDTO reloadAndConvert(Integer id) {
        // Before conversion entity must be refreshed to apply entity graphs
        PathwayAnalysisEntity analysis = pathwayService.getById(id);
        return conversionService.convert(analysis, PathwayAnalysisDTO.class);
    }
}
