package org.ohdsi.webapi.feanalysis;

import org.ohdsi.analysis.cohortcharacterization.design.FeatureAnalysis;
import org.ohdsi.analysis.cohortcharacterization.design.StandardFeatureAnalysisDomain;
import org.ohdsi.webapi.Pagination;
import org.ohdsi.webapi.common.OptionDTO;
import org.ohdsi.webapi.conceptset.ConceptSetExport;
import org.ohdsi.webapi.feanalysis.domain.*;
import org.ohdsi.webapi.feanalysis.dto.FeAnalysisAggregateDTO;
import org.ohdsi.webapi.feanalysis.dto.FeAnalysisDTO;
import org.ohdsi.webapi.feanalysis.dto.FeAnalysisShortDTO;
import org.ohdsi.webapi.security.authz.AuthorizationService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.ohdsi.webapi.security.authz.access.EntityType;
import org.ohdsi.webapi.security.authz.access.AccessType;
import org.ohdsi.webapi.util.ExceptionUtils;
import org.ohdsi.webapi.util.ExportUtil;
import org.ohdsi.webapi.util.HttpUtils;
import org.ohdsi.webapi.util.NameUtils;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RequestMapping("/feature-analysis")
@RestController
public class FeAnalysisController {

    private FeAnalysisService service;
    private ConversionService conversionService;
    private AuthorizationService authorizationService;

    FeAnalysisController(
            final FeAnalysisService service,
            final ConversionService conversionService,
            AuthorizationService authorizationService) {
        this.service = service;
        this.conversionService = conversionService;
        this.authorizationService = authorizationService;
    }

    /**
     * Get a pagable list of all feature analyses available in WebAPI
     * @summary Feature analyses in WebAPI
     * @param pageable
     * @return
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isAnyPermitted(anyOf('read:feature-analysis','write:feature-analysis'))")
    public Page<FeAnalysisShortDTO> list(@Pagination Pageable pageable) {
        return service.getPage(pageable).map(entity -> {
            FeAnalysisShortDTO dto = convertFeAnaysisToShortDto(entity);
            //TODO: figure out populating permissions on lists
            // AuthorizationService.fillWriteAccess(entity, dto);
            return dto;
        });
    }

    /**
     * Does a feature analysis name already exist?
     * @param id The id for a new feature analysis that does not already exist
     * @param name The desired name for the new feature analysis
     * @return 1 if the name conflicts with an existing feature analysis name and 0 otherwise
     */
    @GetMapping(value = "/{id}/exists", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isOwner(#id, FE_ANALYSIS) or isAnyPermitted(anyOf('read:feature-analysis','write:feature-analysis')) or hasEntityAccess(#id, FE_ANALYSIS, READ)")
    public int getCountFeWithSameName(@PathVariable(value = "id", required = false) final int id, @RequestParam("name") String name) {
        return service.getCountFeWithSameName(id, name);
    }

    /**
     * Feature analysis domains
     * @return Feature analysis domains such as DRUG, DRUG_ERA, MEASUREMENT, etc.
     */
    @GetMapping(value = "/domains", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isAnyPermitted(anyOf('read:feature-analysis','write:feature-analysis'))")
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
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @CacheEvict(cacheNames = org.ohdsi.webapi.security.authz.AuthorizationCacheService.CachingSetup.AUTH_INFO_CACHE, key = "@authorizationService.getAuthenticatedPrincipal().getUserId()")
    @PreAuthorize("isPermitted('create:feature-analysis')")
    public FeAnalysisDTO createAnalysis(@RequestBody final FeAnalysisDTO dto) {
        final FeAnalysisEntity createdEntity = service.createAnalysis(conversionService.convert(dto, FeAnalysisEntity.class));
        return convertFeAnalysisToDto(createdEntity);
    }

    /**
     * Update an existing feature analysis
     * @param feAnalysisId ID of Feature analysis to update
     * @param dto Feature analysis specification
     * @return
     */
    @PutMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isOwner(#feAnalysisId, FE_ANALYSIS) or isPermitted('write:feature-analysis') or hasEntityAccess(#feAnalysisId, FE_ANALYSIS, WRITE)")
    public FeAnalysisDTO updateAnalysis(@PathVariable("id") final Integer feAnalysisId, @RequestBody final FeAnalysisDTO dto) {
        final FeAnalysisEntity updatedEntity = service.updateAnalysis(feAnalysisId, conversionService.convert(dto, FeAnalysisEntity.class));
        return convertFeAnalysisToDto(updatedEntity);
    }

    /**
     * Delete a feature analysis
     * @param feAnalysisId ID of feature analysis to delete
     */
    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isOwner(#feAnalysisId, FE_ANALYSIS) or isPermitted('write:feature-analysis') or hasEntityAccess(#feAnalysisId, FE_ANALYSIS, WRITE)")
    public void deleteAnalysis(@PathVariable("id") final Integer feAnalysisId) {
        final FeAnalysisEntity entity = service.findById(feAnalysisId).orElse(null);
        ExceptionUtils.throwNotFoundExceptionIfNull(entity, String.format("There is no feature analysis with id = %d.", feAnalysisId));
        service.deleteAnalysis(entity);
    }

    /**
     * Get data about a specific feature analysis
     * @param feAnalysisId ID of feature analysis to retrieve
     * @return ID, type, name domain, description, etc of feature analysis
     */
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional
    @PreAuthorize("isOwner(#feAnalysisId, FE_ANALYSIS) or isPermitted('read:feature-analysis') or isPermitted('write:feature-analysis') or hasEntityAccess(#feAnalysisId, FE_ANALYSIS, READ)")
    public FeAnalysisDTO getFeAnalysis(@PathVariable("id") final Integer feAnalysisId) {
        final FeAnalysisEntity feAnalysis = service.findById(feAnalysisId).orElse(null);
        ExceptionUtils.throwNotFoundExceptionIfNull(feAnalysis, String.format("There is no feature analysis with id = %d.", feAnalysisId));
        return convertFeAnalysisToDto(feAnalysis);
    }

    @GetMapping(value = "/{id}/export/conceptset", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @PreAuthorize("isOwner(#feAnalysisId, FE_ANALYSIS) or isPermitted('read:feature-analysis') or isPermitted('write:feature-analysis') or hasEntityAccess(#feAnalysisId, FE_ANALYSIS, READ)")
    public ResponseEntity<org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody> exportConceptSets(@PathVariable("id") final Integer feAnalysisId) {

      final FeAnalysisEntity feAnalysis = service.findById(feAnalysisId).orElse(null);
      ExceptionUtils.throwNotFoundExceptionIfNull(feAnalysis, String.format("There is no feature analysis with id = %d.", feAnalysisId));
      if (feAnalysis instanceof FeAnalysisWithCriteriaEntity) {
        List<ConceptSetExport> exportList = service.exportConceptSets((FeAnalysisWithCriteriaEntity<?>) feAnalysis);

        ByteArrayOutputStream stream = ExportUtil.writeConceptSetExportToCSVAndZip(exportList);
        return HttpUtils.respondBinary(stream, String.format("featureAnalysis_%d_export.zip", feAnalysisId));
      } else {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
      }
    }

    /**
     * Create a copy of a feature analysis
     * @param feAnalysisId ID of feature analysis to copy
     * @return The design specification of the new copy
     */
    @GetMapping(value = "/{id}/copy", produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional
    @CacheEvict(cacheNames = org.ohdsi.webapi.security.authz.AuthorizationCacheService.CachingSetup.AUTH_INFO_CACHE, key = "@authorizationService.getAuthenticatedPrincipal().getUserId()")
    @PreAuthorize("(isOwner(#feAnalysisId, FE_ANALYSIS) or isAnyPermitted(anyOf('read:feature-analysis','write:feature-analysis')) or hasEntityAccess(#feAnalysisId, FE_ANALYSIS, READ)) and isPermitted('create:feature-analysis')")
    public FeAnalysisDTO copy(@PathVariable("id") final Integer feAnalysisId) {
        final FeAnalysisEntity feAnalysis = service.findById(feAnalysisId).orElse(null);
        ExceptionUtils.throwNotFoundExceptionIfNull(feAnalysis, String.format("There is no feature analysis with id = %d.", feAnalysisId));
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
    @GetMapping(value = "/aggregates", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isAnyPermitted(anyOf('read:feature-analysis','write:feature-analysis'))")
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
