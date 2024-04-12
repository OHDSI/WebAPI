package org.ohdsi.webapi.pathway;

import com.odysseusinc.arachne.commons.utils.ConverterUtils;
import org.ohdsi.webapi.Constants;
import org.ohdsi.webapi.Pagination;
import org.ohdsi.webapi.check.CheckResult;
import org.ohdsi.webapi.check.checker.pathway.PathwayChecker;
import org.ohdsi.webapi.common.SourceMapKey;
import org.ohdsi.webapi.common.generation.CommonGenerationDTO;
import org.ohdsi.webapi.common.sensitiveinfo.CommonGenerationSensitiveInfoService;
import org.ohdsi.webapi.i18n.I18nService;
import org.ohdsi.webapi.job.JobExecutionResource;
import org.ohdsi.webapi.pathway.converter.SerializedPathwayAnalysisToPathwayAnalysisConverter;
import org.ohdsi.webapi.pathway.domain.PathwayAnalysisEntity;
import org.ohdsi.webapi.pathway.domain.PathwayAnalysisGenerationEntity;
import org.ohdsi.webapi.pathway.dto.*;
import org.ohdsi.webapi.pathway.dto.internal.PathwayAnalysisResult;
import org.ohdsi.webapi.security.PermissionService;
import org.ohdsi.webapi.source.SourceService;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.tag.TagService;
import org.ohdsi.webapi.tag.dto.TagNameListRequestDTO;
import org.ohdsi.webapi.util.ExportUtil;
import org.ohdsi.webapi.util.ExceptionUtils;
import org.ohdsi.webapi.versioning.dto.VersionDTO;
import org.ohdsi.webapi.versioning.dto.VersionUpdateDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;

import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Path("/pathway-analysis")
@Controller
public class PathwayController {

	private ConversionService conversionService;
	private ConverterUtils converterUtils;
	private PathwayService pathwayService;
	private final SourceService sourceService;
	private final CommonGenerationSensitiveInfoService<CommonGenerationDTO> sensitiveInfoService;
	private final I18nService i18nService;
	private PathwayChecker checker;
	private PermissionService permissionService;

	@Autowired
	public PathwayController(ConversionService conversionService, ConverterUtils converterUtils, PathwayService pathwayService, SourceService sourceService, CommonGenerationSensitiveInfoService sensitiveInfoService, PathwayChecker checker, PermissionService permissionService, I18nService i18nService) {

		this.conversionService = conversionService;
		this.converterUtils = converterUtils;
		this.pathwayService = pathwayService;
		this.sourceService = sourceService;
		this.sensitiveInfoService = sensitiveInfoService;
		this.i18nService = i18nService;
		this.checker = checker;
		this.permissionService = permissionService;
	}

	/**
	 * Create a new pathway analysis design.
	 *
	 * A pathway analysis consists of a set of target cohorts, event cohorts, and
	 * analysis settings for collapsing and repeat events.
	 *
	 * By default, the new design will have the createdBy set to the authenticated
	 * user, and the createdDate to the current time.
	 *
	 * @summary Create Pathway Analysis
	 * @param dto the pathway analysis design
	 * @return the created pathway analysis
	 */
	@POST
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Transactional
	public PathwayAnalysisDTO create(final PathwayAnalysisDTO dto) {

		PathwayAnalysisEntity pathwayAnalysis = conversionService.convert(dto, PathwayAnalysisEntity.class);
		PathwayAnalysisEntity saved = pathwayService.create(pathwayAnalysis);
		return reloadAndConvert(saved.getId());
	}

	/**
	 * Creates a copy of a pathway analysis.
	 *
	 * The new pathway will be a copy of the specified pathway analysis id, but
	 * won't contain any tag assignments.
	 *
	 * @summary Copy Pathway Analysis
	 * @param id the analysis to copy
	 * @return The copied pathway analysis.
	 */
	@POST
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Transactional
	public PathwayAnalysisDTO copy(@PathParam("id") final Integer id) {

		PathwayAnalysisDTO dto = get(id);
		dto.setId(null);
		dto.setName(pathwayService.getNameForCopy(dto.getName()));
		dto.setTags(null);
		return create(dto);
	}

	/**
	 * Import a pathway analysis
	 *
	 * The imported analysis contains the cohort definitions referenced by the
	 * targets and event cohort paramaters. During import, any cohort definition
	 * not found (by a hash check) will be inserted into the database as a new
	 * cohort definition, and the cohort definition ids that are referenced will
	 * be updated to reflect the new cohort definition ids.
	 *
	 * @param dto
	 * @return
	 */
	@POST
	@Path("/import")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Transactional
	public PathwayAnalysisDTO importAnalysis(final PathwayAnalysisExportDTO dto) {
		dto.setTags(null);
		dto.setName(pathwayService.getNameWithSuffix(dto.getName()));
		PathwayAnalysisEntity pathwayAnalysis = conversionService.convert(dto, PathwayAnalysisEntity.class);
		PathwayAnalysisEntity imported = pathwayService.importAnalysis(pathwayAnalysis);
		return reloadAndConvert(imported.getId());
	}

	/**
	 * Get a page of pathway analysis designs for list
	 *
	 * From the selected page, a list of PathwayAnalysisDTOs are returned
	 * containing summary information about the analysis (name, id, modified
	 * dates, etc) * @param pageable indicates how many elements per page to
	 * return, and which page to fetch
	 * 
	 * @summary List Designs by Page
	 * @return the list of pathway analysis DTOs.
	 */
	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Transactional
	public Page<PathwayAnalysisDTO> list(@Pagination Pageable pageable) {
		return pathwayService.getPage(pageable).map(pa -> {
			PathwayAnalysisDTO dto = conversionService.convert(pa, PathwayAnalysisDTO.class);
			permissionService.fillWriteAccess(pa, dto);
			permissionService.fillReadAccess(pa, dto);
			return dto;
		});
	}
  

	/**
	 * Check that a pathway analysis name exists.
	 *
	 * This method checks to see if a pathway analysis name exists. The id
	 * parameter is used to 'ignore' an analysis from checking. This is used when
	 * you have an existing analysis which should be ignored when checking if the
	 * name already exists.
	 *
	 * @Summary Check Pathway Analysis Name Name
	 * @param id the pathway analysis id
	 * @param name the name to check
	 * @return a count of the number of pathway analysis designs with the same
	 * name
	 */
	@GET
	@Path("/{id}/exists")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public int getCountPAWithSameName(@PathParam("id") @DefaultValue("0") final int id, @QueryParam("name") String name) {

		return pathwayService.getCountPAWithSameName(id, name);
	}

	/**
	 * Updates a pathway analysis design.
	 *
	 * The modifiedDate and modifiedValues will be populated automatically.
	 *
	 * @summary Update Pathway Analysis Design
	 * @param id the analysis to update
	 * @param dto the pathway analysis design
	 * @return the updated pathway analysis design.
	 */
	@PUT
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Transactional
	public PathwayAnalysisDTO update(@PathParam("id") final Integer id, @RequestBody final PathwayAnalysisDTO dto) {
		pathwayService.saveVersion(id);
		PathwayAnalysisEntity pathwayAnalysis = conversionService.convert(dto, PathwayAnalysisEntity.class);
		pathwayAnalysis.setId(id);
		pathwayService.update(pathwayAnalysis);
		return reloadAndConvert(id);
	}

	/**
	 * Get the pathway analysis design.
	 *
	 * @summary Get Pathway Analysis Design
	 * @param id the design id
	 * @return a pathway analysis design for the given id
	 */
	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Transactional
	public PathwayAnalysisDTO get(@PathParam("id") final Integer id) {
		PathwayAnalysisEntity pathwayAnalysis = pathwayService.getById(id);
		ExceptionUtils.throwNotFoundExceptionIfNull(pathwayAnalysis, String.format(i18nService.translate("pathways.manager.messages.notfound", "There is no pathway analysis with id = %d."), id));
		Map<Integer, Integer> eventCodes = pathwayService.getEventCohortCodes(pathwayAnalysis);

		PathwayAnalysisDTO dto = conversionService.convert(pathwayAnalysis, PathwayAnalysisDTO.class);
		dto.getEventCohorts().forEach(ec -> ec.setCode(eventCodes.get(ec.getId())));

		return dto;
	}

	/**
	 * Export the pathway analysis deign as JSON
	 *
	 * @summary Export Pathway Analysis Design
	 * @param id the design id to export
	 * @return a String containing the pathway analysis design as JSON
	 */
	@GET
	@Path("/{id}/export")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Transactional
	public String export(@PathParam("id") final Integer id) {

		PathwayAnalysisEntity pathwayAnalysis = pathwayService.getById(id);
		ExportUtil.clearCreateAndUpdateInfo(pathwayAnalysis);
		return new SerializedPathwayAnalysisToPathwayAnalysisConverter().convertToDatabaseColumn(pathwayAnalysis);
	}

	/**
	 * Generate pathway analysis sql
	 *
	 * This method generates the analysis sql for the given design id and
	 * specified source. This means that the pathway design must be saved to the
	 * database, and a valid source key is provided as the sourceKey parameter.
	 * The result is a fully translated and populated query containing the schema
	 * parameters and translation based on the specified source.
	 *
	 * @summary Generate Analysis Sql
	 * @param id the pathway analysis design id
	 * @param sourceKey the source used to find the schema and dialect
	 * @return a String containing the analysis sql
	 */
	@GET
	@Path("/{id}/sql/{sourceKey}")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@Consumes(MediaType.APPLICATION_JSON)
	public String getAnalysisSql(@PathParam("id") final Integer id, @PathParam("sourceKey") final String sourceKey) {

		Source source = sourceService.findBySourceKey(sourceKey);
		return pathwayService.buildAnalysisSql(-1L, pathwayService.getById(id), source.getSourceId());
	}

	/**
	 * Delete a pathway analysis design.
	 *
	 * @summary Delete Pathway Analysis Design
	 * @param id
	 */
	@DELETE
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public void delete(@PathParam("id") final Integer id) {

		pathwayService.delete(id);
	}

	/**
	 * Generate pathway analysis.
	 *
	 * This method will execute the analysis sql on the specified source.
	 *
	 * @summary Generate Pathway Analysis
	 * @param pathwayAnalysisId
	 * @param sourceKey
	 * @return a job execution reference
	 */
	@POST
	@Path("/{id}/generation/{sourceKey}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Transactional
	public JobExecutionResource generatePathways(
					@PathParam("id") final Integer pathwayAnalysisId,
					@PathParam("sourceKey") final String sourceKey
	) {

		PathwayAnalysisEntity pathwayAnalysis = pathwayService.getById(pathwayAnalysisId);
		ExceptionUtils.throwNotFoundExceptionIfNull(pathwayAnalysis, String.format("There is no pathway analysis with id = %d.", pathwayAnalysisId));
		PathwayAnalysisDTO pathwayAnalysisDTO = conversionService.convert(pathwayAnalysis, PathwayAnalysisDTO.class);
		CheckResult checkResult = runDiagnostics(pathwayAnalysisDTO);
		if (checkResult.hasCriticalErrors()) {
			throw new RuntimeException("Cannot be generated due to critical errors in design. Call 'check' service for further details");
		}
		Source source = sourceService.findBySourceKey(sourceKey);
		return pathwayService.generatePathways(pathwayAnalysisId, source.getSourceId());
	}

	/**
	 * Cancel analysis execution.
	 *
	 * This method will signal the generation job to cancel on the given source
	 * for the specified design. This cancellation is not immediate: the analysis
	 * sql will stop after the current statement has finished execution.
	 *
	 * @summary Cancel Execution
	 * @param pathwayAnalysisId the pathway analysis id
	 * @param sourceKey the key of the source
	 */
	@DELETE
	@Path("/{id}/generation/{sourceKey}")
	public void cancelPathwaysGeneration(
					@PathParam("id") final Integer pathwayAnalysisId,
					@PathParam("sourceKey") final String sourceKey
	) {

		Source source = sourceService.findBySourceKey(sourceKey);
		pathwayService.cancelGeneration(pathwayAnalysisId, source.getSourceId());
	}

	/**
	 * Returns a list of pathway analysis generation info objects.
	 *
	 * Pathway generation info objects refers to the information related to the
	 * generation on a source. This includes information about the starting time,
	 * duration, and execution status. This method returns the generation
	 * information for any source the user has access to.
	 *
	 * @summary Get pathway generation info
	 * @param pathwayAnalysisId the pathway analysis design id
	 * @return the list of generation info objects for this design
	 */
	@GET
	@Path("/{id}/generation")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Transactional
	public List<CommonGenerationDTO> getPathwayGenerations(
					@PathParam("id") final Integer pathwayAnalysisId
	) {

		Map<String, Source> sourcesMap = sourceService.getSourcesMap(SourceMapKey.BY_SOURCE_KEY);
		return sensitiveInfoService.filterSensitiveInfo(converterUtils.convertList(pathwayService.getPathwayGenerations(pathwayAnalysisId), CommonGenerationDTO.class),
						info -> Collections.singletonMap(Constants.Variables.SOURCE, sourcesMap.get(info.getSourceKey())));
	}

	/**
	 * Return a single generation info for the given generation id.
	 *
	 * @summary Get Generation Info
	 * @param generationId
	 * @return a CommonGenerationDTO for the given generation id
	 */
	@GET
	@Path("/generation/{generationId}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public CommonGenerationDTO getPathwayGenerations(
					@PathParam("generationId") final Long generationId
	) {

		PathwayAnalysisGenerationEntity generationEntity = pathwayService.getGeneration(generationId);
		return sensitiveInfoService.filterSensitiveInfo(conversionService.convert(generationEntity, CommonGenerationDTO.class),
						Collections.singletonMap(Constants.Variables.SOURCE, generationEntity.getSource()));
	}

	/**
	 * Get the pathway analysis design for the given generation id
	 *
	 * When a pathway analysis is generated, a snapshot of the design is stored,
	 * and indexed by the hash of the design. This method allows you to fetch the
	 * pathway design given a generation id.
	 *
	 * @summary Get Design for Generation
	 * @param generationId the generation to fetch the design for
	 * @return a JSON representation of the pathway analysis design.
	 */
	@GET
	@Path("/generation/{generationId}/design")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public String getGenerationDesign(
					@PathParam("generationId") final Long generationId
	) {

		return pathwayService.findDesignByGenerationId(generationId);

	}

	/**
	 * Get the results for the given generation.
	 *
	 * @summary Get Result for Generation
	 * @param generationId the generation id of the results
	 * @return the pathway analysis results
	 */
	@GET
	@Path("/generation/{generationId}/result")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public PathwayPopulationResultsDTO getGenerationResults(
					@PathParam("generationId") final Long generationId
	) {

		PathwayAnalysisResult resultingPathways = pathwayService.getResultingPathways(generationId);

		List<PathwayCodeDTO> eventCodeDtos = resultingPathways.getCodes()
						.stream()
						.map(entry -> {
							PathwayCodeDTO dto = new PathwayCodeDTO();
							dto.setCode(entry.getCode());
							dto.setName(entry.getName());
							dto.setIsCombo(entry.isCombo());
							return dto;
						})
						.collect(Collectors.toList());

		List<TargetCohortPathwaysDTO> pathwayDtos = resultingPathways.getCohortPathwaysList()
						.stream()
						.map(cohortResults -> {
							if (cohortResults.getPathwaysCounts() == null) {
								return null;
							}

							List<PathwayPopulationEventDTO> eventDTOs = cohortResults.getPathwaysCounts()
											.entrySet()
											.stream()
											.map(entry -> new PathwayPopulationEventDTO(entry.getKey(), entry.getValue()))
											.collect(Collectors.toList());
							return new TargetCohortPathwaysDTO(cohortResults.getCohortId(), cohortResults.getTargetCohortCount(), cohortResults.getTotalPathwaysCount(), eventDTOs);
						})
						.filter(Objects::nonNull)
						.collect(Collectors.toList());

		return new PathwayPopulationResultsDTO(eventCodeDtos, pathwayDtos);
	}

	private PathwayAnalysisDTO reloadAndConvert(Integer id) {
		// Before conversion entity must be refreshed to apply entity graphs
		PathwayAnalysisEntity analysis = pathwayService.getById(id);
		return conversionService.convert(analysis, PathwayAnalysisDTO.class);
	}

	/**
	 * Checks the pathway analysis for logic issues
	 *
	 * This method runs a series of logical checks on a pathway analysis and
	 * returns the set of warning, info and error messages.
	 *
	 * @summary Check Pathway Analysis Design
	 * @param pathwayAnalysisDTO the pathway analysis design to check
	 * @return the set of checks (warnings, info and errors)
	 */
	@POST
	@Path("/check")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public CheckResult runDiagnostics(PathwayAnalysisDTO pathwayAnalysisDTO) {

		return new CheckResult(checker.check(pathwayAnalysisDTO));
	}

	/**
	 * Assign tag to Pathway Analysis
	 *
	 * @summary Assign Tag
	 * @param id
	 * @param tagId
	 */
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{id}/tag/")
	public void assignTag(@PathParam("id") final int id, final int tagId) {
		pathwayService.assignTag(id, tagId);
	}

	/**
	 * Unassign tag from Pathway Analysis
	 *
	 * @summary Unassign Tag
	 * @param id
	 * @param tagId
	 */
	@DELETE
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{id}/tag/{tagId}")
	public void unassignTag(@PathParam("id") final int id, @PathParam("tagId") final int tagId) {
		pathwayService.unassignTag(id, tagId);
	}

	/**
	 * Assign protected tag to Pathway Analysis
	 *
	 * @summary Assign Protected Tag
	 * @param id
	 * @param tagId
	 */
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{id}/protectedtag/")
	public void assignPermissionProtectedTag(@PathParam("id") final int id, final int tagId) {
		pathwayService.assignTag(id, tagId);
	}

	/**
	 * Unassign protected tag from Pathway Analysis
	 *
	 * @summary Unassign Protected Tag
	 * @param id
	 * @param tagId
	 */
	@DELETE
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{id}/protectedtag/{tagId}")
	public void unassignPermissionProtectedTag(@PathParam("id") final int id, @PathParam("tagId") final int tagId) {
		pathwayService.unassignTag(id, tagId);
	}

	/**
	 * Get list of versions of Pathway Analysis
	 *
	 * @summary Get Versions
	 * @param id
	 * @return
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{id}/version/")
	public List<VersionDTO> getVersions(@PathParam("id") final long id) {
		return pathwayService.getVersions(id);
	}

	/**
	 * Get specific version of Pathway Analysis
	 *
	 * @summary Get Version
	 * @param id
	 * @param version
	 * @return
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{id}/version/{version}")
	public PathwayVersionFullDTO getVersion(@PathParam("id") final int id, @PathParam("version") final int version) {
		return pathwayService.getVersion(id, version);
	}

	/**
	 * Update version of Pathway Analysis
	 *
	 * @summary Update Version
	 * @param id
	 * @param version
	 * @param updateDTO
	 * @return
	 */
	@PUT
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{id}/version/{version}")
	public VersionDTO updateVersion(@PathParam("id") final int id, @PathParam("version") final int version,
					VersionUpdateDTO updateDTO) {
		return pathwayService.updateVersion(id, version, updateDTO);
	}

	/**
	 * Delete version of Pathway Analysis
	 *
	 * @summary Delete Version
	 * @param id
	 * @param version
	 */
	@DELETE
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{id}/version/{version}")
	public void deleteVersion(@PathParam("id") final int id, @PathParam("version") final int version) {
		pathwayService.deleteVersion(id, version);
	}

	/**
	 * Create a new asset form version of Pathway Analysis
	 *
	 * @Create Asset From Version
	 * @param id
	 * @param version
	 * @return
	 */
	@PUT
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{id}/version/{version}/createAsset")
	public PathwayAnalysisDTO copyAssetFromVersion(@PathParam("id") final int id, @PathParam("version") final int version) {
		return pathwayService.copyAssetFromVersion(id, version);
	}

	/**
	 * Get list of pathways with assigned tags
	 *
	 * @summary List By Tags
	 * @param requestDTO
	 * @return
	 */
	@POST
	@Path("/byTags")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public List<PathwayAnalysisDTO> listByTags(TagNameListRequestDTO requestDTO) {
		if (requestDTO == null || requestDTO.getNames() == null || requestDTO.getNames().isEmpty()) {
			return Collections.emptyList();
		}
		return pathwayService.listByTags(requestDTO);
	}
}
