package org.ohdsi.webapi.pathway;

import com.odysseusinc.arachne.commons.utils.ConverterUtils;
import org.ohdsi.webapi.Pagination;
import org.ohdsi.webapi.common.generation.CommonGenerationDTO;
import org.ohdsi.webapi.pathway.domain.PathwayAnalysisEntity;
import org.ohdsi.webapi.pathway.dto.PathwayAnalysisExportDTO;
import org.ohdsi.webapi.pathway.dto.PathwayAnalysisDTO;
import org.ohdsi.webapi.pathway.dto.PathwayCodeDTO;
import org.ohdsi.webapi.pathway.dto.PathwayPopulationEventDTO;
import org.ohdsi.webapi.pathway.dto.PathwayPopulationResultsDTO;
import org.ohdsi.webapi.pathway.dto.internal.PathwayAnalysisResult;
import org.ohdsi.webapi.service.SourceService;
import org.ohdsi.webapi.source.Source;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Path("/pathway-analysis")
@Controller
public class PathwayController {

    private ConversionService conversionService;
    private ConverterUtils converterUtils;
    private PathwayService pathwayService;
    private final SourceService sourceService;

    @Autowired
    public PathwayController(ConversionService conversionService, ConverterUtils converterUtils, PathwayService pathwayService, SourceService sourceService) {

        this.conversionService = conversionService;
        this.converterUtils = converterUtils;
        this.pathwayService = pathwayService;
        this.sourceService = sourceService;
    }

    @POST
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public PathwayAnalysisDTO create(final PathwayAnalysisDTO dto) {

        PathwayAnalysisEntity pathwayAnalysis = conversionService.convert(dto, PathwayAnalysisEntity.class);
        PathwayAnalysisEntity saved = pathwayService.create(pathwayAnalysis);
        return conversionService.convert(saved, PathwayAnalysisDTO.class);
    }

    @POST
    @Path("/import")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public PathwayAnalysisDTO importAnalysis(final PathwayAnalysisExportDTO dto) {

        PathwayAnalysisEntity pathwayAnalysis = conversionService.convert(dto, PathwayAnalysisEntity.class);
        PathwayAnalysisEntity imported = pathwayService.importAnalysis(pathwayAnalysis);
        return conversionService.convert(imported, PathwayAnalysisDTO.class);
    }

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Page<PathwayAnalysisDTO> list(@Pagination Pageable pageable) {

        return pathwayService.getPage(pageable).map(pa -> conversionService.convert(pa, PathwayAnalysisDTO.class));
    }

    @PUT
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public PathwayAnalysisDTO update(@PathParam("id") final Integer id, @RequestBody final PathwayAnalysisDTO dto) {

        PathwayAnalysisEntity pathwayAnalysis = conversionService.convert(dto, PathwayAnalysisEntity.class);
        pathwayAnalysis.setId(id);
        PathwayAnalysisEntity saved = pathwayService.update(pathwayAnalysis);
        return conversionService.convert(saved, PathwayAnalysisDTO.class);
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public PathwayAnalysisDTO get(@PathParam("id") final Integer id) {

        PathwayAnalysisEntity pathwayAnalysis = pathwayService.getById(id);
        Map<Integer, Integer> eventCodes = pathwayService.getEventCohortCodes(pathwayAnalysis);

        PathwayAnalysisDTO dto = conversionService.convert(pathwayAnalysis, PathwayAnalysisDTO.class);
        dto.getEventCohorts().forEach(ec -> ec.setCode(eventCodes.get(ec.getPathwayCohortId())));

        return dto;
    }

    @GET
    @Path("/{id}/export")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public PathwayAnalysisExportDTO export(@PathParam("id") final Integer id) {

        PathwayAnalysisEntity pathwayAnalysis = pathwayService.getById(id);
        return conversionService.convert(pathwayAnalysis, PathwayAnalysisExportDTO.class);
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
    public void generatePathways(
            @PathParam("id") final Integer pathwayAnalysisId,
            @PathParam("sourceKey") final String sourceKey
    ) {

        Source source = sourceService.findBySourceKey(sourceKey);
        pathwayService.generatePathways(pathwayAnalysisId, source.getSourceId());
    }

    @GET
    @Path("/{id}/generation")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public List<CommonGenerationDTO> getPathwayGenerations(
            @PathParam("id") final Integer pathwayAnalysisId
    ) {

        return converterUtils.convertList(pathwayService.getPathwayGenerations(pathwayAnalysisId), CommonGenerationDTO.class);
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
                .entrySet()
                .stream()
                .map(entry -> {
                    PathwayCodeDTO dto = new PathwayCodeDTO();
                    dto.setCode(entry.getKey());
                    dto.setName(entry.getValue());
                    return dto;
                })
                .collect(Collectors.toList());

        List<PathwayPopulationEventDTO> pathwayDtos = resultingPathways.getPathwaysCounts().entrySet()
                .stream()
                .map(entry -> {
                    PathwayPopulationEventDTO dto = new PathwayPopulationEventDTO();
                    dto.setPath(entry.getKey());
                    dto.setPersonCount(entry.getValue());
                    return dto;
                })
                .collect(Collectors.toList());

        PathwayPopulationResultsDTO dto = new PathwayPopulationResultsDTO();
        dto.setEventCodes(eventCodeDtos);
        dto.setPathways(pathwayDtos);

        return dto;
    }

    // TODO:
    // endpoint for import
}
