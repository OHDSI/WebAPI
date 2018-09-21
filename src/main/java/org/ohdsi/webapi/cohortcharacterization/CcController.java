package org.ohdsi.webapi.cohortcharacterization;

import com.odysseusinc.arachne.commons.utils.ConverterUtils;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.ohdsi.webapi.Pagination;
import org.ohdsi.webapi.cohortcharacterization.domain.CohortCharacterizationEntity;
import org.ohdsi.webapi.cohortcharacterization.dto.*;
import org.ohdsi.webapi.job.JobExecutionResource;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

@Path("/cohort-characterization")
@Controller
@Transactional
public class CcController {
    
    private CcService service;
    private ConversionService conversionService;
    private ConverterUtils converterUtils;
    
    CcController(
            final CcService service,
            final ConversionService conversionService, 
            final ConverterUtils converterUtils) {
        this.service = service;
        this.conversionService = conversionService;
        this.converterUtils = converterUtils;
    }

    @POST
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public CohortCharacterizationDTO create(final CohortCharacterizationDTO dto) {
        final CohortCharacterizationEntity createdEntity = service.createCc(conversionService.convert(dto, CohortCharacterizationEntity.class));
        return conversionService.convert(createdEntity, CohortCharacterizationDTO.class);
    }

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Page<CcShortDTO> list(@Pagination Pageable pageable) {
        return service.getPageWithLinkedEntities(pageable).map(this::convertCcToShortDto);
    }

    @GET
    @Path("/design")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Page<CohortCharacterizationDTO> listDesign(@Pagination Pageable pageable) {
        return service.getPageWithLinkedEntities(pageable).map(this::convertCcToDto);
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public CcShortDTO get(@PathParam("id") final Long id) {
        return convertCcToShortDto(service.findById(id));
    }

    @GET
    @Path("/{id}/design")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public CohortCharacterizationDTO getDesign(@PathParam("id") final Long id) {
        return convertCcToDto(service.findByIdWithLinkedEntities(id));
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public void deleteCc(@PathParam("id") final Long id) {
        service.deleteCc(id);
    }
    
    private CohortCharacterizationDTO convertCcToDto(final CohortCharacterizationEntity entity) {
        return conversionService.convert(entity, CohortCharacterizationDTO.class);
    }

    private CcShortDTO convertCcToShortDto(final CohortCharacterizationEntity entity) {
        return conversionService.convert(entity, CcShortDTO.class);
    }
    
    @PUT
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public CohortCharacterizationDTO update(@PathParam("id") final Long id, final CohortCharacterizationDTO dto) {
        final CohortCharacterizationEntity entity = conversionService.convert(dto, CohortCharacterizationEntity.class);
        entity.setId(id);
        final CohortCharacterizationEntity updatedEntity = service.updateCc(entity);
        return conversionService.convert(updatedEntity, CohortCharacterizationDTO.class);
    }

    @POST
    @Path("/import")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public CohortCharacterizationDTO doImport(final CcExportDTO dto) {
        final CohortCharacterizationEntity entity = conversionService.convert(dto, CohortCharacterizationEntity.class);
        return conversionService.convert(service.importCc(entity), CohortCharacterizationDTO.class);
    }

    @GET
    @Path("/{id}/export")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public String export(@PathParam("id") final Long id) {
        return service.serializeCc(id);
    }
    
    @POST
    @Path("/{id}/generation/{sourceKey}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public JobExecutionResource generate(@PathParam("id") final Long id, @PathParam("sourceKey") final String sourceKey) {
        return service.generateCc(id, sourceKey);
    }
    
    @GET
    @Path("/{id}/generation")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public List<CcGenerationDTO> getGenerationList(@PathParam("id") final Long id) {
        return converterUtils.convertList(service.findGenerationsByCcId(id), CcGenerationDTO.class);
    }

    @GET
    @Path("/generation/{generationId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public CcGenerationDTO getGeneration(@PathParam("generationId") final Long generationId) {
        return conversionService.convert(service.findGenerationById(generationId), CcGenerationDTO.class);
    }

    @DELETE
    @Path("/generation/{generationId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public void deleteGeneration(@PathParam("generationId") final Long generationId) {
        service.deleteCcGeneration(generationId);
    }

    @GET
    @Path("/generation/{generationId}/design")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public CcExportDTO getGenerationDesign(
            @PathParam("generationId") final Long generationId) {
        return conversionService.convert(service.findDesignByGenerationId(generationId), CcExportDTO.class);
    }

    @GET
    @Path("/generation/{generationId}/result")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public List<CcResult> getGenerationsResults(
            @PathParam("generationId") final Long generationId) {
        return converterUtils.convertList(service.findResults(generationId), CcResult.class);
    }
}
