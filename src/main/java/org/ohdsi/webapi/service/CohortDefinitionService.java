/*
 * Copyright 2015 Observational Health Data Sciences and Informatics [OHDSI.org].
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ohdsi.webapi.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.commonmark.Extension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.node.*;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.ohdsi.analysis.Utils;
import org.ohdsi.circe.check.Checker;
import org.ohdsi.circe.cohortdefinition.CohortExpression;
import org.ohdsi.circe.cohortdefinition.CohortExpressionQueryBuilder;
import org.ohdsi.circe.cohortdefinition.ConceptSet;
import org.ohdsi.circe.cohortdefinition.printfriendly.MarkdownRender;
import org.ohdsi.sql.SqlRender;
import org.ohdsi.webapi.Constants;
import org.ohdsi.webapi.check.CheckResult;
import org.ohdsi.webapi.check.checker.cohort.CohortChecker;
import org.ohdsi.webapi.check.warning.Warning;
import org.ohdsi.webapi.check.warning.WarningUtils;
import org.ohdsi.webapi.cohortdefinition.CleanupCohortTasklet;
import org.ohdsi.webapi.cohortdefinition.CohortDefinition;
import org.ohdsi.webapi.cohortdefinition.CohortDefinitionDetails;
import org.ohdsi.webapi.cohortdefinition.CohortDefinitionRepository;
import org.ohdsi.webapi.cohortdefinition.CohortGenerationInfo;
import org.ohdsi.webapi.cohortdefinition.InclusionRuleReport;
import org.ohdsi.webapi.cohortdefinition.dto.CohortDTO;
import org.ohdsi.webapi.cohortdefinition.dto.CohortGenerationInfoDTO;
import org.ohdsi.webapi.cohortdefinition.dto.CohortMetadataDTO;
import org.ohdsi.webapi.cohortdefinition.dto.CohortMetadataImplDTO;
import org.ohdsi.webapi.cohortdefinition.dto.CohortVersionFullDTO;
import org.ohdsi.webapi.cohortsample.CleanupCohortSamplesTasklet;
import org.ohdsi.webapi.cohortsample.CohortSamplingService;
import org.ohdsi.webapi.cohortdefinition.dto.CohortRawDTO;
import org.ohdsi.webapi.cohortdefinition.event.CohortDefinitionChangedEvent;
import org.ohdsi.webapi.common.SourceMapKey;
import org.ohdsi.webapi.common.generation.GenerateSqlResult;
import org.ohdsi.webapi.common.sensitiveinfo.CohortGenerationSensitiveInfoService;
import org.ohdsi.webapi.conceptset.ConceptSetExport;
import org.ohdsi.webapi.job.JobExecutionResource;
import org.ohdsi.webapi.job.JobTemplate;
import org.ohdsi.webapi.security.PermissionService;
import org.ohdsi.webapi.service.dto.CheckResultDTO;
import org.ohdsi.webapi.shiro.Entities.UserEntity;
import org.ohdsi.webapi.shiro.management.datasource.SourceIdAccessor;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceDaimon;
import org.ohdsi.webapi.source.SourceInfo;
import org.ohdsi.webapi.tag.domain.HasTags;
import org.ohdsi.webapi.tag.dto.TagNameListRequestDTO;
import org.ohdsi.webapi.util.*;
import org.ohdsi.webapi.util.ExceptionUtils;
import org.ohdsi.webapi.util.NameUtils;
import org.ohdsi.webapi.util.PreparedStatementRenderer;
import org.ohdsi.webapi.util.SessionUtils;
import org.ohdsi.webapi.versioning.domain.CohortVersion;
import org.ohdsi.webapi.versioning.domain.Version;
import org.ohdsi.webapi.versioning.domain.VersionBase;
import org.ohdsi.webapi.versioning.domain.VersionType;
import org.ohdsi.webapi.versioning.dto.VersionDTO;
import org.ohdsi.webapi.versioning.dto.VersionUpdateDTO;
import org.ohdsi.webapi.versioning.service.VersionService;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.builder.SimpleJobBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.convert.ConversionService;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.ServletContext;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.ws.rs.core.Response.ResponseBuilder;

import static org.ohdsi.webapi.Constants.Params.COHORT_DEFINITION_ID;
import static org.ohdsi.webapi.Constants.Params.JOB_NAME;
import static org.ohdsi.webapi.Constants.Params.SOURCE_ID;
import org.ohdsi.webapi.source.SourceService;
import static org.ohdsi.webapi.util.SecurityUtils.whitelist;

/**
 * Provides REST services for working with cohort definitions.
 *
 * @summary Provides REST services for working with cohort definitions.
 * @author cknoll1
 */
@Path("/cohortdefinition")
@Component
public class CohortDefinitionService extends AbstractDaoService implements HasTags<Integer> {

	private static final CohortExpressionQueryBuilder queryBuilder = new CohortExpressionQueryBuilder();

	@Autowired
	private CohortDefinitionRepository cohortDefinitionRepository;

	@Autowired
	private JobBuilderFactory jobBuilders;

	@Autowired
	private StepBuilderFactory stepBuilders;

	@Autowired
	private JobTemplate jobTemplate;

	@Autowired
	private CohortGenerationService cohortGenerationService;

	@Autowired
	private JobService jobService;

	@Autowired
	private CohortGenerationSensitiveInfoService sensitiveInfoService;

	@Autowired
	private SourceIdAccessor sourceIdAccessor;

	@Autowired
	ConversionService conversionService;

	@Autowired
	private ObjectMapper objectMapper;
  
	@Autowired
	private CohortSamplingService samplingService;

	@Autowired
	private ApplicationEventPublisher eventPublisher;

	@Autowired
	private SourceService sourceService;

	@Autowired
	private VocabularyService vocabularyService;

	@Autowired
	private PermissionService permissionService;

	@PersistenceContext
	protected EntityManager entityManager;

	@Autowired
	private CohortChecker cohortChecker;

	@Autowired
	private VersionService<CohortVersion> versionService;

        @Value("${security.defaultGlobalReadPermissions}")
	private boolean defaultGlobalReadPermissions;

	private final MarkdownRender markdownPF = new MarkdownRender();

	private final List<Extension> extensions = Arrays.asList(TablesExtension.create());

	private final RowMapper<InclusionRuleReport.Summary> summaryMapper = (rs, rowNum) -> {
		InclusionRuleReport.Summary summary = new InclusionRuleReport.Summary();
		summary.baseCount = rs.getLong("base_count");
		summary.finalCount = rs.getLong("final_count");
		summary.lostCount = rs.getLong("lost_count");

		double matchRatio = (summary.baseCount > 0) ? ((double) summary.finalCount / (double) summary.baseCount) : 0.0;
		summary.percentMatched = new BigDecimal(matchRatio * 100.0).setScale(2, RoundingMode.HALF_UP).toPlainString() + "%";
		return summary;
	};

	private final RowMapper<InclusionRuleReport.InclusionRuleStatistic> inclusionRuleStatisticMapper = new RowMapper<InclusionRuleReport.InclusionRuleStatistic>() {

		@Override
		public InclusionRuleReport.InclusionRuleStatistic mapRow(ResultSet rs, int rowNum) throws SQLException {
			InclusionRuleReport.InclusionRuleStatistic statistic = new InclusionRuleReport.InclusionRuleStatistic();
			statistic.id = rs.getInt("rule_sequence");
			statistic.name = rs.getString("name");
			statistic.countSatisfying = rs.getLong("person_count");
			long personTotal = rs.getLong("person_total");

			long gainCount = rs.getLong("gain_count");
			double excludeRatio = personTotal > 0 ? (double) gainCount / (double) personTotal : 0.0;
			String percentExcluded = new BigDecimal(excludeRatio * 100.0).setScale(2, RoundingMode.HALF_UP).toPlainString();
			statistic.percentExcluded = percentExcluded + "%";

			long satisfyCount = rs.getLong("person_count");
			double satisfyRatio = personTotal > 0 ? (double) satisfyCount / (double) personTotal : 0.0;
			String percentSatisfying = new BigDecimal(satisfyRatio * 100.0).setScale(2, RoundingMode.HALF_UP).toPlainString();
			statistic.percentSatisfying = percentSatisfying + "%";
			return statistic;
		}
	};

	private final RowMapper<Long[]> inclusionRuleResultItemMapper = new RowMapper<Long[]>() {

		@Override
		public Long[] mapRow(ResultSet rs, int rowNum) throws SQLException {
			Long[] resultItem = new Long[2];
			resultItem[0] = rs.getLong("inclusion_rule_mask");
			resultItem[1] = rs.getLong("person_count");
			return resultItem;
		}
	};

	private CohortGenerationInfo findBySourceId(Set<CohortGenerationInfo> infoList, Integer sourceId) {
		for (CohortGenerationInfo info : infoList) {
			if (info.getId().getSourceId().equals(sourceId)) {
				return info;
			}
		}
		return null;
	}

	private InclusionRuleReport.Summary getInclusionRuleReportSummary(int id, Source source, int modeId) {

		String sql = "select cs.base_count, cs.final_count, cc.lost_count from @tableQualifier.cohort_summary_stats cs left join @tableQualifier.cohort_censor_stats cc "
						+ "on cc.cohort_definition_id = cs.cohort_definition_id where cs.cohort_definition_id = @id and cs.mode_id = @modeId";
		String tqName = "tableQualifier";
		String tqValue = source.getTableQualifier(SourceDaimon.DaimonType.Results);
		String[] varNames = {"id", "modeId"};
		Object[] varValues = {whitelist(id), whitelist(modeId)};
		PreparedStatementRenderer psr = new PreparedStatementRenderer(source, sql, tqName, tqValue, varNames, varValues, SessionUtils.sessionId());
		List<InclusionRuleReport.Summary> result = getSourceJdbcTemplate(source).query(psr.getSql(), psr.getSetter(), summaryMapper);
		return result.isEmpty() ? new InclusionRuleReport.Summary() : result.get(0);
	}

	private List<InclusionRuleReport.InclusionRuleStatistic> getInclusionRuleStatistics(int id, Source source, int modeId) {

		String sql = "select i.rule_sequence, i.name, s.person_count, s.gain_count, s.person_total"
						+ " from @tableQualifier.cohort_inclusion i join @tableQualifier.cohort_inclusion_stats s on i.cohort_definition_id = s.cohort_definition_id"
						+ " and i.rule_sequence = s.rule_sequence"
						+ " where i.cohort_definition_id = @id and mode_id = @modeId ORDER BY i.rule_sequence";
		String tqName = "tableQualifier";
		String tqValue = source.getTableQualifier(SourceDaimon.DaimonType.Results);
		String[] varNames = {"id", "modeId"};
		Object[] varValues = {whitelist(id), whitelist(modeId)};
		PreparedStatementRenderer psr = new PreparedStatementRenderer(source, sql, tqName, tqValue, varNames, varValues, SessionUtils.sessionId());
		return getSourceJdbcTemplate(source).query(psr.getSql(), psr.getSetter(), inclusionRuleStatisticMapper);
	}

	private int countSetBits(long n) {
		int count = 0;
		while (n > 0) {
			n &= (n - 1);
			count++;
		}
		return count;
	}

	private String formatBitMask(Long n, int size) {
		return StringUtils.reverse(StringUtils.leftPad(Long.toBinaryString(n), size, "0"));
	}

	private String getInclusionRuleTreemapData(int id, int inclusionRuleCount, Source source, int modeId) {

		String sql = "select inclusion_rule_mask, person_count from @tableQualifier.cohort_inclusion_result where cohort_definition_id = @id and mode_id = @modeId";
		String tqName = "tableQualifier";
		String tqValue = source.getTableQualifier(SourceDaimon.DaimonType.Results);
		String[] varNames = {"id", "modeId"};
		Object[] varValues = {whitelist(id), whitelist(modeId)};
		PreparedStatementRenderer psr = new PreparedStatementRenderer(source, sql, tqName, tqValue, varNames, varValues, SessionUtils.sessionId());

		// [0] is the inclusion rule bitmask, [1] is the count of the match
		List<Long[]> items = this.getSourceJdbcTemplate(source).query(psr.getSql(), psr.getSetter(), inclusionRuleResultItemMapper);
		Map<Integer, List<Long[]>> groups = new HashMap<>();
		for (Long[] item : items) {
			int bitsSet = countSetBits(item[0]);
			if (!groups.containsKey(bitsSet)) {
				groups.put(bitsSet, new ArrayList<Long[]>());
			}
			groups.get(bitsSet).add(item);
		}

		StringBuilder treemapData = new StringBuilder("{\"name\" : \"Everyone\", \"children\" : [");

		List<Integer> groupKeys = new ArrayList<>(groups.keySet());
		Collections.sort(groupKeys);
		Collections.reverse(groupKeys);

		int groupCount = 0;
		// create a nested treemap data where more matches (more bits set in string) appear higher in the hierarchy)
		for (Integer groupKey : groupKeys) {
			if (groupCount > 0) {
				treemapData.append(",");
			}

			treemapData.append(String.format("{\"name\" : \"Group %d\", \"children\" : [", groupKey));

			int groupItemCount = 0;
			for (Long[] groupItem : groups.get(groupKey)) {
				if (groupItemCount > 0) {
					treemapData.append(",");
				}

				//sb_treemap.Append("{\"name\": \"" + cohort_identifer + "\", \"size\": " + cohorts[cohort_identifer].ToString() + "}");
				treemapData.append(String.format("{\"name\": \"%s\", \"size\": %d}", formatBitMask(groupItem[0], inclusionRuleCount), groupItem[1]));
				groupItemCount++;
			}
			groupCount++;
		}

		treemapData.append(StringUtils.repeat("]}", groupCount + 1));

		return treemapData.toString();
	}

	public static class GenerateSqlRequest {

		public GenerateSqlRequest() {
		}

		@JsonProperty("expression")
		public CohortExpression expression;

		@JsonProperty("options")
		public CohortExpressionQueryBuilder.BuildExpressionQueryOptions options;

	}
	@Context
	ServletContext context;

	/**
	 * Returns OHDSI template SQL for a given cohort definition 
	 *
	 * @summary Generate Sql
	 * @param request A GenerateSqlRequest containing the cohort expression and options.
	 * @return The OHDSI template SQL needed to generate the input cohort definition as a character string
	 */
	@Path("sql")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public GenerateSqlResult generateSql(GenerateSqlRequest request) {
		CohortExpressionQueryBuilder.BuildExpressionQueryOptions options = request.options;
		GenerateSqlResult result = new GenerateSqlResult();
		if (options == null) {
			options = new CohortExpressionQueryBuilder.BuildExpressionQueryOptions();
		}
		String expressionSql = queryBuilder.buildExpressionQuery(request.expression, options);
		result.templateSql = SqlRender.renderSql(expressionSql, null, null);

		return result;
	}

	/**
	 * Returns metadata about all cohort definitions in the WebAPI database
	 *
	 * @summary List Cohort Definitions
	 * @return List of metadata about all cohort definitions in WebAPI
	 * @see org.ohdsi.webapi.cohortdefinition.CohortMetadataDTO
	 */
	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	@Transactional
	public List<CohortMetadataDTO> getCohortDefinitionList() {
		List<CohortDefinition> definitions = cohortDefinitionRepository.list();
		return definitions.stream()
						.filter(!defaultGlobalReadPermissions ? entity -> permissionService.hasReadAccess(entity) : entity -> true)
						.map(def -> {
							CohortMetadataDTO dto = conversionService.convert(def, CohortMetadataImplDTO.class);
							permissionService.fillWriteAccess(def, dto);
							permissionService.fillReadAccess(def, dto);
							return dto;
						})
						.collect(Collectors.toList());
	}

	/**
	 * Creates a cohort definition in the WebAPI database.
	 * 
	 * The values for createdBy and createdDate are automatically populated and
	 * the function ignores parameter values for these fields.
	 *
	 * @summary Create Cohort Definition
	 * @param dto The cohort definition to create.
	 * @return The newly created cohort definition
	 */
	@POST
	@Path("/")
	@Transactional
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public CohortDTO createCohortDefinition(CohortDTO dto) {

		Date currentTime = Calendar.getInstance().getTime();

		UserEntity user = userRepository.findByLogin(security.getSubject());
		//create definition in 2 saves, first to get the generated ID for the new dto
		// then to associate the details with the definition
		CohortDefinition newDef = new CohortDefinition();
		newDef.setName(StringUtils.trim(dto.getName()))
						.setDescription(dto.getDescription())
						.setExpressionType(dto.getExpressionType());
		newDef.setCreatedBy(user);
		newDef.setCreatedDate(currentTime);

		newDef = this.cohortDefinitionRepository.save(newDef);

		// associate details
		CohortDefinitionDetails details = new CohortDefinitionDetails();
		details.setCohortDefinition(newDef)
						.setExpression(Utils.serialize(dto.getExpression()));

		newDef.setDetails(details);

		CohortDefinition createdDefinition = this.cohortDefinitionRepository.save(newDef);
		return conversionService.convert(createdDefinition, CohortDTO.class);
	}

	/**
	 * Returns the 'raw' cohort definition for the given id.
	 *
	 * 'Raw' means that the cohort expression is returned as a string, and not as
	 * a concrete CohortExpression class. This method is maintained for legacy
	 * comparability.
	 *
	 *
	 * @summary Get Raw Cohort Definition
	 * @param id The cohort definition id
	 * @return The cohort definition JSON expression
	 */
	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public CohortRawDTO getCohortDefinitionRaw(@PathParam("id") final int id) {
		return getTransactionTemplate().execute(transactionStatus -> {
			CohortDefinition d = this.cohortDefinitionRepository.findOneWithDetail(id);
			ExceptionUtils.throwNotFoundExceptionIfNull(d, String.format("There is no cohort definition with id = %d.", id));
			return conversionService.convert(d, CohortRawDTO.class);
		});
	}

	/**
	 * Returns the cohort definition containing the circe cohort expression
	 *
	 * @Summary Get Cohort Definition
	 * @param id The cohort definition id
	 * @return The cohort definition containing a Circe CohortExpression object.
	 */
	public CohortDTO getCohortDefinition(final int id) {
		return getTransactionTemplate().execute(transactionStatus -> {
			CohortDefinition d = this.cohortDefinitionRepository.findOneWithDetail(id);
			ExceptionUtils.throwNotFoundExceptionIfNull(d, String.format("There is no cohort definition with id = %d.", id));
			return conversionService.convert(d, CohortDTO.class);
		});
	}

	/**
	 * Check that a cohort exists.
	 *
	 * This method checks to see if a cohort definition name exists. The id
	 * parameter is used to 'ignore' a cohort definition from checking. This is
	 * used when you have an existing cohort definition which should be ignored
	 * when checking if the name already exists.
	 *
	 * @Summary Check Cohort Definition Name
	 * @param id The cohort definition id
	 * @param name The cohort definition name
	 * @return 1 if the a cohort with the given name and id exist in WebAPI and 0
	 * otherwise
	 */
	@GET
	@Path("/{id}/exists")
	@Produces(MediaType.APPLICATION_JSON)
	public int getCountCDefWithSameName(@PathParam("id") @DefaultValue("0") final int id, @QueryParam("name") String name) {

		return cohortDefinitionRepository.getCountCDefWithSameName(id, name);
	}

	/**
	 * Saves the cohort definition for the given id.
	 *
	 * The modifiedBy and modifiedDate are set automatically, and those values
	 * submitted in CohortDTO will be ignored. * @summary Save Cohort Definition
	 *
	 * @summary Save Cohort Definition
	 * @param id The cohort definition id
	 * @return The updated CohortDefinition
	 */
	@PUT
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Transactional
	public CohortDTO saveCohortDefinition(@PathParam("id") final int id, CohortDTO def) {
		Date currentTime = Calendar.getInstance().getTime();

		saveVersion(id);

		CohortDefinition currentDefinition = this.cohortDefinitionRepository.findOneWithDetail(id);
		UserEntity modifier = userRepository.findByLogin(security.getSubject());

		currentDefinition.setName(def.getName())
				.setDescription(def.getDescription())
				.setExpressionType(def.getExpressionType())
				.getDetails().setExpression(Utils.serialize(def.getExpression()));
		currentDefinition.setModifiedBy(modifier);
		currentDefinition.setModifiedDate(currentTime);

		currentDefinition = this.cohortDefinitionRepository.save(currentDefinition);
		eventPublisher.publishEvent(new CohortDefinitionChangedEvent(currentDefinition));

		return getCohortDefinition(id);
	}

	/**
	 * Queues up a generate cohort task for the specified cohort definition id.
	 *
	 * @param id	the Cohort Definition ID to generate
	 * @param sourceKey	The source to execute the cohort generation
	 * @return	the job info for the cohort generation
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{id}/generate/{sourceKey}")
	@Transactional
	public JobExecutionResource generateCohort(@PathParam("id") final int id, @PathParam("sourceKey") final String sourceKey) {

		Source source = getSourceRepository().findBySourceKey(sourceKey);
		CohortDefinition currentDefinition = this.cohortDefinitionRepository.findOne(id);
		UserEntity user = userRepository.findByLogin(security.getSubject());
		return cohortGenerationService.generateCohortViaJob(user, currentDefinition, source);
	}

	/**
	 * Cancel a cohort generation task
	 *
	 * This method updates the generation info to 'Canceled' and invalidates the
	 * job execution on the server.
	 *
	 * Note: invalidating the job is performed by indicating that the job
	 * execution should stop at the next SQL step in the analysis query. This
	 * means that the execution will not physically cancel until the current step
	 * in the SQL query completes.
	 *
	 * @summary Cancel Cohort Generation.
	 * @param id the id of the cohort definition being generated
	 * @param sourceKey the sourceKey for the target database for generation
	 * @return
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{id}/cancel/{sourceKey}")
	public Response cancelGenerateCohort(@PathParam("id") final int id, @PathParam("sourceKey") final String sourceKey) {

		final Source source = Optional.ofNullable(getSourceRepository().findBySourceKey(sourceKey))
						.orElseThrow(NotFoundException::new);
		getTransactionTemplateRequiresNew().execute(status -> {
			CohortDefinition currentDefinition = cohortDefinitionRepository.findOne(id);
			if (Objects.nonNull(currentDefinition)) {
				CohortGenerationInfo info = findBySourceId(currentDefinition.getGenerationInfoList(), source.getSourceId());
				if (Objects.nonNull(info)) {
					invalidateExecution(info);
					cohortDefinitionRepository.save(currentDefinition);
				}
			}
			return null;
		});

		jobService.cancelJobExecution(e -> {
			JobParameters parameters = e.getJobParameters();
			String jobName = e.getJobInstance().getJobName();
			return Objects.equals(parameters.getString(COHORT_DEFINITION_ID), Integer.toString(id))
							&& Objects.equals(parameters.getString(SOURCE_ID), Integer.toString(source.getSourceId()))
							&& Objects.equals(Constants.GENERATE_COHORT, jobName);
		});
		return Response.status(Response.Status.OK).build();
	}

	/**
	 * Returns a list of cohort generation info objects.
	 *
	 * Cohort generation info objects refers to the information related to the
	 * generation on a source. This includes information about the starting time,
	 * duration, and execution status. This method returns the generation
	 * information for any source the user has access to.
	 *
	 * @summary Get cohort generation info
	 * @param id - the Cohort Definition ID to generate
	 * @return information about the Cohort Analysis Job for each source
	 * @throws NotFoundException
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{id}/info")
	@Transactional
	public List<CohortGenerationInfoDTO> getInfo(@PathParam("id") final int id) {
		CohortDefinition def = this.cohortDefinitionRepository.findOne(id);
		ExceptionUtils.throwNotFoundExceptionIfNull(def, String.format("There is no cohort definition with id = %d.", id));

		Set<CohortGenerationInfo> infoList = def.getGenerationInfoList();

		List<CohortGenerationInfo> result = infoList.stream().filter(genInfo -> sourceIdAccessor.hasAccess(genInfo.getId().getSourceId())).collect(Collectors.toList());

		Map<Integer, Source> sourceMap = sourceService.getSourcesMap(SourceMapKey.BY_SOURCE_ID);
		List<CohortGenerationInfo> filteredResult = sensitiveInfoService.filterSensitiveInfo(result,
						gi -> Collections.singletonMap(Constants.Variables.SOURCE, sourceMap.get(gi.getId().getSourceId())));
		return filteredResult.stream()
						.map(t -> conversionService.convert(t, CohortGenerationInfoDTO.class))
						.collect(Collectors.toList());
	}

	/**
	 * Copies the specified cohort definition.
	 *
	 * This method takes a cohort definition id, and creates a copy. This copy has
	 * no tags and the owner is set to the user who made the copy.
	 *
	 * @summary Copy Cohort Definition
	 * @param id - the Cohort Definition ID to copy
	 * @return the copied cohort definition as a CohortDTO
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{id}/copy")
	@Transactional
	public CohortDTO copy(@PathParam("id") final int id) {
		CohortDTO sourceDef = getCohortDefinition(id);
		sourceDef.setId(null); // clear the ID
		sourceDef.setTags(null);
		sourceDef.setName(NameUtils.getNameForCopy(sourceDef.getName(), this::getNamesLike, cohortDefinitionRepository.findByName(sourceDef.getName())));
		CohortDTO copyDef = createCohortDefinition(sourceDef);

		return copyDef;
	}

	public List<String> getNamesLike(String copyName) {

		return cohortDefinitionRepository.findAllByNameStartsWith(copyName).stream().map(CohortDefinition::getName).collect(Collectors.toList());
	}

	/**
	 * Deletes the specified cohort definition
	 *
	 * When a cohort definition is deleted, any generation job is canceled, and
	 * any generated sample is removed from the sources.
	 *
	 * @summary Delete Cohort Definition
	 * @param id - the Cohort Definition ID to delete
	 */
	@DELETE
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{id}")
	public void delete(@PathParam("id") final int id) {
		// perform the JPA update in a separate transaction
		this.getTransactionTemplateRequiresNew().execute(new TransactionCallbackWithoutResult() {
			@Override
			public void doInTransactionWithoutResult(final TransactionStatus status) {
				CohortDefinition def = cohortDefinitionRepository.findOne(id);
				if (!Objects.isNull(def)) {
					def.getGenerationInfoList().forEach(cohortGenerationInfo -> {
						Integer sourceId = cohortGenerationInfo.getId().getSourceId();

						jobService.cancelJobExecution(e -> {
							JobParameters parameters = e.getJobParameters();
							String jobName = e.getJobInstance().getJobName();
							return Objects.equals(parameters.getString(COHORT_DEFINITION_ID), Integer.toString(id))
											&& Objects.equals(parameters.getString(SOURCE_ID), Integer.toString(sourceId))
											&& Objects.equals(Constants.GENERATE_COHORT, jobName);
						});
					});
					cohortDefinitionRepository.delete(def);
					samplingService.launchDeleteSamplesTasklet(id);
				} else {
					log.warn("Failed to delete Cohort Definition with ID = {}", id);
				}
			}
		});

		JobParametersBuilder builder = new JobParametersBuilder();
		builder.addString(JOB_NAME, String.format("Cleanup cohort %d.", id));
		builder.addString(COHORT_DEFINITION_ID, ("" + id));

		final JobParameters jobParameters = builder.toJobParameters();

		log.info("Beginning cohort cleanup for cohort definition id: {}", "" + id);

		CleanupCohortTasklet cleanupTasklet = new CleanupCohortTasklet(this.getTransactionTemplateNoTransaction(), this.getSourceRepository());

		Step cleanupStep = stepBuilders.get("cohortDefinition.cleanupCohort")
						.tasklet(cleanupTasklet)
						.build();

		CleanupCohortSamplesTasklet cleanupSamplesTasklet = samplingService.createDeleteSamplesTasklet();

		Step cleanupSamplesStep = stepBuilders.get("cohortDefinition.cleanupSamples")
						.tasklet(cleanupSamplesTasklet)
						.build();

		SimpleJobBuilder cleanupJobBuilder = jobBuilders.get("cleanupCohort")
						.start(cleanupStep)
						.next(cleanupSamplesStep);

		Job cleanupCohortJob = cleanupJobBuilder.build();

		this.jobTemplate.launch(cleanupCohortJob, jobParameters);
	}

	private List<ConceptSetExport> getConceptSetExports(CohortDefinition def, SourceInfo vocabSource) throws RuntimeException {

		CohortExpression expression;
		try {
			expression = objectMapper.readValue(def.getDetails().getExpression(), CohortExpression.class);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return Arrays.stream(expression.conceptSets)
						.map(cs -> vocabularyService.exportConceptSet(cs, vocabSource))
						.collect(Collectors.toList());
	}

	/**
	 * Return concept sets used in a cohort definition as a zip file.
	 *
	 * This method extracts the concept set from the specified cohort definition
	 * and serializes the elements as a CSV and zips the results into a file with
	 * from 'cohortdefinition_{id}_export.zip".
	 *
	 * @summary Export Concept Sets as ZIP
	 * @param id a cohort definition id
	 * @return a binary stream containing the zip file with concept sets.
	 */
	@GET
	@Path("/{id}/export/conceptset")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response exportConceptSets(@PathParam("id") final int id) {

		Source source = sourceService.getPriorityVocabularySource();
		if (Objects.isNull(source)) {
			throw new ForbiddenException();
		}
		CohortDefinition def = this.cohortDefinitionRepository.findOneWithDetail(id);
		if (Objects.isNull(def)) {
			throw new NotFoundException();
		}

		List<ConceptSetExport> exports = getConceptSetExports(def, new SourceInfo(source));
		ByteArrayOutputStream exportStream = ExportUtil.writeConceptSetExportToCSVAndZip(exports);

		return HttpUtils.respondBinary(exportStream, String.format("cohortdefinition_%d_export.zip", def.getId()));
	}

	/**
	 * Get the Inclusion Rule report for the specified source and mode
	 *
	 * The mode refers to the results for either 'all events' (0) or 'best event'
	 * (1). The best event report limits the selected entry event to
	 * one-per-person, and therefore this result can be considered a 'by person'
	 * report.
	 *
	 * @summary Get Inclusion Rule Report
	 * @param id a cohort definition id
	 * @param sourceKey the source to fetch results from
	 * @param modeId the mode of the report: 0 = all events, 1 = best event
	 * @return a binary stream containing the zip file with concept sets.
	 */
	@GET
	@Path("/{id}/report/{sourceKey}")
	@Produces(MediaType.APPLICATION_JSON)
	@Transactional
	public InclusionRuleReport getInclusionRuleReport(
					@PathParam("id") final int id,
					@PathParam("sourceKey") final String sourceKey,
					@DefaultValue("0") @QueryParam("mode") int modeId) {

		Source source = this.getSourceRepository().findBySourceKey(sourceKey);

		InclusionRuleReport.Summary summary = getInclusionRuleReportSummary(whitelist(id), source, modeId);
		List<InclusionRuleReport.InclusionRuleStatistic> inclusionRuleStats = getInclusionRuleStatistics(whitelist(id), source, modeId);
		String treemapData = getInclusionRuleTreemapData(whitelist(id), inclusionRuleStats.size(), source, modeId);

		InclusionRuleReport report = new InclusionRuleReport();
		report.summary = summary;
		report.inclusionRuleStats = inclusionRuleStats;
		report.treemapData = treemapData;

		return report;
	}

	/**
	 * Checks the cohort definition for logic issues
	 * 
	 * This method runs a series of logical checks on a cohort definition and returns the set of warning, info and error messages.
	 *
	 * @summary Check Cohort Definition
	 * @param expression The cohort definition expression
	 * @return The cohort check result
	 */
	@POST
	@Path("/check")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Transactional
	public CheckResultDTO runDiagnostics(CohortExpression expression) {
		Checker checker = new Checker();
		return new CheckResultDTO(checker.check(expression));
	}
	
	/**
	 * Checks the cohort definition for logic issues
	 * 
	 * This method runs a series of logical checks on a cohort definition and returns the set of warning, info and error messages.
	 * 
	 * This method is similar to /check except this method accepts a ChortDTO which includes tags.
	 *
	 * @summary Check Cohort Definition
	 * @param cohortDTO The cohort definition expression
	 * @return The cohort check result
	 */
	@POST
	@Path("/checkV2")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Transactional
	public CheckResult runDiagnosticsWithTags(CohortDTO cohortDTO) {
		Checker checker = new Checker();
		CheckResultDTO checkResultDTO = new CheckResultDTO(checker.check(cohortDTO.getExpression()));
		List<Warning> circeWarnings = checkResultDTO.getWarnings().stream()
				.map(WarningUtils::convertCirceWarning)
				.collect(Collectors.toList());
		CheckResult checkResult = new CheckResult(cohortChecker.check(cohortDTO));
		checkResult.getWarnings().addAll(circeWarnings);
		return checkResult;
	}
	

	/**
	 * Render a cohort expression in html or markdown form.
	 *
	 * This method calls out to the markdown renderer for CIRCE cohort
	 * expressions, and then converts to HTML if required. The response will
	 * contain the media type as TEXT_PLAIN or markdown, or TEXT_HTML for html.
	 * The body of the response is the print friendly content.
	 *
	 * @summary Cohort Print Friendly
	 * @param expression The CIRCE cohort expression to render
	 * @param format The format to render, can be either 'html' or 'markdown'.  Defaults to 'html'
	 * @return an HTTP response with the content, with the appropriate MediaType
	 * based on the format that was requested.
	 */
	@POST
	@Path("/printfriendly/cohort")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response cohortPrintFriendly(CohortExpression expression, @DefaultValue("html") @QueryParam("format") String format) {
		String markdown = markdownPF.renderCohort(expression);
		return printFrindly(markdown, format);
	}

	/**
	 * Render a list of concept sets in html or markdown form.
	 *
	 * This method calls out to the markdown renderer concept set expressions, and
	 * then converts to HTML if required. The response will contain the media type
	 * as TEXT_PLAIN or markdown, or TEXT_HTML for html. The body of the response
	 * is the print friendly content.
	 *
	 * @summary Concept Set Print Friendly
	 * @param conceptSetList A List of concept set expressions
	 * @param format The format to render, can be either 'html' or 'markdown'.  Defaults to 'html'
	 * @return an HTTP response with the content, with the appropriate MediaType
	 * based on the format that was requested.
	 */
	@POST
	@Path("/printfriendly/conceptsets")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response conceptSetListPrintFriendly(List<ConceptSet> conceptSetList, @DefaultValue("html") @QueryParam("format") String format) {
		String markdown = markdownPF.renderConceptSetList(conceptSetList.toArray(new ConceptSet[0]));
		return printFrindly(markdown, format);
	}

	private Response printFrindly(String markdown, String format) {

		ResponseBuilder res;
		if ("html".equalsIgnoreCase(format)) {
			Parser parser = Parser.builder().extensions(extensions).build();
			Node document = parser.parse(markdown);
			HtmlRenderer renderer = HtmlRenderer.builder().extensions(extensions).build();
			String html = renderer.render(document);
			res = Response.ok(html, MediaType.TEXT_HTML);

		} else if ("markdown".equals(format)) {
			res = Response.ok(markdown, MediaType.TEXT_PLAIN);
		} else {
			res = Response.status(Response.Status.UNSUPPORTED_MEDIA_TYPE);
		}
		return res.build();
	}

	/**
	 * Assign tag to Cohort Definition
	 *
	 * @summary Assign Tag
	 * @param id the cohort definition id
	 * @param tagId the tag id
	 */
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{id}/tag/")
	@Transactional
	public void assignTag(@PathParam("id") final Integer id, final int tagId) {
		CohortDefinition entity = cohortDefinitionRepository.findOne(id);
		checkOwnerOrAdminOrGranted(entity);
		assignTag(entity, tagId);
	}

	/**
	 * Unassign tag from Cohort Definition
	 *
	 * @summary Unassign Tag
	 * @param id the cohort definition id
	 * @param tagId the tag id
	 */
	@DELETE
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{id}/tag/{tagId}")
	@Transactional
	public void unassignTag(@PathParam("id") final Integer id, @PathParam("tagId") final int tagId) {
		CohortDefinition entity = cohortDefinitionRepository.findOne(id);
		checkOwnerOrAdminOrGranted(entity);
		unassignTag(entity, tagId);
	}

	/**
	 * Assign protected tag to Cohort Definition. This method passes through to
	 * assignTag(), but permissions to access this endpoint is determined by the
	 * path /{id}/protectedtag
	 *
	 * @summary Assign Protected Tag
	 * @param id
	 * @param tagId
	 */
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{id}/protectedtag/")
	@Transactional
	public void assignPermissionProtectedTag(@PathParam("id") final int id, final int tagId) {
		assignTag(id, tagId);
	}

	/**
	 * Unassign protected tag from Cohort Definition
	 *
	 * @summary Unassign Protected Tag
	 * @param id
	 * @param tagId
	 */
	@DELETE
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{id}/protectedtag/{tagId}")
	@Transactional
	public void unassignPermissionProtectedTag(@PathParam("id") final int id, @PathParam("tagId") final int tagId) {
		unassignTag(id, tagId);
	}

	/**
	 * Get list of versions of Cohort Definition
	 *
	 * @summary Get Cohort Definition Versions
	 * @param id the cohort definition id
	 * @return the list of VersionDTO containing version info for the cohort
	 * definition
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{id}/version/")
	@Transactional
	public List<VersionDTO> getVersions(@PathParam("id") final long id) {
		List<VersionBase> versions = versionService.getVersions(VersionType.COHORT, id);
		return versions.stream()
				.map(v -> conversionService.convert(v, VersionDTO.class))
				.collect(Collectors.toList());
	}

	/**
	 * Get version of Cohort Definition
	 *
	 * @summary Get Cohort Definition Version
	 * @param id The cohort definition id
	 * @param version The version to fetch
	 * @return
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{id}/version/{version}")
	@Transactional
	public CohortVersionFullDTO getVersion(@PathParam("id") final int id, @PathParam("version") final int version) {
		checkVersion(id, version, false);
		CohortVersion cohortVersion = versionService.getById(VersionType.COHORT, id, version);

		return conversionService.convert(cohortVersion, CohortVersionFullDTO.class);
	}

	/**
	 * Updates version of Cohort Definition.
	 *
	 * The specified version is checked for permission and if it exists, and if
	 * this check passes, the version is updated.
	 *
	 * @summary Update Version
	 * @param id The cohort definition id
	 * @param version the id of the version
	 * @param updateDTO the new version data
	 * @return the updated version state as VersionDTO
	 */
	@PUT
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{id}/version/{version}")
	@Transactional
	public VersionDTO updateVersion(@PathParam("id") final int id, @PathParam("version") final int version,
									VersionUpdateDTO updateDTO) {
		checkVersion(id, version);
		updateDTO.setAssetId(id);
		updateDTO.setVersion(version);
		CohortVersion updated = versionService.update(VersionType.COHORT, updateDTO);

		return conversionService.convert(updated, VersionDTO.class);
	}

	/**
	 * Delete version of Cohort Definition
	 *
	 * @summary Delete Cohort Definition Version
	 * @param id the cohort definition id
	 * @param version the version id
	 */
	@DELETE
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{id}/version/{version}")
	@Transactional
	public void deleteVersion(@PathParam("id") final int id, @PathParam("version") final int version) {
		checkVersion(id, version);
		versionService.delete(VersionType.COHORT, id, version);
	}

	/**
	 * Create a new asset from version of Cohort Definition.
	 *
	 * This method fetches the cohort definition version based on the id and
	 * version parameter, and creates a new cohort definition (without tags) and
	 * an automatically generated name.
	 *
	 * @summary Create Cohort from Version
	 * @param id the cohort definition id
	 * @param version the version id
	 * @return
	 */
	@PUT
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{id}/version/{version}/createAsset")
	@Transactional
	public CohortDTO copyAssetFromVersion(@PathParam("id") final int id, @PathParam("version") final int version) {
		checkVersion(id, version, false);
		CohortVersion cohortVersion = versionService.getById(VersionType.COHORT, id, version);
		CohortVersionFullDTO fullDTO = conversionService.convert(cohortVersion, CohortVersionFullDTO.class);
		CohortDTO dto = conversionService.convert(fullDTO.getEntityDTO(), CohortDTO.class);
		dto.setId(null);
		dto.setTags(null);
		dto.setName(NameUtils.getNameForCopy(dto.getName(), this::getNamesLike,
				cohortDefinitionRepository.findByName(dto.getName())));
		return createCohortDefinition(dto);
	}

	/**
	 * Get list of cohort definitions with assigned tags.
	 *
   * This method accepts a TagNameListRequestDTO that contains the list of tag names
   * to find cohort definitions with.
	 *
   * @summary List Cohorts By Tag
	 * @param requestDTO contains a list of tag names
	 * @return the set of cohort definitions that match one of the included tag names.
	 */
	@POST
	@Path("/byTags")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Transactional
	public List<CohortDTO> listByTags(TagNameListRequestDTO requestDTO) {
		if (requestDTO == null || requestDTO.getNames() == null || requestDTO.getNames().isEmpty()) {
			return Collections.emptyList();
		}
		List<String> names = requestDTO.getNames().stream()
				.map(name -> name.toLowerCase(Locale.ROOT))
				.collect(Collectors.toList());
		List<CohortDefinition> entities = cohortDefinitionRepository.findByTags(names);
		return listByTags(entities, names, CohortDTO.class);
	}

	private void checkVersion(int id, int version) {
		checkVersion(id, version, true);
	}

	private void checkVersion(int id, int version, boolean checkOwnerShip) {
		Version cohortVersion = versionService.getById(VersionType.COHORT, id, version);
		ExceptionUtils.throwNotFoundExceptionIfNull(cohortVersion,
				String.format("There is no cohort version with id = %d.", version));

		CohortDefinition entity = cohortDefinitionRepository.findOne(id);
		if (checkOwnerShip) {
			checkOwnerOrAdminOrGranted(entity);
		}
	}

	private CohortVersion saveVersion(int id) {
		CohortDefinition def = this.cohortDefinitionRepository.findOneWithDetail(id);
		CohortVersion version = conversionService.convert(def, CohortVersion.class);

		UserEntity user = Objects.nonNull(def.getModifiedBy()) ? def.getModifiedBy() : def.getCreatedBy();
		Date versionDate = Objects.nonNull(def.getModifiedDate()) ? def.getModifiedDate() : def.getCreatedDate();
		version.setCreatedBy(user);
		version.setCreatedDate(versionDate);
		return versionService.create(VersionType.COHORT, version);
	}

	public List<CohortDTO> getCohortDTOs(List<Integer> ids) {
		return getCohorts(ids).stream()
				.map(def -> conversionService.convert(def, CohortDTO.class))
				.filter(Objects::nonNull)
				.collect(Collectors.toList());
	}

	public List<CohortDefinition> getCohorts(List<Integer> ids) {
		return ids.stream()
				.map(id -> cohortDefinitionRepository.findOne(id))
				.filter(Objects::nonNull)
				.collect(Collectors.toList());
	}
}
