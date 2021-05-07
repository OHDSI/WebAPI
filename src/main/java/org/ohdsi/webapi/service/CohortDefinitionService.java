/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ohdsi.webapi.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.commonmark.Extension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.ohdsi.analysis.Utils;
import org.ohdsi.circe.check.Checker;
import org.ohdsi.circe.check.warnings.BaseWarning;
import org.ohdsi.circe.cohortdefinition.CohortExpression;
import org.ohdsi.circe.cohortdefinition.CohortExpressionQueryBuilder;
import org.ohdsi.circe.cohortdefinition.ConceptSet;
import org.ohdsi.circe.cohortdefinition.printfriendly.MarkdownRender;
import org.ohdsi.sql.SqlRender;
import org.ohdsi.webapi.Constants;
import org.ohdsi.webapi.check.CheckResult;
import org.ohdsi.webapi.check.checker.cohort.CohortChecker;
import org.ohdsi.webapi.check.warning.DefaultWarning;
import org.ohdsi.webapi.check.warning.Warning;
import org.ohdsi.webapi.check.warning.WarningSeverity;
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
import org.ohdsi.webapi.cohortdefinition.dto.CohortRawDTO;
import org.ohdsi.webapi.cohortdefinition.event.CohortDefinitionChangedEvent;
import org.ohdsi.webapi.cohortsample.CleanupCohortSamplesTasklet;
import org.ohdsi.webapi.cohortsample.CohortSamplingService;
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
import org.ohdsi.webapi.source.SourceService;
import org.ohdsi.webapi.tag.TagService;
import org.ohdsi.webapi.tag.domain.Tag;
import org.ohdsi.webapi.util.ExceptionUtils;
import org.ohdsi.webapi.util.ExportUtil;
import org.ohdsi.webapi.util.HttpUtils;
import org.ohdsi.webapi.util.NameUtils;
import org.ohdsi.webapi.util.PreparedStatementRenderer;
import org.ohdsi.webapi.util.SessionUtils;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.builder.SimpleJobBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.convert.ConversionService;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import springfox.documentation.service.Tags;

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
import javax.ws.rs.core.Response.ResponseBuilder;
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
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.ohdsi.webapi.Constants.Params.COHORT_DEFINITION_ID;
import static org.ohdsi.webapi.Constants.Params.JOB_NAME;
import static org.ohdsi.webapi.Constants.Params.SOURCE_ID;
import static org.ohdsi.webapi.util.SecurityUtils.whitelist;

/**
 *
 * @author cknoll1
 */
@Path("/cohortdefinition")
@Component
public class CohortDefinitionService extends AbstractDaoService {

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
	private TagService tagService;

	@Autowired
	private CohortChecker cohortChecker;

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
	 * Returns all cohort definitions in the cohort schema
	 *
	 * @return List of cohort_definition
	 */
	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	@Transactional
	public List<CohortMetadataDTO> getCohortDefinitionList() {
		tagService.listInfoDTO("");
		List<CohortDefinition> definitions = cohortDefinitionRepository.list();

		return definitions.stream()
						.map(def -> {
							CohortMetadataDTO dto = conversionService.convert(def, CohortMetadataDTO.class);
							permissionService.fillWriteAccess(def, dto);
							return dto;
						})
						.collect(Collectors.toList());
	}

	/**
	 * Creates the cohort definition
	 *
	 * @param dto The cohort definition to create.
	 * @return The new CohortDefinition
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
	 * Returns the cohort definition for the given id
	 *
	 * @param id The cohort definition id
	 * @return The CohortDefinition
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
	 * This method returns the cohort definition containg the circe cohort
	 * expression
	 *
	 * @param id
	 * @return
	 */
	public CohortDTO getCohortDefinition(final int id) {

		return getTransactionTemplate().execute(transactionStatus -> {
			CohortDefinition d = this.cohortDefinitionRepository.findOneWithDetail(id);
			ExceptionUtils.throwNotFoundExceptionIfNull(d, String.format("There is no cohort definition with id = %d.", id));
			return conversionService.convert(d, CohortDTO.class);
		});
	}

	@GET
	@Path("/{id}/exists")
	@Produces(MediaType.APPLICATION_JSON)
	public int getCountCDefWithSameName(@PathParam("id") @DefaultValue("0") final int id, @QueryParam("name") String name) {

		return cohortDefinitionRepository.getCountCDefWithSameName(id, name);
	}

	/**
	 * Saves the cohort definition for the given id
	 *
	 * @param id The cohort definition id
	 * @return The CohortDefinition
	 */
	@PUT
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public CohortDTO saveCohortDefinition(@PathParam("id") final int id, CohortDTO def) {
		Date currentTime = Calendar.getInstance().getTime();

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
	 * @param id - the Cohort Definition ID to generate
	 * @return information about the Cohort Analysis Job
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
	 * Queues up a generate cohort task for the specified cohort definition id.
	 *
	 * @param id - the Cohort Definition ID to generate
	 * @return information about the Cohort Analysis Job
	 * @throws Exception
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{id}/info")
	@Transactional
	public List<CohortGenerationInfoDTO> getInfo(@PathParam("id") final int id) {
		CohortDefinition def = this.cohortDefinitionRepository.findOne(id);
		if (Objects.isNull(def)) {
			throw new IllegalArgumentException(String.format("There is no cohort definition with id = %d.", id));
		}
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
	 * Copies the specified cohort definition
	 *
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
	 * @param id - the Cohort Definition ID to copy
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

	@POST
	@Path("/check")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Transactional
	public CheckResult runDiagnostics(CohortDTO cohortDTO) {
		Checker checker = new Checker();
		CheckResultDTO checkResultDTO = new CheckResultDTO(checker.check(cohortDTO.getExpression()));
		List<Warning> circeWarnings = checkResultDTO.getWarnings().stream()
				.map(WarningUtils::convertCirceWarning)
				.collect(Collectors.toList());
		CheckResult checkResult = new CheckResult(cohortChecker.check(cohortDTO));
		checkResult.getWarnings().addAll(circeWarnings);
		return checkResult;
	}

	@POST
	@Path("/printfriendly/cohort")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response cohortPrintFriendly(CohortExpression expression, @DefaultValue("html") @QueryParam("format") String format) {
		String markdown = markdownPF.renderCohort(expression);
		return printFrindly(markdown, format);
	}

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

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{id}/tag/")
	@Transactional
	public void assignTag(@PathParam("id") final int id, final int tagId) {
		CohortDefinition def = cohortDefinitionRepository.findOne(id);
		if (Objects.nonNull(def)) {
			Tag tag = tagService.getById(tagId);
			if (Objects.nonNull(tag)) {
				def.getTags().add(tag);
			}
		}
	}

	@DELETE
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{id}/tag/{tagId}")
	@Transactional
	public void unassignTag(@PathParam("id") final int id, @PathParam("tagId") final int tagId) {
		CohortDefinition def = cohortDefinitionRepository.findOne(id);
		if (Objects.nonNull(def)) {
			Set<Tag> tags = def.getTags().stream()
					.filter(t -> t.getId() != tagId)
					.collect(Collectors.toSet());
			def.setTags(tags);
		}
	}
}
