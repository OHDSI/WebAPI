package org.ohdsi.webapi.feanalysis;

import com.odysseusinc.arachne.commons.utils.ConverterUtils;
import org.ohdsi.standardized_analysis_api.cohortcharacterization.design.FeatureAnalysis;
import org.ohdsi.webapi.Pagination;
import org.ohdsi.webapi.feanalysis.dto.FeAnalysisDTO;
import org.ohdsi.webapi.feanalysis.dto.FeAnalysisShortDTO;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/feature-analyses")
@Controller
public class FeAnalysisController {

    private FeAnalysisService service;
    private ConversionService conversionService;
    private ConverterUtils converterUtils;

    FeAnalysisController(
            final FeAnalysisService service,
            final ConversionService conversionService,
            final ConverterUtils converterUtils) {
        this.service = service;
        this.conversionService = conversionService;
        this.converterUtils = converterUtils;
    }

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Page<FeAnalysisDTO> list(@Pagination Pageable pageable) { // TODO: FeAnalysisShortDTO
        return service.getPage(pageable).map(this::convertFeAnaysisToShortDto);
    }

    private FeAnalysisDTO convertFeAnaysisToShortDto(final FeatureAnalysis entity) {
        return conversionService.convert(entity, FeAnalysisDTO.class);
    }
}
