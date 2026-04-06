package org.ohdsi.webapi.cohortcharacterization;

import org.ohdsi.webapi.arachne.commons.utils.CommonFilenameUtils;
import org.ohdsi.webapi.arachne.commons.utils.ConverterUtils;
import com.opencsv.CSVWriter;
import org.ohdsi.analysis.cohortcharacterization.design.StandardFeatureAnalysisType;
import org.ohdsi.featureExtraction.FeatureExtraction;
import org.ohdsi.webapi.Constants;
import org.ohdsi.webapi.Pagination;
import org.ohdsi.webapi.check.CheckResult;
import org.ohdsi.webapi.check.checker.characterization.CharacterizationChecker;
import org.ohdsi.webapi.cohortcharacterization.domain.CcGenerationEntity;
import org.ohdsi.webapi.cohortcharacterization.domain.CohortCharacterizationEntity;
import org.ohdsi.webapi.cohortcharacterization.dto.CcExportDTO;
import org.ohdsi.webapi.cohortcharacterization.dto.CcPrevalenceStat;
import org.ohdsi.webapi.cohortcharacterization.dto.CcResult;
import org.ohdsi.webapi.cohortcharacterization.dto.CcShortDTO;
import org.ohdsi.webapi.cohortcharacterization.dto.CcTemporalResult;
import org.ohdsi.webapi.cohortcharacterization.dto.CcVersionFullDTO;
import org.ohdsi.webapi.cohortcharacterization.dto.CohortCharacterizationDTO;
import org.ohdsi.webapi.cohortcharacterization.dto.ExportExecutionResultRequest;
import org.ohdsi.webapi.cohortcharacterization.dto.GenerationResults;
import org.ohdsi.webapi.cohortcharacterization.report.Report;
import org.ohdsi.webapi.common.SourceMapKey;
import org.ohdsi.webapi.common.generation.CommonGenerationDTO;
import org.ohdsi.webapi.common.sensitiveinfo.CommonGenerationSensitiveInfoService;
import org.ohdsi.webapi.conceptset.ConceptSetExport;
import org.ohdsi.webapi.feanalysis.FeAnalysisService;
import org.ohdsi.webapi.feanalysis.domain.FeAnalysisEntity;
import org.ohdsi.webapi.feanalysis.domain.FeAnalysisWithStringEntity;
import org.ohdsi.webapi.job.JobExecutionResource;
import org.ohdsi.webapi.security.authz.AuthorizationService;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceService;
import org.ohdsi.webapi.tag.dto.TagNameListRequestDTO;
import org.ohdsi.webapi.util.ExceptionUtils;
import org.ohdsi.webapi.util.ExportUtil;
import org.ohdsi.webapi.util.HttpUtils;
import org.ohdsi.webapi.versioning.dto.VersionDTO;
import org.ohdsi.webapi.versioning.dto.VersionUpdateDTO;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.springframework.security.access.prepost.PreAuthorize;
import org.ohdsi.webapi.security.authz.access.AccessType;
import org.ohdsi.webapi.security.authz.access.EntityType;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RestController
@RequestMapping("/cohort-characterization")
@Transactional
public class CcController {

    private CcService service;
    private FeAnalysisService feAnalysisService;
    private ConversionService conversionService;
    private ConverterUtils converterUtils;
    private final CommonGenerationSensitiveInfoService<CommonGenerationDTO> sensitiveInfoService;
    private final SourceService sourceService;
    private CharacterizationChecker checker;
    private AuthorizationService authorizationService;

    public CcController(
            final CcService service,
            final FeAnalysisService feAnalysisService,
            final ConversionService conversionService,
            final ConverterUtils converterUtils,
            CommonGenerationSensitiveInfoService sensitiveInfoService,
            SourceService sourceService, CharacterizationChecker checker,
            AuthorizationService authorizationService) {
        this.service = service;
        this.feAnalysisService = feAnalysisService;
        this.conversionService = conversionService;
        this.converterUtils = converterUtils;
        this.sensitiveInfoService = sensitiveInfoService;
        this.sourceService = sourceService;
        this.checker = checker;
        this.authorizationService = authorizationService;
        FeatureExtraction.init(null);
    }

    /**
     * Create a new cohort characterization
     *
     * @param dto A cohort characterization JSON definition (name, cohorts, featureAnalyses, etc.)
     * @return The cohort characterization definition passed in as input
     * with additional fields (createdDate, hasWriteAccess, tags, id, hashcode).
     */
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Transactional
    @PreAuthorize("isPermitted('create:cohort-characterization')")
    public CohortCharacterizationDTO create(@RequestBody final CohortCharacterizationDTO dto) {
        final CohortCharacterizationEntity createdEntity = service.createCc(conversionService.convert(dto, CohortCharacterizationEntity.class));
        return conversionService.convert(createdEntity, CohortCharacterizationDTO.class);
    }

    /**
     * Create a copy of an existing cohort characterization
     *
     * @param id An existing cohort characterization id
     * @return The cohort characterization definition of the newly created copy
     */
    @PostMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("(isOwner(#id, COHORT_CHARACTERIZATION) or isPermitted(anyOf('read:cohort-characterization','write:cohort-characterization') or hasEntityAccess(#id, COHORT_CHARACTERIZATION, READ)) and isPermitted('create:cohort-characterization')")
    public CohortCharacterizationDTO copy(@PathVariable("id") final Long id) {
        CohortCharacterizationDTO dto = getDesign(id);
        dto.setName(service.getNameForCopy(dto.getName()));
        dto.setId(null);
        dto.setTags(null);
        dto.getStratas().forEach(s -> s.setId(null));
        dto.getParameters().forEach(p -> p.setId(null));
        return create(dto);
    }

    /**
     * Get information about the cohort characterization analyses in WebAPI
     *
     * @return A json object with information about the characterization analyses in WebAPI.
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Page<CcShortDTO> list(@Pagination Pageable pageable) {
            return service.getPage(pageable);
    }

    /**
     * Get the design specification for every cohort-characterization analysis in WebAPI.
     *
     * @return A json object with all characterization design specifications.
     */
    @GetMapping(value = "/design", produces = MediaType.APPLICATION_JSON_VALUE)
    public Page<CohortCharacterizationDTO> listDesign(@Pagination Pageable pageable) {
        return service.getPageWithLinkedEntities(pageable).map(entity -> {
          CohortCharacterizationDTO dto = convertCcToDto(entity);
          //authorizationService.fillWriteAccess(entity, dto);
          //authorizationService.fillReadAccess(entity, dto);
          return dto;
      });
    }

    /**
     * Get metadata about a cohort characterization.
     *
     * @param id The id for an existing cohort characterization
     * @return name, createdDate, tags, etc for a single cohort characterization.
     */
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isOwner(#id, COHORT_CHARACTERIZATION) or isPermitted('read:cohort-characterization','write:cohort-characterization') or hasEntityAccess(#id, COHORT_CHARACTERIZATION, READ)")
    public CcShortDTO get(@PathVariable("id") final Long id) {
        return convertCcToShortDto(service.findById(id));
    }

    /**
     * Get the complete design specification for a single cohort characterization.
     *
     * @param id The id for an existing cohort characterization
     * @return JSON containing the cohort characterization specification
     */
    @GetMapping(value = "/{id}/design", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isOwner(#id, COHORT_CHARACTERIZATION) or isPermitted('read:cohort-characterization','write:cohort-characterization') or hasEntityAccess(#id, COHORT_CHARACTERIZATION, READ)")
    public CohortCharacterizationDTO getDesign(@PathVariable("id") final Long id) {
        CohortCharacterizationEntity cc = service.findByIdWithLinkedEntities(id);
        ExceptionUtils.throwNotFoundExceptionIfNull(cc, String.format("There is no cohort characterization with id = %d.", id));
        return convertCcToDto(cc);
    }

    /**
     * Check if a cohort characterization with the same name exists
     *
     * <p>This endpoint is used to check that a desired name for a characterization does not already exist in WebAPI</p>
     *
     * @param id The id for a new characterization that does not currently exist in WebAPI
     * @param name The desired name for the new cohort characterization
     * @return The number of existing characterizations with the same name that was passed as a query parameter
     */
    @GetMapping(value = "/{id}/exists", produces = MediaType.APPLICATION_JSON_VALUE)
    public int getCountCcWithSameName(@PathVariable(value = "id", required = false) final long id, @RequestParam("name") String name) {
        return service.getCountCcWithSameName(id, name);
    }

    /**
     * Remove a characterization from WebAPI
     *
     * @param id The id for a characterization that currently exists in WebAPI
     */
    @DeleteMapping(value = "/{id}")
    @PreAuthorize("isOwner(#id, COHORT_CHARACTERIZATION) or isPermitted('write:cohort-characterization') or hasEntityAccess(#id, COHORT_CHARACTERIZATION, WRITE)")
    public void deleteCc(@PathVariable("id") final Long id) {
        service.deleteCc(id);
    }
    
    private CohortCharacterizationDTO convertCcToDto(final CohortCharacterizationEntity entity) {
        return conversionService.convert(entity, CohortCharacterizationDTO.class);
    }

    private CcShortDTO convertCcToShortDto(final CohortCharacterizationEntity entity) {
        return conversionService.convert(entity, CcShortDTO.class);
    }

    @PutMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isOwner(#id, COHORT_CHARACTERIZATION) or isPermitted('write:cohort-characterization') or hasEntityAccess(#id, COHORT_CHARACTERIZATION, WRITE)")
    public CohortCharacterizationDTO update(@PathVariable("id") final Long id, @RequestBody final CohortCharacterizationDTO dto) {
        service.saveVersion(dto.getId());
        final CohortCharacterizationEntity entity = conversionService.convert(dto, CohortCharacterizationEntity.class);
        entity.setId(id);
        final CohortCharacterizationEntity updatedEntity = service.updateCc(entity);
        return convertCcToDto(updatedEntity);
    }

    /**
     * Add a new cohort characterization analysis to WebAPI
     *
     * @chrisknoll this endpoint did not work when I tried it.
     *
     * @param dto A cohort characterization definition
     * @return The same cohort characterization definition that was passed as input
     */
    @PostMapping(value = "/import", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isPermitted('create:cohort-characterization')")
    public CohortCharacterizationDTO doImport(@RequestBody final CcExportDTO dto) {
        dto.setName(service.getNameWithSuffix(dto.getName()));
        dto.setTags(null);
        final CohortCharacterizationEntity entity = conversionService.convert(dto, CohortCharacterizationEntity.class);
        return conversionService.convert(service.importCc(entity), CohortCharacterizationDTO.class);
    }

    /**
     * Get a cohort characterization definition
     *
     * @param id The id of an existing cohort characterization definition
     * @return JSON containing the cohort characterization definition
     */
    @GetMapping(value = "/{id}/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isOwner(#id, COHORT_CHARACTERIZATION) or isPermitted('read:cohort-characterization','write:cohort-characterization') or hasEntityAccess(#id, COHORT_CHARACTERIZATION, READ)")
    public String export(@PathVariable("id") final Long id) {
        return service.serializeCc(id);
    }

    /**
     * Get csv files containing concept sets used in a characterization analysis
     * @param id The id for a cohort characterization analysis
     * @return A zip file containing three csv files (mappedConcepts, includedConcepts, conceptSetExpression)
     */
    @GetMapping(value = "/{id}/export/conceptset", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @PreAuthorize("isOwner(#id, COHORT_CHARACTERIZATION) or isPermitted('read:cohort-characterization') or hasEntityAccess(#id, COHORT_CHARACTERIZATION, READ)")
    public ResponseEntity<StreamingResponseBody> exportConceptSets(@PathVariable("id") final Long id) {

        CohortCharacterizationEntity cc = service.findById(id);
        ExceptionUtils.throwNotFoundExceptionIfNull(cc, String.format("There is no cohort characterization with id = %d.", id));
        List<ConceptSetExport> exportList = service.exportConceptSets(cc);
        ByteArrayOutputStream stream = ExportUtil.writeConceptSetExportToCSVAndZip(exportList);
        return HttpUtils.respondBinary(stream, String.format("cc_%d_export.zip", id));
    }

    /**
     * Check that a cohort characterization definition is correct
     * @summary Check a cohort characterization definition
     * @param characterizationDTO A cohort characterization definition object
     * @return A list of warnings that is possibly empty
     */
    @PostMapping(value = "/check", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public CheckResult runDiagnostics(@RequestBody CohortCharacterizationDTO characterizationDTO){
        return new CheckResult(checker.check(characterizationDTO));
    }

    /**
     * Generate a cohort characterization on a single data source
     * @param id The id of an existing cohort characterization in WebAPI
     * @param sourceKey The identifier for the data source to generate against
     * @return A json object with information about the generation job included the status and execution id.
     */
    @PostMapping(value = "/{id}/generation/{sourceKey}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("(isOwner(#id, COHORT_CHARACTERIZATION) or isPermitted(anyOf('write:cohort-characterization','read:cohort-characterization')) or hasEntityAccess(#id, COHORT_CHARACTERIZATION, READ)) and (isPermitted('write:source') or hasSourceAccess(#sourceKey, WRITE))")
    public JobExecutionResource generate(@PathVariable("id") final Long id, @PathVariable("sourceKey") final String sourceKey) {
        CohortCharacterizationEntity cc = service.findByIdWithLinkedEntities(id);
        ExceptionUtils.throwNotFoundExceptionIfNull(cc, String.format("There is no cohort characterization with id = %d.", id));
        CheckResult checkResult = runDiagnostics(convertCcToDto(cc));
        if (checkResult.hasCriticalErrors()) {
            throw new RuntimeException("Cannot be generated due to critical errors in design. Call 'check' service for further details");
        }
        return service.generateCc(id, sourceKey);
    }

    /**
     * Cancel a cohort characterization generation
     * @param id The id of an existing cohort characterization
     * @param sourceKey The sourceKey for the data source to generate against
     * @return Status code
     */
    @DeleteMapping(value = "/{id}/generation/{sourceKey}")
    @PreAuthorize("(isOwner(#id, COHORT_CHARACTERIZATION) or isPermitted(anyOf('write:cohort-characterization','read:cohort-characterization')) or hasEntityAccess(#id, COHORT_CHARACTERIZATION, READ)) and (isPermitted('write:source') or hasSourceAccess(#sourceKey, WRITE))")
    public ResponseEntity<Void> cancelGeneration(@PathVariable("id") final Long id, @PathVariable("sourceKey") final String sourceKey) {
        service.cancelGeneration(id, sourceKey);
        return ResponseEntity.ok().build();
    }

    /**
     * Get all generations for a cohort characterization
     * @param id The id for an existing cohort characterization
     * @return An array of all generations that includes the generation id, sourceKey, start and end times
     */
    @GetMapping(value = "/{id}/generation", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isOwner(#id, COHORT_CHARACTERIZATION) or isPermitted(anyOf('read:cohort-characterization','write:cohort-characterization')) or hasEntityAccess(#id, COHORT_CHARACTERIZATION, READ)")
    public List<CommonGenerationDTO> getGenerationList(@PathVariable("id") final Long id) {

        Map<String, Source> sourcesMap = sourceService.getSourcesMap(SourceMapKey.BY_SOURCE_KEY);
        return sensitiveInfoService.filterSensitiveInfo(converterUtils.convertList(service.findGenerationsByCcId(id), CommonGenerationDTO.class),
                info -> Collections.singletonMap(Constants.Variables.SOURCE, sourcesMap.get(info.getSourceKey())));
    }

    /**
     * Get generation information by generation id
     * @param generationId The generation id to look up
     * @return Data about the generation including the generation id, sourceKey, hashcode, start and end times
     */
    @GetMapping(value = "/generation/{generationId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonGenerationDTO getGeneration(@PathVariable("generationId") final Long generationId) {
        checkGenerationReadAccess(generationId);

        CcGenerationEntity generationEntity = service.findGenerationById(generationId);
        return sensitiveInfoService.filterSensitiveInfo(conversionService.convert(generationEntity, CommonGenerationDTO.class),
                Collections.singletonMap(Constants.Variables.SOURCE, generationEntity.getSource()));
    }

    /**
     * Delete a cohort characterization generation
     * @param generationId
     */
    @DeleteMapping(value = "/generation/{generationId}")
    public void deleteGeneration(@PathVariable("generationId") final Long generationId) {
        checkGenerationWriteAccess(generationId);
        service.deleteCcGeneration(generationId);
    }

    /**
     * Get the definition of a cohort characterization for a given generation id
     * @param generationId
     * @return A cohort characterization definition
     */
    @GetMapping(value = "/generation/{generationId}/design", produces = MediaType.APPLICATION_JSON_VALUE)
    public CcExportDTO getGenerationDesign(
            @PathVariable("generationId") final Long generationId) {
        checkGenerationReadAccess(generationId);
        return conversionService.convert(service.findDesignByGenerationId(generationId), CcExportDTO.class);
    }

    /**
     * Get the total number of analyses in a cohort characterization
     *
     * @param generationId
     * @return The total number of analyses in the given cohort characterization
     */
    @GetMapping(value = "/generation/{generationId}/result/count", produces = MediaType.APPLICATION_JSON_VALUE)
    public Long getGenerationsResultsCount( @PathVariable("generationId") final Long generationId) {
        checkGenerationReadAccess(generationId);
        return service.getCCResultsTotalCount(generationId);
    }

    /**
     * Get cohort characterization results
     * @param generationId id for generation
     * @param thresholdLevel The max prevelance for a covariate. Covariates that occur in less than {threholdLevel}%
     *                       of the cohort will not be returned. Default is 0.01 = 1%
     * @return The complete set of characterization analyses filtered by the thresholdLevel parameter
     */
    @GetMapping(value = "/generation/{generationId}/result", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<CcResult> getGenerationsResults(
            @PathVariable("generationId") final Long generationId, @RequestParam(value = "thresholdLevel", defaultValue = "0.01") final float thresholdLevel) {
        checkGenerationReadAccess(generationId);
        return service.findResultAsList(generationId, thresholdLevel);
    }

    @GetMapping(value = "/generation/{generationId}/temporalresult", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<CcTemporalResult> getGenerationTemporalResults(@PathVariable("generationId") final Long generationId) {
        checkGenerationReadAccess(generationId);
        return service.findTemporalResultAsList(generationId);
    }

    @PostMapping(value = "/generation/{generationId}/result", produces = MediaType.APPLICATION_JSON_VALUE)
    public GenerationResults getGenerationsResults(
            @PathVariable("generationId") final Long generationId, @RequestBody ExportExecutionResultRequest params) {
        checkGenerationReadAccess(generationId);
        return service.findData(generationId, params);
    }

    @PostMapping(value = "/generation/{generationId}/result/export", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<byte[]> exportGenerationsResults(
            @PathVariable("generationId") final Long generationId, @RequestBody ExportExecutionResultRequest params) {
        checkGenerationReadAccess(generationId);
        GenerationResults res = service.exportExecutionResult(generationId, params);
        return prepareExecutionResultResponse(res.getReports());
    }

    private ResponseEntity<byte[]> prepareExecutionResultResponse(List<Report> reports) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(baos)) {

            for (Report report : reports) {
                createZipEntry(zos, report);
            }

            zos.closeEntry();
            baos.flush();

            return ResponseEntity
                    .ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, String.format("attachment; filename=\"%s\"", "reports.zip"))
                    .body(baos.toByteArray());
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private void createZipEntry(ZipOutputStream zos, Report report) throws IOException {
        StringWriter sw = new StringWriter();
        CSVWriter csvWriter = new CSVWriter(sw, ',', CSVWriter.DEFAULT_QUOTE_CHARACTER, CSVWriter.DEFAULT_ESCAPE_CHARACTER);
        csvWriter.writeAll(report.header);
        csvWriter.writeAll(report.getResultArray());
        csvWriter.flush();

        String filename = report.analysisName;
        if (report.isComparative) {
            filename = "Export comparison (" + filename + ")";
        } else {
            filename = "Export (" + filename + ")";
        }
        // trim the name so it can be opened by archiver,
        // -1 is for dot character
        if (filename.length() >= 64) {
            filename = filename.substring(0, 63);
        }
        filename = CommonFilenameUtils.sanitizeFilename(filename);
        ZipEntry resultsEntry = new ZipEntry(filename + ".csv");
        zos.putNextEntry(resultsEntry);
        zos.write(sw.getBuffer().toString().getBytes());
    }

    @GetMapping(value = "/generation/{generationId}/explore/prevalence/{analysisId}/{cohortId}/{covariateId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<CcPrevalenceStat> getPrevalenceStat(@PathVariable("generationId") Long generationId,
                                                    @PathVariable("analysisId") Long analysisId,
                                                    @PathVariable("cohortId") Long cohortId,
                                                    @PathVariable("covariateId") Long covariateId) {
        checkGenerationReadAccess(generationId);

        Integer presetId = convertPresetAnalysisIdToSystem(Math.toIntExact(analysisId));
        List<CcPrevalenceStat> stats = service.getPrevalenceStatsByGenerationId(generationId, Long.valueOf(presetId), cohortId, covariateId);
        convertPresetAnalysesToLocal(stats);
        return stats;
    }

    /**
     * Assign tag to Cohort Characterization
     *
     * @param id
     * @param tagId
     */
    @PostMapping(value = "/{id}/tag/", produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional
    @PreAuthorize("isOwner(#id, COHORT_CHARACTERIZATION) or isPermitted('admin:tags') or isPermitted('write:cohort-characterization') or hasEntityAccess(#id, COHORT_CHARACTERIZATION, WRITE)")
    public void assignTag(@PathVariable("id") final long id, @RequestBody final int tagId) {
        service.assignTag(id, tagId);
    }

    /**
     * Unassign tag from Cohort Characterization
     *
     * @param id
     * @param tagId
     */
    @DeleteMapping(value = "/{id}/tag/{tagId}")
    @Transactional
    @PreAuthorize("isOwner(#id, COHORT_CHARACTERIZATION) or isPermitted('admin:tags') or isPermitted('write:cohort-characterization') or hasEntityAccess(#id, COHORT_CHARACTERIZATION, WRITE)")
    public void unassignTag(@PathVariable("id") final long id, @PathVariable("tagId") final int tagId) {
        service.unassignTag(id, tagId);
    }

    /**
     * Assign protected tag to Cohort Characterization
     *
     * @param id
     * @param tagId
     */
    @PostMapping(value = "/{id}/protectedtag/")
    @Transactional
    @PreAuthorize("isOwner(#id, COHORT_CHARACTERIZATION) or isPermitted('admin:tags') or isPermitted('write:cohort-characterization') or hasEntityAccess(#id, COHORT_CHARACTERIZATION, WRITE)")
    public void assignPermissionProtectedTag(@PathVariable("id") final long id, @RequestBody final int tagId) {
        service.assignTag(id, tagId);
    }

    /**
     * Unassign protected tag from Cohort Characterization
     *
     * @param id
     * @param tagId
     */
    @DeleteMapping(value = "/{id}/protectedtag/{tagId}")
    @Transactional
    @PreAuthorize("isOwner(#id, COHORT_CHARACTERIZATION) or isPermitted('admin:tags') or isPermitted('write:cohort-characterization') or hasEntityAccess(#id, COHORT_CHARACTERIZATION, WRITE)")
    public void unassignPermissionProtectedTag(@PathVariable("id") final long id, @PathVariable("tagId") final int tagId) {
        service.unassignTag(id, tagId);
    }

    /**
     * Get list of versions of Cohort Characterization
     *
     * @param id
     * @return
     */
    @GetMapping(value = "/{id}/version/", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isOwner(#id, COHORT_CHARACTERIZATION) or isPermitted(anyOf('read:cohort-characterization','write:cohort-characterization')) or hasEntityAccess(#id, COHORT_CHARACTERIZATION, READ)")
    public List<VersionDTO> getVersions(@PathVariable("id") final long id) {
        return service.getVersions(id);
    }

    /**
     * Get version of Cohort Characterization
     *
     * @param id
     * @param version
     * @return
     */
    @GetMapping(value = "/{id}/version/{version}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isOwner(#id, COHORT_CHARACTERIZATION) or isPermitted(anyOf('read:cohort-characterization','write:cohort-characterization')) or hasEntityAccess(#id, COHORT_CHARACTERIZATION, READ)")
    public CcVersionFullDTO getVersion(@PathVariable("id") final long id, @PathVariable("version") final int version) {
        return service.getVersion(id, version);
    }

    /**
     * Update version of Cohort Characterization
     *
     * @param id
     * @param version
     * @param updateDTO
     * @return
     */
    @PutMapping(value = "/{id}/version/{version}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isOwner(#id, COHORT_CHARACTERIZATION) or isPermitted('write:cohort-characterization') or hasEntityAccess(#id, COHORT_CHARACTERIZATION, WRITE)")
    public VersionDTO updateVersion(@PathVariable("id") final long id, @PathVariable("version") final int version,
                                    @RequestBody VersionUpdateDTO updateDTO) {
        return service.updateVersion(id, version, updateDTO);
    }

    /**
     * Delete version of Cohort Characterization
     *
     * @param id
     * @param version
     */
    @DeleteMapping(value = "/{id}/version/{version}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isOwner(#id, COHORT_CHARACTERIZATION) or isPermitted('write:cohort-characterization') or hasEntityAccess(#id, COHORT_CHARACTERIZATION, WRITE)")
    public void deleteVersion(@PathVariable("id") final long id, @PathVariable("version") final int version) {
        service.deleteVersion(id, version);
    }

    /**
     * Create a new asset form version of Cohort Characterization
     *
     * @param id
     * @param version
     * @return
     */
    @PutMapping(value = "/{id}/version/{version}/createAsset", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isOwner(#id, COHORT_CHARACTERIZATION) or isPermitted('write:cohort-characterization') or hasEntityAccess(#id, COHORT_CHARACTERIZATION, WRITE)")
    public CohortCharacterizationDTO copyAssetFromVersion(@PathVariable("id") final long id,
                                                          @PathVariable("version") final int version) {
        return service.copyAssetFromVersion(id, version);
    }

    /**
     * Get list of cohort characterizations with assigned tags
     *
     * @param requestDTO
     * @return
     */
    @PostMapping(value = "/byTags", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public List<CcShortDTO> listByTags(@RequestBody TagNameListRequestDTO requestDTO) {
        if (requestDTO == null || requestDTO.getNames() == null || requestDTO.getNames().isEmpty()) {
            return Collections.emptyList();
        }
        return service.listByTags(requestDTO);
    }

    // --- generation-level authorization helpers (generationId-only endpoints)
    private void checkGenerationReadAccess(Long generationId) {
        CcGenerationEntity generationEntity = service.findGenerationById(generationId);
        ExceptionUtils.throwNotFoundExceptionIfNull(generationEntity, String.format("There is no generation with id = %d.", generationId));
        CohortCharacterizationEntity cc = generationEntity.getCohortCharacterization();
        if (cc == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Associated cohort characterization not found");
        }
        String sourceKey = generationEntity.getSource() != null ? generationEntity.getSource().getSourceKey() : null;

        boolean ccAllowed = authorizationService.isOwner(cc.getId(), EntityType.COHORT_CHARACTERIZATION)
                || authorizationService.isPermitted("read:cohort-characterization")
                || authorizationService.isPermitted("write:cohort-characterization")
                || authorizationService.hasEntityAccess(cc.getId(), EntityType.COHORT_CHARACTERIZATION, AccessType.READ);

        boolean sourceAllowed = sourceKey != null && (authorizationService.isPermitted("read:source")
                || authorizationService.isPermitted("write:source")
                || authorizationService.hasSourceAccess(sourceKey, AccessType.READ));

        if (!ccAllowed || !sourceAllowed) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
    }

    private void checkGenerationWriteAccess(Long generationId) {
        CcGenerationEntity generationEntity = service.findGenerationById(generationId);
        ExceptionUtils.throwNotFoundExceptionIfNull(generationEntity, String.format("There is no generation with id = %d.", generationId));
        CohortCharacterizationEntity cc = generationEntity.getCohortCharacterization();
        if (cc == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Associated cohort characterization not found");
        }
        String sourceKey = generationEntity.getSource() != null ? generationEntity.getSource().getSourceKey() : null;

        boolean ccAllowed = authorizationService.isOwner(cc.getId(), EntityType.COHORT_CHARACTERIZATION)
                || authorizationService.isPermitted("read:cohort-characterization")
                || authorizationService.isPermitted("write:cohort-characterization")
                || authorizationService.hasEntityAccess(cc.getId(), EntityType.COHORT_CHARACTERIZATION, AccessType.READ);

        boolean sourceAllowed = sourceKey != null && (authorizationService.isPermitted("write:source")
                || authorizationService.hasSourceAccess(sourceKey, AccessType.WRITE));

        if (!ccAllowed || !sourceAllowed) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
    }

    private void convertPresetAnalysesToLocal(List<? extends CcResult> ccResults) {

      List<FeAnalysisWithStringEntity> presetFeAnalyses = feAnalysisService.findPresetAnalysesBySystemNames(ccResults.stream().map(CcResult::getAnalysisName).distinct().collect(Collectors.toList()));
      ccResults.stream().filter(res -> Objects.equals(res.getFaType(), StandardFeatureAnalysisType.PRESET.name()))
              .forEach(res -> {
                presetFeAnalyses.stream().filter(fa -> fa.getDesign().equals(res.getAnalysisName())).findFirst().ifPresent(fa -> {
                  res.setAnalysisId(fa.getId());
                  res.setAnalysisName(fa.getName());
                });
              });
    }

    private Integer convertPresetAnalysisIdToSystem(Integer analysisId) {

        FeAnalysisEntity fe = feAnalysisService.findById(analysisId).orElse(null);
        if (fe instanceof FeAnalysisWithStringEntity && fe.isPreset()) {
            FeatureExtraction.PrespecAnalysis prespecAnalysis = FeatureExtraction.getNameToPrespecAnalysis().get(((FeAnalysisWithStringEntity) fe).getDesign());
            return prespecAnalysis.analysisId;
        }
        return analysisId;
    }
}
