/*
 * Copyright 2017 Observational Health Data Sciences and Informatics [OHDSI.org].
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

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.jasper.builder.export.JasperDocxExporterBuilder;
import net.sf.dynamicreports.jasper.builder.export.JasperHtmlExporterBuilder;
import net.sf.dynamicreports.jasper.builder.export.JasperPdfExporterBuilder;
import net.sf.dynamicreports.report.builder.DynamicReports;
import org.apache.commons.lang.StringUtils;
import org.ohdsi.sql.SqlRender;
import org.ohdsi.sql.SqlTranslate;
import org.ohdsi.webapi.helper.ResourceHelper;
import org.ohdsi.webapi.shiro.management.Security;
import org.ohdsi.webapi.study.Study;
import org.ohdsi.webapi.study.StudyCohort;
import org.ohdsi.webapi.study.StudySource;
import org.ohdsi.webapi.study.report.Report;
import org.ohdsi.webapi.study.report.ReportCohortPair;
import org.ohdsi.webapi.study.report.ReportContent;
import org.ohdsi.webapi.study.report.ReportCovariate;
import org.ohdsi.webapi.study.report.ReportRepository;
import org.ohdsi.webapi.study.report.ReportSource;
import org.ohdsi.webapi.study.report.ReportStatus;
import org.ohdsi.webapi.study.report.StudyReportManager;
import org.ohdsi.webapi.util.SessionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Component;

/**
 *
 * @author Chris Knoll <cknoll@ohdsi.org>
 */
@Path("/report")
@Component
public class StudyReportService extends AbstractDaoService {

	private final String QUERY_COVARIATE_STATS = ResourceHelper.GetResourceAsString("/resources/study/report/sql/queryCovariateStats.sql");
	private final String QUERY_OUTCOME_STATS = ResourceHelper.GetResourceAsString("/resources/study/report/sql/queryOutcomeStats.sql");

	@Autowired
	StudyService studyService;

	@Autowired
	ReportRepository reportRepository;

	@Autowired
	private Security security;
	
	@Autowired
	private StudyReportManager studyReportManager = new StudyReportManager();

	@PersistenceContext
	protected EntityManager entityManager;

	public static class ReportListItem {

		private Integer id;
		private String name;
		private String description;
		private Integer studyId;
		private String studyName;
		private String createdBy;
		@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
		private Date createdDate;
		private String modifiedBy;
		@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
		private Date modifiedDate;
		private ReportStatus status = ReportStatus.DRAFT;

		public Integer getId() {
			return id;
		}

		public void setId(Integer id) {
			this.id = id;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		public Integer getStudyId() {
			return studyId;
		}

		public void setStudyId(Integer studyId) {
			this.studyId = studyId;
		}

		public String getStudyName() {
			return studyName;
		}

		public void setStudyName(String studyName) {
			this.studyName = studyName;
		}

		public String getCreatedBy() {
			return createdBy;
		}

		public void setCreatedBy(String createdBy) {
			this.createdBy = createdBy;
		}

		public Date getCreatedDate() {
			return createdDate;
		}

		public void setCreatedDate(Date createdDate) {
			this.createdDate = createdDate;
		}

		public String getModifiedBy() {
			return modifiedBy;
		}

		public void setModifiedBy(String modifiedBy) {
			this.modifiedBy = modifiedBy;
		}

		public Date getModifiedDate() {
			return modifiedDate;
		}

		public void setModifiedDate(Date modifiedDate) {
			this.modifiedDate = modifiedDate;
		}

		public ReportStatus getStatus() {
			return status;
		}

		public void setStatus(ReportStatus status) {
			this.status = status;
		}

	}

	public static class ReportDTO extends ReportListItem {

		public List<StudyService.CohortDetail> cohorts = new ArrayList<>();
		public List<CohortPair> cohortPairs = new ArrayList<>();
		public Set<ReportCovariate> covariates = new HashSet<>();
		public List<ReportContent> content = new ArrayList<>();
		public List<ReportSourceDTO> sources = new ArrayList<>();
	}

	public static class CohortPair {

		public long target;
		public long outcome;
		public boolean isActive;

		public CohortPair() {
		}

		public CohortPair(long target, long outcome, boolean isActive) {
			this.target = target;
			this.outcome = outcome;
			this.isActive = isActive;
		}
	}

	public static class ReportSourceDTO extends StudyService.StudySourceDTO {

		public boolean isActive;

		public ReportSourceDTO() {
		}

		public ReportSourceDTO(int sourceId, String name, boolean isActive) {
			this.sourceId = sourceId;
			this.name = name;
			this.isActive = isActive;
		}
	}

	public static class CovariateStat {

		private String dataSource;
		private int cohortId;
		private long covariateId;
		private String name;
		private long count;
		private double statValue;

		public CovariateStat() {
		}

		public CovariateStat(String dataSource, int cohortId, long covariateId, String name, long count, double statValue) {
			this.dataSource = dataSource;
			this.cohortId = cohortId;
			this.covariateId = covariateId;
			this.name = name;
			this.count = count;
			this.statValue = statValue;
		}

		public String getDataSource() {
			return dataSource;
		}

		public void setDataSource(String dataSource) {
			this.dataSource = dataSource;
		}

		public int getCohortId() {
			return cohortId;
		}

		public void setCohortId(int cohortId) {
			this.cohortId = cohortId;
		}

		public long getCovariateId() {
			return covariateId;
		}

		public void setCovariateId(long covariateId) {
			this.covariateId = covariateId;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public long getCount() {
			return count;
		}

		public void setCount(long count) {
			this.count = count;
		}

		public double getStatValue() {
			return statValue;
		}

		public void setStatValue(double statValue) {
			this.statValue = statValue;
		}

	}
	
	public static class OutcomeSummaryStat {
		private String dataSource;
		private long targetCohortId;
		private long outcomeCohortId;
		private int atRiskPP;
		private int casesPP;
		private Double personTimePP;
		private Double incidenceProportionPP;
		private Double incidenceRatePP;
		private int casesITT;
		private Double personTimeITT;
		private Double incidenceProportionITT;
		private Double incidenceRateITT;

		public OutcomeSummaryStat(String dataSource, long targetCohorId, long outcomeCohortId, int atRiskPP, int casesPP, Double personTimePP, Double incidenceProportionPP, Double incidenceRatePP, int casesITT, Double personTimeITT, Double incidenceProportionITT, Double incidenceRateITT) {
			this.dataSource = dataSource;
			this.targetCohortId = targetCohorId;
			this.outcomeCohortId = outcomeCohortId;
			this.atRiskPP = atRiskPP;
			this.casesPP = casesPP;
			this.personTimePP = personTimePP;
			this.incidenceProportionPP = incidenceProportionPP;
			this.incidenceRatePP = incidenceRatePP;
			this.casesITT = casesITT;
			this.personTimeITT = personTimeITT;
			this.incidenceProportionITT = incidenceProportionITT;
			this.incidenceRateITT = incidenceRateITT;
		}

		public String getDataSource() {
			return dataSource;
		}

		public void setDataSource(String dataSource) {
			this.dataSource = dataSource;
		}

		public long getTargetCohortId() {
			return targetCohortId;
		}

		public void setTargetCohortId(long targetCohortId) {
			this.targetCohortId = targetCohortId;
		}

		public long getOutcomeCohortId() {
			return outcomeCohortId;
		}

		public void setOutcomeCohortId(long outcomeCohortId) {
			this.outcomeCohortId = outcomeCohortId;
		}

		public int getAtRiskPP() {
			return atRiskPP;
		}

		public void setAtRiskPP(int atRiskPP) {
			this.atRiskPP = atRiskPP;
		}

		public int getCasesPP() {
			return casesPP;
		}

		public void setCasesPP(int casesPP) {
			this.casesPP = casesPP;
		}

		public Double getPersonTimePP() {
			return personTimePP;
		}

		public void setPersonTimePP(Double personTimePP) {
			this.personTimePP = personTimePP;
		}

		public Double getIncidenceProportionPP() {
			return incidenceProportionPP;
		}

		public void setIncidenceProportionPP(Double incidenceProportionPP) {
			this.incidenceProportionPP = incidenceProportionPP;
		}

		public Double getIncidenceRatePP() {
			return incidenceRatePP;
		}

		public void setIncidenceRatePP(Double incidenceRatePP) {
			this.incidenceRatePP = incidenceRatePP;
		}

		public int getCasesITT() {
			return casesITT;
		}

		public void setCasesITT(int casesITT) {
			this.casesITT = casesITT;
		}

		public Double getPersonTimeITT() {
			return personTimeITT;
		}

		public void setPersonTimeITT(Double personTimeITT) {
			this.personTimeITT = personTimeITT;
		}

		public Double getIncidenceProportionITT() {
			return incidenceProportionITT;
		}

		public void setIncidenceProportionITT(Double incidenceProportionITT) {
			this.incidenceProportionITT = incidenceProportionITT;
		}

		public Double getIncidenceRateITT() {
			return incidenceRateITT;
		}

		public void setIncidenceRateITT(Double incidenceRateITT) {
			this.incidenceRateITT = incidenceRateITT;
		}
		
		
	}

	private ReportDTO fromReport(Report report) {
		HashMap<Long, StudyService.CohortDetail> cohorts = new HashMap<>();

		ReportDTO result = new ReportDTO();
		result.setId(report.getId());
		result.setName(report.getName());
		result.setDescription(report.getDescription());
		result.setStudyId(report.getStudy().getId());
		result.setStudyName(report.getStudy().getName());
		result.setCreatedBy(report.getCreatedBy());
		result.setCreatedDate(report.getCreatedDate());
		result.setModifiedBy(report.getModifiedBy());
		result.setModifiedDate(report.getModifiedDate());
		result.setStatus(report.getStatus());

		result.cohortPairs = report.getCohortPairs().stream().map(p -> {
			StudyService.CohortDetail target;
			if ((target = cohorts.get(p.getTarget().getId())) == null) {
				target = studyService.fromStudyCohort(p.getTarget());
				cohorts.put(target.cohortId, target);
			}

			StudyService.CohortDetail outcome;
			if ((outcome = cohorts.get(p.getOutcome().getId())) == null) {
				outcome = studyService.fromStudyCohort(p.getOutcome());
				cohorts.put(outcome.cohortId, outcome);
			}

			cohorts.put(target.cohortId, target);
			cohorts.put(outcome.cohortId, outcome);
			CohortPair pair = new CohortPair(target.cohortId, outcome.cohortId, p.isActive());
			return pair;
		}).collect(Collectors.toList());
		result.covariates = report.getCovariates().stream().collect(Collectors.toSet());
		result.cohorts = cohorts.values().stream().collect(Collectors.toList());
		result.content = report.getContent().stream().collect(Collectors.toList());
		result.sources = report.getSources().stream().map(s -> {
			return new ReportSourceDTO(s.getSource().getId(), s.getSource().getName(), s.isActive());
		}).collect(Collectors.toList());

		return result;
	}

	private ReportDTO save(ReportDTO report) {
		Date currentTime = Calendar.getInstance().getTime();
		Report reportEntity;

		if (report.getId() != null) {
			reportEntity = reportRepository.findOne(report.getId());
			reportEntity.setModifiedDate(currentTime);
			reportEntity.setModifiedBy(security.getSubject());
		} else {
			reportEntity = new Report();
			reportEntity.setCreatedDate(currentTime);
			reportEntity.setCreatedBy(security.getSubject());
			reportEntity.setModifiedBy(null);
			reportEntity.setModifiedDate(null);
			reportEntity.setStudy(entityManager.getReference(Study.class, report.getStudyId()));
			reportEntity.setStatus(ReportStatus.DRAFT);
		}

		reportEntity.setName(report.getName());
		reportEntity.setDescription(report.getDescription());
		reportEntity.setCohortPairs(report.cohortPairs.stream().map(p -> {
			ReportCohortPair pair = new ReportCohortPair();
			pair.setTarget(entityManager.getReference(StudyCohort.class, p.target));
			pair.setOutcome(entityManager.getReference(StudyCohort.class, p.outcome));
			pair.setActive(p.isActive);
			return pair;
		}).collect(Collectors.toList()));
		reportEntity.setContent(report.content);
		reportEntity.setCovariates(report.covariates);
		reportEntity.setSources(report.sources.stream().map(s -> {
			ReportSource source = new ReportSource();
			source.setActive(s.isActive);
			source.setSource(entityManager.getReference(StudySource.class, s.sourceId));
			return source;
		}).collect(Collectors.toList()));

		reportEntity = reportRepository.save(reportEntity);

		return fromReport(reportEntity);
	}

	private JdbcTemplate getSourceJdbcTemplate(StudySource source) {
		DriverManagerDataSource dataSource = new DriverManagerDataSource(source.getSourceConnection());
		JdbcTemplate template = new JdbcTemplate(dataSource);
		return template;
	}

	private String buildCovariateStatQuery(List<Long> cohorts, List<Integer> sources, List<Long> covariateIds) {

		
		String covaraiteIdList = StringUtils.join(covariateIds, ",");
		String cohortIdList = StringUtils.join(cohorts, ",");
		String sourceIdList = StringUtils.join(sources, ",");

		String covariateQuery = QUERY_COVARIATE_STATS;
		covariateQuery = StringUtils.replace(covariateQuery, "@covariate_id_list", covaraiteIdList);
		covariateQuery = StringUtils.replace(covariateQuery, "@cohort_id_list", cohortIdList);
		covariateQuery = StringUtils.replace(covariateQuery, "@source_id_list", sourceIdList);

		return covariateQuery;
	}

	private String buildOutcomeStatQuery(List<Long> targetCohortIds, List<Long> outcomeCohortIds, List<Integer> sources) {

		String covariateQuery = QUERY_OUTCOME_STATS;
		covariateQuery = StringUtils.replace(covariateQuery, "@target_id_list", StringUtils.join(targetCohortIds, ","));
		covariateQuery = StringUtils.replace(covariateQuery, "@outcome_id_list", StringUtils.join(outcomeCohortIds, ","));
		covariateQuery = StringUtils.replace(covariateQuery, "@source_id_list", StringUtils.join(sources, ","));

		return covariateQuery;
	}
	
	public List<CovariateStat> getReportCovariates(Report report) {

		// Collect list of covariateIds
		List<Long> covariateIds = report.getCovariates().stream().map(ReportCovariate::getCovariateId).collect(Collectors.toList());
		
		// Collect list of cohort IDs
		List<Long> cohorts = report.getCohortPairs().stream().map(rc -> rc.getTarget().getId()).distinct().collect(Collectors.toList());
		
		// Use the order of sources that are returned from the Report entity, but only include isActive()
		List<Integer> activeSourceIds = report.getSources().stream().filter(source -> source.isActive()).map(rs -> rs.getSource().getId()).collect(Collectors.toList());

		// Get covaraite stats from each active datasource
		String covariateQuery = buildCovariateStatQuery(cohorts, activeSourceIds, covariateIds);
		covariateQuery = SqlRender.renderSql(covariateQuery, 
			new String[] {"study_results_schema"}, 
			new String[] {this.getStudyResultsSchema()}
		);
		
		String translatedSql = SqlTranslate.translateSql(covariateQuery, "sql server", this.getStudyResultsDialect(), SessionUtils.sessionId(), this.getStudyResultsSchema());
		List<CovariateStat> covariateStats = this.getStudyResultsJdbcTemplate().query(translatedSql, (row, rowNum) -> {
			return new CovariateStat(row.getString("source_key"),
							row.getInt("cohort_definition_id"),
							row.getLong("covariate_id"),
							row.getString("covariate_name"),
							row.getLong("count_value"),
							row.getDouble("stat_value")
			);
		});

		return covariateStats;
	}
	
	public List<OutcomeSummaryStat> getReportOutcomes(Report report) {
		// Collect list of cohort IDs
		List<Long> targetCohorts = report.getCohortPairs().stream().map(rc -> rc.getTarget().getId()).distinct().collect(Collectors.toList());
		List<Long> outcomeCohorts = report.getCohortPairs().stream().map(rc -> rc.getOutcome().getId()).distinct().collect(Collectors.toList());
		
		// Use the order of sources that are returned from the Report entity, but only include isActive()
		List<Integer> activeSourceIds = report.getSources().stream().filter(source -> source.isActive()).map(rs -> rs.getSource().getId()).collect(Collectors.toList());
		
		String outcomeQuery = buildOutcomeStatQuery(targetCohorts, outcomeCohorts, activeSourceIds);
		outcomeQuery = SqlRender.renderSql(outcomeQuery, 
			new String[] {"study_results_schema"}, 
			new String[] {this.getStudyResultsSchema()}
		);
		
		String translatedSql = SqlTranslate.translateSql(outcomeQuery, "sql server", this.getStudyResultsDialect(), SessionUtils.sessionId(), this.getStudyResultsSchema());
		
		List<OutcomeSummaryStat> outcomeStats = this.getStudyResultsJdbcTemplate().query(translatedSql, (row, rowNum) -> {
			return new OutcomeSummaryStat(row.getString("source_key"),
				row.getLong("target_cohort_id"),
				row.getLong("outcome_cohort_id"),
				row.getInt("at_risk_pp"),
				row.getInt("cases_pp"),
				row.getDouble("pt_pp"),
				row.getDouble("ip_pp"),
				row.getDouble("ir_pp"),
				row.getInt("cases_itt"),
				row.getDouble("pt_itt"),
				row.getDouble("ip_itt"),
				row.getDouble("ir_itt")
			);
		});
		
		return outcomeStats;
	}
	

	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public List<ReportListItem> getReportList() {
		List<Report> reports = reportRepository.list();

		List<ReportListItem> result = reports.stream().map(r -> {
			ReportListItem item = new ReportListItem();
			item.setId(r.getId());
			item.setName(r.getName());
			item.setDescription(r.getDescription());
			item.setStudyId(r.getStudy().getId());
			item.setStudyName(r.getStudy().getName());
			item.setCreatedBy(r.getCreatedBy());
			item.setCreatedDate(r.getCreatedDate());
			item.setModifiedBy(r.getModifiedBy());
			item.setModifiedDate(r.getModifiedDate());
			item.setStatus(r.getStatus());

			return item;
		}).collect(Collectors.toList());

		return result;
	}

	@POST
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Transactional
	public ReportDTO createReport(ReportDTO report) {
		if (report.getId() != null) {
			// POST to url should result in a new creation of an entity, so clear any existing reportId.  
			// Alternatively we could throw an exception here.
			report.setId(null);
		}

		return save(report);
	}

	@PUT
	@Path("/{reportId}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Transactional
	public ReportDTO saveReport(ReportDTO report) {
		return save(report);
	}

	@GET
	@Path("/{reportId}")
	@Produces(MediaType.APPLICATION_JSON)
	@Transactional
	public ReportDTO getReport(
					@PathParam("reportId") final int reportId
	) {
		Report report = reportRepository.findOne(reportId);
		return fromReport(report);
	}

	@GET
	@Transactional
	@Path("/{reportId}.pdf")
	public Response getReportPdf(@PathParam("reportId") final int reportId) throws Exception {
		Report report = reportRepository.findOne(reportId);
		JasperReportBuilder jp = studyReportManager.getMainReport(report);

		StreamingOutput output = (out) -> {
			try {
				JasperPdfExporterBuilder exporter = DynamicReports.export.pdfExporter(out);
				jp.toPdf(exporter);
			} catch (Exception ex) {
				throw new WebApplicationException(ex, Response.Status.INTERNAL_SERVER_ERROR);
			}
		};

		Response response = Response
						.ok(output)
						.type("application/pdf")
						.header("Content-disposition", "inline; filename=reportList.pdf")
						.build();

		return response;
	}

	@GET
	@Transactional
	@Path("/{reportId}.docx")
	public Response getReportDocx(@PathParam("reportId") final int reportId) throws Exception {
		Report report = reportRepository.findOne(reportId);
		JasperReportBuilder jp = studyReportManager.getMainReport(report);

		// stream output to client
		StreamingOutput output = (out) -> {
			JasperDocxExporterBuilder exporter = DynamicReports.export.docxExporter(out);
			
			exporter.setFramesAsNestedTables(false);
			try {
				jp.toDocx(exporter);
			} catch (Exception ex) {
				throw new WebApplicationException(ex, Response.Status.INTERNAL_SERVER_ERROR);
			}
		};

		Response response = Response
						.ok(output)
						.type("application/vnd.openxmlformats-officedocument.wordprocessingml.document")
						.header("Content-disposition", "inline; filename=reportList.docx")
						.build();

		return response;
	}
	
	@GET
	@Transactional
	@Path("/{reportId}.html")
	public Response getReportHtml(@PathParam("reportId") final int reportId) throws Exception {
		Report report = reportRepository.findOne(reportId);
		JasperReportBuilder jp = studyReportManager.getMainReport(report);

		// stream output to client
		StreamingOutput output = (out) -> {
			
			JasperHtmlExporterBuilder exporter = DynamicReports.export.htmlExporter(out);
			exporter.setImagesURI("/imagesPath/");
			try {
				jp.toHtml(exporter);
			} catch (Exception ex) {
				throw new WebApplicationException(ex, Response.Status.INTERNAL_SERVER_ERROR);
			}
		};

		Response response = Response
						.ok(output)
						.type("text/html;charset=UTF-8")
						.build();

		return response;
	}
	
}
