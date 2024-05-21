package org.ohdsi.webapi.feanalysis;

import org.ohdsi.analysis.cohortcharacterization.design.FeatureAnalysis;
import org.ohdsi.analysis.cohortcharacterization.design.StandardFeatureAnalysisDomain;
import org.ohdsi.webapi.Pagination;
import org.ohdsi.webapi.cohortcharacterization.dto.CcShortDTO;
import org.ohdsi.webapi.common.OptionDTO;
import org.ohdsi.webapi.conceptset.ConceptSetExport;
import org.ohdsi.webapi.feanalysis.domain.*;
import org.ohdsi.webapi.feanalysis.dto.FeAnalysisAggregateDTO;
import org.ohdsi.webapi.feanalysis.dto.FeAnalysisDTO;
import org.ohdsi.webapi.feanalysis.dto.FeAnalysisShortDTO;
import org.ohdsi.webapi.security.PermissionService;
import org.ohdsi.webapi.util.ExportUtil;
import org.ohdsi.webapi.util.HttpUtils;
import org.ohdsi.webapi.util.NameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Path("/feature-analysis")
@Controller
public class FeAnalysisController {

    private FeAnalysisService service;
    private ConversionService conversionService;
    private PermissionService permissionService;

    FeAnalysisController(
            final FeAnalysisService service,
            final ConversionService conversionService,
            PermissionService permissionService) {
        this.service = service;
        this.conversionService = conversionService;
        this.permissionService = permissionService;
    }

    /**
     * Get a pagable list of all feature analyses available in WebAPI
     * @summary Feature analyses in WebAPI
     * @param pageable
     * @return
     */
    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Page<FeAnalysisShortDTO> list(@Pagination Pageable pageable) {
        return service.getPage(pageable).map(entity -> {
            FeAnalysisShortDTO dto = convertFeAnaysisToShortDto(entity);
            permissionService.fillWriteAccess(entity, dto);
            return dto;
        });
    }

    /**
     * Does a feature analysis name already exist?
     * @param id The id for a new feature analysis that does not already exist
     * @param name The desired name for the new feature analysis
     * @return 1 if the name conflicts with an existing feature analysis name and 0 otherwise
     */
    @GET
    @Path("/{id}/exists")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public int getCountFeWithSameName(@PathParam("id") @DefaultValue("0") final int id, @QueryParam("name") String name) {
        return service.getCountFeWithSameName(id, name);
    }

    /**
     * Feature analysis domains
     * @return Feature analysis domains such as DRUG, DRUG_ERA, MEASUREMENT, etc.
     */
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

    /**
     * Create a new feature analysis
     * @param dto Feature analysis specification
     * @return
     */
    @POST
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public FeAnalysisDTO createAnalysis(final FeAnalysisDTO dto) {
        final FeAnalysisEntity createdEntity = service.createAnalysis(conversionService.convert(dto, FeAnalysisEntity.class));
        return convertFeAnalysisToDto(createdEntity);
    }

    /**
     * Update an existing feature analysis
     * @param feAnalysisId ID of Feature analysis to update
     * @param dto Feature analysis specification
     * @return
     */
    @PUT
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public FeAnalysisDTO updateAnalysis(@PathParam("id") final Integer feAnalysisId, final FeAnalysisDTO dto) {
        final FeAnalysisEntity updatedEntity = service.updateAnalysis(feAnalysisId, conversionService.convert(dto, FeAnalysisEntity.class));
        return convertFeAnalysisToDto(updatedEntity);
    }

    /**
     * Delete a feature analysis
     * @param feAnalysisId ID of feature analysis to delete
     */
    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public void deleteAnalysis(@PathParam("id") final Integer feAnalysisId) {
        final FeAnalysisEntity entity = service.findById(feAnalysisId).orElseThrow(NotFoundException::new);
        service.deleteAnalysis(entity);
    }

    /**
     * Get data about a specific feature analysis
     * @param feAnalysisId ID of feature analysis to retrieve
     * @return ID, type, name domain, description, etc of feature analysis
     */
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public FeAnalysisDTO getFeAnalysis(@PathParam("id") final Integer feAnalysisId) {
        final FeAnalysisEntity feAnalysis = service.findById(feAnalysisId)
                .orElseThrow(NotFoundException::new);
        return convertFeAnalysisToDto(feAnalysis);
    }

    @GET
    @Path("/{id}/export/conceptset")
    public Response exportConceptSets(@PathParam("id") final Integer feAnalysisId) {

      final FeAnalysisEntity feAnalysis = service.findById(feAnalysisId).orElseThrow(NotFoundException::new);
      if (feAnalysis instanceof FeAnalysisWithCriteriaEntity) {
        List<ConceptSetExport> exportList = service.exportConceptSets((FeAnalysisWithCriteriaEntity<?>) feAnalysis);

        ByteArrayOutputStream stream = ExportUtil.writeConceptSetExportToCSVAndZip(exportList);
        return HttpUtils.respondBinary(stream, String.format("featureAnalysis_%d_export.zip", feAnalysisId));
      } else {
        throw new BadRequestException();
      }
    }

    /**
     * Create a copy of a feature analysis
     * @param feAnalysisId ID of feature analysis to copy
     * @return The design specification of the new copy
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/copy")
    @Transactional
    public FeAnalysisDTO copy(@PathParam("id") final Integer feAnalysisId) {
        final FeAnalysisEntity feAnalysis = service.findById(feAnalysisId)
                .orElseThrow(NotFoundException::new);
        final FeAnalysisEntity feAnalysisForCopy = getNewEntityForCopy(feAnalysis);

        FeAnalysisEntity saved;
        switch (feAnalysis.getType()) {
            case CRITERIA_SET:
                saved = service.createCriteriaAnalysis((FeAnalysisWithCriteriaEntity) feAnalysisForCopy);
                break;
            case CUSTOM_FE:
                saved = service.createAnalysis(feAnalysisForCopy);
                break;
            default:
                throw new IllegalArgumentException("Analysis with type: " + feAnalysis.getType() + " cannot be copied");
        }

        return convertFeAnalysisToDto(saved);
    }

    private FeAnalysisEntity getNewEntityForCopy(FeAnalysisEntity entity) {
        FeAnalysisEntity entityForCopy;
        switch (entity.getType()) {
            case CRITERIA_SET:
                switch (entity.getStatType()) {
                    case PREVALENCE:
                        entityForCopy = new FeAnalysisWithPrevalenceCriteriaEntity((FeAnalysisWithCriteriaEntity) entity);
                        break;
                    case DISTRIBUTION:
                        entityForCopy = new FeAnalysisWithDistributionCriteriaEntity((FeAnalysisWithCriteriaEntity) entity);
                        break;
                    default:
                        throw new IllegalArgumentException();
                }

                // deep copy of criteria list...
                final List<FeAnalysisCriteriaEntity> criteriaList = new ArrayList<>();
                ((FeAnalysisWithCriteriaEntity) entity).getDesign().forEach(c -> {
                    final FeAnalysisCriteriaEntity criteria = createCriteriaEntity((FeAnalysisCriteriaEntity) c);
                    criteria.setName(((FeAnalysisCriteriaEntity) c).getName());
                    criteria.setExpressionString(((FeAnalysisCriteriaEntity) c).getExpressionString());
                    criteria.setAggregate(((FeAnalysisCriteriaEntity) c).getAggregate());
                    criteriaList.add(criteria);
                });
                entityForCopy.setDesign(criteriaList);

                // ...and concept sets
                final FeAnalysisConcepsetEntity concepsetEntity = new FeAnalysisConcepsetEntity();
                concepsetEntity.setRawExpression(((FeAnalysisWithCriteriaEntity) entity).getConceptSetEntity().getRawExpression());
                ((FeAnalysisWithCriteriaEntity) entityForCopy).setConceptSetEntity(concepsetEntity);
                break;
            case CUSTOM_FE:
                entityForCopy = new FeAnalysisWithStringEntity((FeAnalysisWithStringEntity) entity);
                break;
            default:
                throw new IllegalArgumentException("Analysis with type: " + entity.getType() + " cannot be copied");
        }
        entityForCopy.setId(null);
        entityForCopy.setName(
                NameUtils.getNameForCopy(entityForCopy.getName(), this::getNamesLike, service.findByName(entityForCopy.getName())));
        entityForCopy.setCreatedBy(null);
        entityForCopy.setCreatedDate(null);
        entityForCopy.setModifiedBy(null);
        entityForCopy.setModifiedDate(null);
        return entityForCopy;
    }

    /**
     * Get aggregation functions used in feature analyses
     * @return
     */
    @GET
    @Path("/aggregates")
    @Produces(MediaType.APPLICATION_JSON)
    public List<FeAnalysisAggregateDTO> listAggregates() {
        List<FeAnalysisAggregateDTO> result = service.findAggregates().stream()
                .map(this::convertFeAnalysisAggregateToDto)
                .collect(Collectors.toList());
        return result;
    }

    private FeAnalysisShortDTO convertFeAnaysisToShortDto(final FeatureAnalysis entity) {
        return conversionService.convert(entity, FeAnalysisShortDTO.class);
    }

    private FeAnalysisDTO convertFeAnalysisToDto(final FeatureAnalysis entity) {
        return conversionService.convert(entity, FeAnalysisDTO.class);
    }

    private List<String> getNamesLike(String copyName) {
        return service.getNamesLike(copyName);
    }

    private FeAnalysisAggregateDTO convertFeAnalysisAggregateToDto(final FeAnalysisAggregateEntity entity) {
        return conversionService.convert(entity, FeAnalysisAggregateDTO.class);
    }

    private FeAnalysisCriteriaEntity createCriteriaEntity(FeAnalysisCriteriaEntity basis) {
        if (basis instanceof FeAnalysisWindowedCriteriaEntity) {
            return new FeAnalysisWindowedCriteriaEntity();
        } else if (basis instanceof FeAnalysisDemographicCriteriaEntity) {
            return new FeAnalysisDemographicCriteriaEntity();
        } else {
            return new FeAnalysisCriteriaGroupEntity();
        }
    }
}
