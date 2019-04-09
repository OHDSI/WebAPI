package org.ohdsi.webapi.cohortcharacterization;

import com.odysseusinc.arachne.commons.utils.ConverterUtils;
import org.ohdsi.analysis.Utils;
import org.ohdsi.analysis.cohortcharacterization.design.CohortCharacterization;
import org.ohdsi.analysis.cohortcharacterization.design.StandardFeatureAnalysisType;
import org.ohdsi.featureExtraction.FeatureExtraction;
import org.ohdsi.webapi.Constants;
import org.ohdsi.webapi.Pagination;
import org.ohdsi.webapi.cohortcharacterization.domain.CcGenerationEntity;
import org.ohdsi.webapi.cohortcharacterization.domain.CohortCharacterizationEntity;
import org.ohdsi.webapi.cohortcharacterization.dto.*;
import org.ohdsi.webapi.common.SourceMapKey;
import org.ohdsi.webapi.common.generation.CommonGenerationDTO;
import org.ohdsi.webapi.common.sensitiveinfo.CommonGenerationSensitiveInfoService;
import org.ohdsi.webapi.feanalysis.FeAnalysisService;
import org.ohdsi.webapi.feanalysis.domain.FeAnalysisEntity;
import org.ohdsi.webapi.feanalysis.domain.FeAnalysisWithStringEntity;
import org.ohdsi.webapi.job.JobExecutionResource;
import org.ohdsi.webapi.service.SourceService;
import org.ohdsi.webapi.source.SourceInfo;
import org.ohdsi.webapi.util.ExceptionUtils;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

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

    CcController(
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
        dto.setName(service.getNameForCopy(dto.getName()));
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

        Map<String, SourceInfo> sourcesMap = sourceService.getSourcesMap(SourceMapKey.BY_SOURCE_KEY);
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
    @Path("/generation/{generationId}/result")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public List<CcResult> getGenerationsResults(
            @PathParam("generationId") final Long generationId, @DefaultValue("0.01") @QueryParam("thresholdLevel") final float thresholdLevel) {
        List<CcResult> ccResults = service.findResults(generationId, thresholdLevel);
        convertPresetAnalysesToLocal(ccResults);
        return ccResults;
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
