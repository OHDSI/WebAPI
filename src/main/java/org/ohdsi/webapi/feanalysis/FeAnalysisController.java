package org.ohdsi.webapi.feanalysis;

import org.ohdsi.analysis.cohortcharacterization.design.FeatureAnalysis;
import org.ohdsi.analysis.cohortcharacterization.design.StandardFeatureAnalysisDomain;
import org.ohdsi.webapi.Pagination;
import org.ohdsi.webapi.common.OptionDTO;
import org.ohdsi.webapi.feanalysis.domain.FeAnalysisEntity;
import org.ohdsi.webapi.feanalysis.dto.FeAnalysisDTO;
import org.ohdsi.webapi.feanalysis.dto.FeAnalysisShortDTO;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;

@Path("/feature-analysis")
@Controller
public class FeAnalysisController {

    private FeAnalysisService service;
    private ConversionService conversionService;

    FeAnalysisController(
            final FeAnalysisService service,
            final ConversionService conversionService) {
        this.service = service;
        this.conversionService = conversionService;
    }

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Page<FeAnalysisShortDTO> list(@Pagination Pageable pageable) {
        return service.getPage(pageable).map(this::convertFeAnaysisToShortDto);
    }

    @GET
    @Path("/{id}/exists")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public int getCountFeWithSameName(@PathParam("id") @DefaultValue("0") final int id, @QueryParam("name") String name) {
        return service.getCountFeWithSameName(id, name);
    }

    @GET
    @Path("/domains")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public List<OptionDTO> listDomains() {

        List<OptionDTO> options = new ArrayList<>();
        for(StandardFeatureAnalysisDomain enumEntry: StandardFeatureAnalysisDomain.values()) {
            options.add(new OptionDTO(enumEntry.name(), enumEntry.getName()));
        }
        return options;
    }

    @POST
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public FeAnalysisDTO createAnalysis(final FeAnalysisDTO dto) {
        final FeAnalysisEntity createdEntity = service.createAnalysis(conversionService.convert(dto, FeAnalysisEntity.class));
        return convertFeAnalysisToDto(createdEntity);
    }

    @PUT
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public FeAnalysisDTO updateAnalysis(@PathParam("id") final Integer feAnalysisId, final FeAnalysisDTO dto) {
        final FeAnalysisEntity updatedEntity = service.updateAnalysis(feAnalysisId, conversionService.convert(dto, FeAnalysisEntity.class));
        return convertFeAnalysisToDto(updatedEntity);
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public void deleteAnalysis(@PathParam("id") final Integer feAnalysisId) {
        final FeAnalysisEntity entity = service.findById(feAnalysisId).orElseThrow(NotFoundException::new);
        service.deleteAnalysis(entity);
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public FeAnalysisDTO getFeAnalysis(@PathParam("id") final Integer feAnalysisId) {
        final FeAnalysisEntity feAnalysis = service.findById(feAnalysisId)
                .orElseThrow(NotFoundException::new);
        return convertFeAnalysisToDto(feAnalysis);
    }

    private FeAnalysisShortDTO convertFeAnaysisToShortDto(final FeatureAnalysis entity) {
        return conversionService.convert(entity, FeAnalysisShortDTO.class);
    }

    private FeAnalysisDTO convertFeAnalysisToDto(final FeatureAnalysis entity) {
        return conversionService.convert(entity, FeAnalysisDTO.class);
    }

}
