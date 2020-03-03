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
import org.ohdsi.webapi.cohortcharacterization.domain.CcGenerationEntity;
import org.ohdsi.webapi.cohortcharacterization.domain.CohortCharacterizationEntity;
import org.ohdsi.webapi.cohortcharacterization.dto.CcExportDTO;
import org.ohdsi.webapi.cohortcharacterization.dto.CcPrevalenceStat;
import org.ohdsi.webapi.cohortcharacterization.dto.CcResult;
import org.ohdsi.webapi.cohortcharacterization.dto.CcShortDTO;
import org.ohdsi.webapi.cohortcharacterization.dto.CohortCharacterizationDTO;
import org.ohdsi.webapi.cohortcharacterization.dto.ExportExecutionResultRequest;
import org.ohdsi.webapi.cohortcharacterization.dto.GenerationResults;
import org.ohdsi.webapi.cohortcharacterization.report.Report;
import org.ohdsi.webapi.common.SourceMapKey;
import org.ohdsi.webapi.common.generation.CommonGenerationDTO;
import org.ohdsi.webapi.common.sensitiveinfo.CommonGenerationSensitiveInfoService;
import org.ohdsi.webapi.feanalysis.FeAnalysisService;
import org.ohdsi.webapi.feanalysis.domain.FeAnalysisEntity;
import org.ohdsi.webapi.feanalysis.domain.FeAnalysisWithStringEntity;
import org.ohdsi.webapi.job.JobExecutionResource;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceService;
import org.ohdsi.webapi.util.ExceptionUtils;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
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
import javax.ws.rs.core.Response;
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

    public CcController(
            final CcService service,
            final FeAnalysisService feAnalysisService,
            final ConversionService conversionService,
            final ConverterUtils converterUtils,
            CommonGenerationSensitiveInfoService sensitiveInfoService,
            SourceService sourceService) {
        this.service = service;
        this.feAnalysisService = feAnalysisService;
        this.conversionService = conversionService;
        this.converterUtils = converterUtils;
        this.sensitiveInfoService = sensitiveInfoService;
        this.sourceService = sourceService;
        FeatureExtraction.init(null);
    }

    @POST
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public CohortCharacterizationDTO create(final CohortCharacterizationDTO dto) {
        final CohortCharacterizationEntity createdEntity = service.createCc(conversionService.convert(dto, CohortCharacterizationEntity.class));
        return conversionService.convert(createdEntity, CohortCharacterizationDTO.class);
    }

    @POST
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public CohortCharacterizationDTO copy(@PathParam("id") final Long id) {
        CohortCharacterizationDTO dto = getDesign(id);
        dto.setName(service.getNameForCopy(dto.getName()));
        dto.setId(null);
        dto.getStratas().forEach(s -> s.setId(null));
        dto.getParameters().forEach(p -> p.setId(null));
        return create(dto);
    }

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Page<CcShortDTO> list(@Pagination Pageable pageable) {
        return service.getPage(pageable).map(this::convertCcToShortDto);
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
        CohortCharacterization  cc = service.findByIdWithLinkedEntities(id);
        ExceptionUtils.throwNotFoundExceptionIfNull(cc, String.format("There is no cohort characterization with id = %d.", id));
        return convertCcToDto(service.findByIdWithLinkedEntities(id));
    }

    @GET
    @Path("/{id}/exists")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public int getCountCcWithSameName(@PathParam("id") @DefaultValue("0") final long id, @QueryParam("name") String name) {
        return service.getCountCcWithSameName(id, name);
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
        dto.setName(service.getNameWithSuffix(dto.getName()));
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

    @DELETE
    @Path("/{id}/generation/{sourceKey}")
    public Response cancelGeneration(@PathParam("id") final Long id, @PathParam("sourceKey") final String sourceKey) {
        service.cancelGeneration(id, sourceKey);
        return Response.ok().build();
    }

    @GET
    @Path("/{id}/generation")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public List<CommonGenerationDTO> getGenerationList(@PathParam("id") final Long id) {

        Map<String, Source> sourcesMap = sourceService.getSourcesMap(SourceMapKey.BY_SOURCE_KEY);
        return sensitiveInfoService.filterSensitiveInfo(converterUtils.convertList(service.findGenerationsByCcId(id), CommonGenerationDTO.class),
                info -> Collections.singletonMap(Constants.Variables.SOURCE, sourcesMap.get(info.getSourceKey())));
    }

    @GET
    @Path("/generation/{generationId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public CommonGenerationDTO getGeneration(@PathParam("generationId") final Long generationId) {

        CcGenerationEntity generationEntity = service.findGenerationById(generationId);
        return sensitiveInfoService.filterSensitiveInfo(conversionService.convert(generationEntity, CommonGenerationDTO.class),
                Collections.singletonMap(Constants.Variables.SOURCE, generationEntity.getSource()));
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
    @Path("/generation/{generationId}/result/count")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Long getGenerationsResultsCount( @PathParam("generationId") final Long generationId) {
        return service.getCCResultsTotalCount(generationId);
    }

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
