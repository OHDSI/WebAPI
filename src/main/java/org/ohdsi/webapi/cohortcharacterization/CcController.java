package org.ohdsi.webapi.cohortcharacterization;

import com.odysseusinc.arachne.commons.utils.CommonFilenameUtils;
import com.odysseusinc.arachne.commons.utils.ConverterUtils;
import com.opencsv.CSVWriter;
import com.qmino.miredot.annotations.ReturnType;
import org.ohdsi.analysis.Utils;
import org.ohdsi.analysis.cohortcharacterization.design.CohortCharacterization;
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
import org.ohdsi.webapi.security.PermissionService;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestBody;

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
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Path("/cohort-characterization")
@Controller
@Transactional
public class CcController {

    private CcService service;
    private FeAnalysisService feAnalysisService;
    private ConversionService conversionService;
    private ConverterUtils converterUtils;
    private final CommonGenerationSensitiveInfoService<CommonGenerationDTO> sensitiveInfoService;
    private final SourceService sourceService;
    private CharacterizationChecker checker;
    private PermissionService permissionService;

    public CcController(
            final CcService service,
            final FeAnalysisService feAnalysisService,
            final ConversionService conversionService,
            final ConverterUtils converterUtils,
            CommonGenerationSensitiveInfoService sensitiveInfoService,
            SourceService sourceService, CharacterizationChecker checker,
            PermissionService permissionService) {
        this.service = service;
        this.feAnalysisService = feAnalysisService;
        this.conversionService = conversionService;
        this.converterUtils = converterUtils;
        this.sensitiveInfoService = sensitiveInfoService;
        this.sourceService = sourceService;
        this.checker = checker;
        this.permissionService = permissionService;
        FeatureExtraction.init(null);
    }

    /**
     * Create a new cohort characterization
     *
     * @param dto A cohort characterization JSON definition (name, cohorts, featureAnalyses, etc.)
     * @return The cohort characterization definition passed in as input
     * with additional fields (createdDate, hasWriteAccess, tags, id, hashcode).
     */
    @POST
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Transactional
    public CohortCharacterizationDTO create(final CohortCharacterizationDTO dto) {
        final CohortCharacterizationEntity createdEntity = service.createCc(conversionService.convert(dto, CohortCharacterizationEntity.class));
        return conversionService.convert(createdEntity, CohortCharacterizationDTO.class);
    }

    /**
     * Create a copy of an existing cohort characterization
     *
     * @param id An existing cohort characterization id
     * @return The cohort characterization definition of the newly created copy
     */
    @POST
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public CohortCharacterizationDTO copy(@PathParam("id") final Long id) {
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
    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Page<CcShortDTO> list(@Pagination Pageable pageable) {
      return service.getPage(pageable).map(entity -> {
          CcShortDTO dto = convertCcToShortDto(entity);
          permissionService.fillWriteAccess(entity, dto);
          permissionService.fillReadAccess(entity, dto);
          return dto;
      });
    }

    /**
     * Get the design specification for every cohort-characterization analysis in WebAPI.
     *
     * @return A json object with all characterization design specifications.
     */
    @GET
    @Path("/design")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Page<CohortCharacterizationDTO> listDesign(@Pagination Pageable pageable) {
        return service.getPageWithLinkedEntities(pageable).map(entity -> {
          CohortCharacterizationDTO dto = convertCcToDto(entity);
          permissionService.fillWriteAccess(entity, dto);
          permissionService.fillReadAccess(entity, dto);
          return dto;
      });
    }

    /**
     * Get metadata about a cohort characterization.
     *
     * @param id The id for an existing cohort characterization
     * @return name, createdDate, tags, etc for a single cohort characterization.
     */
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public CcShortDTO get(@PathParam("id") final Long id) {
        return convertCcToShortDto(service.findById(id));
    }

    /**
     * Get the complete design specification for a single cohort characterization.
     *
     * @param id The id for an existing cohort characterization
     * @return JSON containing the cohort characterization specification
     */
    @GET
    @Path("/{id}/design")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public CohortCharacterizationDTO getDesign(@PathParam("id") final Long id) {
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
    @GET
    @Path("/{id}/exists")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public int getCountCcWithSameName(@PathParam("id") @DefaultValue("0") final long id, @QueryParam("name") String name) {
        return service.getCountCcWithSameName(id, name);
    }

    /**
     * Remove a characterization from WebAPI
     *
     * @param id The id for a characterization that currently exists in WebAPI
     */
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
        service.saveVersion(dto.getId());
        final CohortCharacterizationEntity entity = conversionService.convert(dto, CohortCharacterizationEntity.class);
        entity.setId(id);
        final CohortCharacterizationEntity updatedEntity = service.updateCc(entity);
        return conversionService.convert(updatedEntity, CohortCharacterizationDTO.class);
    }

    /**
     * Add a new cohort characterization analysis to WebAPI
     *
     * @chrisknoll this endpoint did not work when I tried it.
     *
     * @param dto A cohort characterization definition
     * @return The same cohort characterization definition that was passed as input
     */
    @POST
    @Path("/import")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public CohortCharacterizationDTO doImport(final CcExportDTO dto) {
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
    @GET
    @Path("/{id}/export")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public String export(@PathParam("id") final Long id) {
        return service.serializeCc(id);
    }

    /**
     * Get csv files containing concept sets used in a characterization analysis
     * @param id The id for a cohort characterization analysis
     * @return A zip file containing three csv files (mappedConcepts, includedConcepts, conceptSetExpression)
     */
    @GET
    @Path("/{id}/export/conceptset")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response exportConceptSets(@PathParam("id") final Long id) {

        CohortCharacterizationEntity cc = service.findById(id);
        Optional.ofNullable(cc).orElseThrow(NotFoundException::new);
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
    @POST
    @Path("/check")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public CheckResult runDiagnostics(CohortCharacterizationDTO characterizationDTO){
        return new CheckResult(checker.check(characterizationDTO));
    }

    /**
     * Generate a cohort characterization on a single data source
     * @param id The id of an existing cohort characterization in WebAPI
     * @param sourceKey The identifier for the data source to generate against
     * @return A json object with information about the generation job included the status and execution id.
     */
    @POST
    @Path("/{id}/generation/{sourceKey}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public JobExecutionResource generate(@PathParam("id") final Long id, @PathParam("sourceKey") final String sourceKey) {
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
    @DELETE
    @Path("/{id}/generation/{sourceKey}")
    public Response cancelGeneration(@PathParam("id") final Long id, @PathParam("sourceKey") final String sourceKey) {
        service.cancelGeneration(id, sourceKey);
        return Response.ok().build();
    }

    /**
     * Get all generations for a cohort characterization
     * @param id The id for an existing cohort characterization
     * @return An array of all generations that includes the generation id, sourceKey, start and end times
     */
    @GET
    @Path("/{id}/generation")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public List<CommonGenerationDTO> getGenerationList(@PathParam("id") final Long id) {

        Map<String, Source> sourcesMap = sourceService.getSourcesMap(SourceMapKey.BY_SOURCE_KEY);
        return sensitiveInfoService.filterSensitiveInfo(converterUtils.convertList(service.findGenerationsByCcId(id), CommonGenerationDTO.class),
                info -> Collections.singletonMap(Constants.Variables.SOURCE, sourcesMap.get(info.getSourceKey())));
    }

    /**
     * Get generation information by generation id
     * @param generationId The generation id to look up
     * @return Data about the generation including the generation id, sourceKey, hashcode, start and end times
     */
    @GET
    @Path("/generation/{generationId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public CommonGenerationDTO getGeneration(@PathParam("generationId") final Long generationId) {

        CcGenerationEntity generationEntity = service.findGenerationById(generationId);
        return sensitiveInfoService.filterSensitiveInfo(conversionService.convert(generationEntity, CommonGenerationDTO.class),
                Collections.singletonMap(Constants.Variables.SOURCE, generationEntity.getSource()));
    }

    /**
     * Delete a cohort characterization generation
     * @param generationId
     */
    @DELETE
    @Path("/generation/{generationId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public void deleteGeneration(@PathParam("generationId") final Long generationId) {
        service.deleteCcGeneration(generationId);
    }

    /**
     * Get the definition of a cohort characterization for a given generation id
     * @param generationId
     * @return A cohort characterization definition
     */
    @GET
    @Path("/generation/{generationId}/design")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public CcExportDTO getGenerationDesign(
            @PathParam("generationId") final Long generationId) {
        return conversionService.convert(service.findDesignByGenerationId(generationId), CcExportDTO.class);
    }

    /**
     * Get the total number of analyses in a cohort characterization
     *
     * @param generationId
     * @return The total number of analyses in the given cohort characterization
     */
    @GET
    @Path("/generation/{generationId}/result/count")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Long getGenerationsResultsCount( @PathParam("generationId") final Long generationId) {
        return service.getCCResultsTotalCount(generationId);
    }

    /**
     * Get cohort characterization results
     * @param generationId id for generation
     * @param thresholdLevel The max prevelance for a covariate. Covariates that occur in less than {threholdLevel}%
     *                       of the cohort will not be returned. Default is 0.01 = 1%
     * @return The complete set of characterization analyses filtered by the thresholdLevel parameter
     */
    @GET
    @Path("/generation/{generationId}/result")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public List<CcResult> getGenerationsResults(
            @PathParam("generationId") final Long generationId, @DefaultValue("0.01") @QueryParam("thresholdLevel") final float thresholdLevel) {
        return service.findResultAsList(generationId, thresholdLevel);
    }

    @POST
    @Path("/generation/{generationId}/result")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ReturnType("java.lang.Object")
    public GenerationResults getGenerationsResults(
            @PathParam("generationId") final Long generationId, @RequestBody ExportExecutionResultRequest params) {
        return service.findData(generationId, params);
    }

    @POST
    @Path("/generation/{generationId}/result/export")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response exportGenerationsResults(
            @PathParam("generationId") final Long generationId, ExportExecutionResultRequest params) {
        GenerationResults res = service.exportExecutionResult(generationId, params);
        return prepareExecutionResultResponse(res.getReports());
    }

    private Response prepareExecutionResultResponse(List<Report> reports) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(baos)) {

            for (Report report : reports) {
                createZipEntry(zos, report);
            }

            zos.closeEntry();
            baos.flush();

            return Response
                    .ok(baos)
                    .type(MediaType.APPLICATION_OCTET_STREAM)
                    .header("Content-Disposition", String.format("attachment; filename=\"%s\"", "reports.zip"))
                    .build();
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

    @GET
    @Path("/generation/{generationId}/explore/prevalence/{analysisId}/{cohortId}/{covariateId}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<CcPrevalenceStat> getPrevalenceStat(@PathParam("generationId") Long generationId,
                                                    @PathParam("analysisId") Long analysisId,
                                                    @PathParam("cohortId") Long cohortId,
                                                    @PathParam("covariateId") Long covariateId) {

        Integer presetId = convertPresetAnalysisIdToSystem(Math.toIntExact(analysisId));
        List<CcPrevalenceStat> stats = service.getPrevalenceStatsByGenerationId(generationId, Long.valueOf(presetId), cohortId, covariateId);
        convertPresetAnalysesToLocal(stats);
        return stats;
    }

    /**
     * Download a cohort characterization R study package that can be used to run the characterization on an OMOP CDM from R
     * @summary Download a cohort characterization R package
     * @param analysisId id of the cohort characterization to convert to an R study package
     * @param packageName The name of the R study package
     * @return A zip file containing the cohort characterization R study package
     */
    @GET
    @Path("{id}/download")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response downloadPackage(@PathParam("id") Long analysisId, @QueryParam("packageName") String packageName) {

        if (packageName == null) {
            packageName = "CohortCharacterization" + String.valueOf(analysisId);
        }
        if (!Utils.isAlphaNumeric(packageName)) {
            throw new IllegalArgumentException("The package name must be alphanumeric only.");
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        service.hydrateAnalysis(analysisId, packageName, baos);

        return Response
                .ok(baos)
                .type(MediaType.APPLICATION_OCTET_STREAM)
                .header("Content-Disposition", String.format("attachment; filename=\"cohort_characterization_study_%d_export.zip\"", analysisId))
                .build();
    }

    /**
     * Assign tag to Cohort Characterization
     *
     * @param id
     * @param tagId
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/tag/")
    @javax.transaction.Transactional
    public void assignTag(@PathParam("id") final long id, final int tagId) {
        service.assignTag(id, tagId);
    }

    /**
     * Unassign tag from Cohort Characterization
     *
     * @param id
     * @param tagId
     */
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/tag/{tagId}")
    @javax.transaction.Transactional
    public void unassignTag(@PathParam("id") final long id, @PathParam("tagId") final int tagId) {
        service.unassignTag(id, tagId);
    }

    /**
     * Assign protected tag to Cohort Characterization
     *
     * @param id
     * @param tagId
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/protectedtag/")
    @javax.transaction.Transactional
    public void assignPermissionProtectedTag(@PathParam("id") final long id, final int tagId) {
        service.assignTag(id, tagId);
    }

    /**
     * Unassign protected tag from Cohort Characterization
     *
     * @param id
     * @param tagId
     */
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/protectedtag/{tagId}")
    @javax.transaction.Transactional
    public void unassignPermissionProtectedTag(@PathParam("id") final long id, @PathParam("tagId") final int tagId) {
        service.unassignTag(id, tagId);
    }

    /**
     * Get list of versions of Cohort Characterization
     *
     * @param id
     * @return
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/version/")
    public List<VersionDTO> getVersions(@PathParam("id") final long id) {
        return service.getVersions(id);
    }

    /**
     * Get version of Cohort Characterization
     *
     * @param id
     * @param version
     * @return
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/version/{version}")
    public CcVersionFullDTO getVersion(@PathParam("id") final long id, @PathParam("version") final int version) {
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
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/version/{version}")
    public VersionDTO updateVersion(@PathParam("id") final long id, @PathParam("version") final int version,
                                    VersionUpdateDTO updateDTO) {
        return service.updateVersion(id, version, updateDTO);
    }

    /**
     * Delete version of Cohort Characterization
     *
     * @param id
     * @param version
     */
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/version/{version}")
    public void deleteVersion(@PathParam("id") final long id, @PathParam("version") final int version) {
        service.deleteVersion(id, version);
    }

    /**
     * Create a new asset form version of Cohort Characterization
     *
     * @param id
     * @param version
     * @return
     */
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/version/{version}/createAsset")
    public CohortCharacterizationDTO copyAssetFromVersion(@PathParam("id") final long id,
                                                          @PathParam("version") final int version) {
        return service.copyAssetFromVersion(id, version);
    }

    /**
     * Get list of cohort characterizations with assigned tags
     *
     * @param requestDTO
     * @return
     */
    @POST
    @Path("/byTags")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public List<CcShortDTO> listByTags(TagNameListRequestDTO requestDTO) {
        if (requestDTO == null || requestDTO.getNames() == null || requestDTO.getNames().isEmpty()) {
            return Collections.emptyList();
        }
        return service.listByTags(requestDTO);
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
