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
import java.util.List;
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
import net.sf.dynamicreports.jasper.builder.export.JasperPdfExporterBuilder;
import net.sf.dynamicreports.report.builder.DynamicReports;
import net.sf.jasperreports.engine.export.HtmlExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleHtmlExporterOutput;
import net.sf.jasperreports.export.SimpleHtmlReportConfiguration;
import org.apache.commons.lang.StringUtils;
import org.ohdsi.sql.SqlRender;
import org.ohdsi.sql.SqlTranslate;
import org.ohdsi.webapi.helper.ResourceHelper;
import org.ohdsi.webapi.shiro.management.Security;
import org.ohdsi.webapi.study.Study;
import org.ohdsi.webapi.study.StudyCohort;
import org.ohdsi.webapi.study.StudySource;
import org.ohdsi.webapi.study.report.CovariateSection;
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

	private final String QUERY_COVARIATE_PREVALENCE_STATS = ResourceHelper.GetResourceAsString("/resources/study/report/sql/queryCovariatePrevalenceStats.sql");
	private final String QUERY_COVARIATE_DISTRIBUTION_STATS = ResourceHelper.GetResourceAsString("/resources/study/report/sql/queryCovariateDistributionStats.sql");
	private final String QUERY_OUTCOME_STATS = ResourceHelper.GetResourceAsString("/resources/study/report/sql/queryOutcomeStats.sql");
	private final String QUERY_CCA_STATS = ResourceHelper.GetResourceAsString("/resources/study/report/sql/queryCCAStats.sql");
	private final String QUERY_SCCA_STATS = ResourceHelper.GetResourceAsString("/resources/study/report/sql/querySCCAStats.sql");

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
		public List<ReportCovariate> covariates = new ArrayList<>();
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

		@Override
		public boolean equals(Object o) {
			if (!(o instanceof CohortPair)) {
				return false;
			}
			CohortPair c = (CohortPair) o;
			return (this.target == c.target && this.outcome == c.outcome);
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

	public static class PrevalenceStat {

		private String dataSource;
		private int cohortId;
		private long covariateId;
		private String name;
		private String analysisName;
		private String timeWindow;
		private long count;
		private double statValue;

		public PrevalenceStat() {
		}

		public PrevalenceStat(String dataSource, int cohortId, long covariateId, String name, String analysisName, String timeWindow, long count, double statValue) {
			this.dataSource = dataSource;
			this.cohortId = cohortId;
			this.covariateId = covariateId;
			this.name = name;
			this.analysisName = analysisName;
			this.timeWindow = timeWindow;
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

		public String getAnalysisName() {
			return analysisName;
		}

		public void setAnalysisName(String analysisName) {
			this.analysisName = analysisName;
		}

		public String getTimeWindow() {
			return timeWindow;
		}

		public void setTimeWindow(String timeWindow) {
			this.timeWindow = timeWindow;
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

	public static class DistributionStat {
		private String dataSource;
		private int cohortId;
		private String cohortName;
		private long covariateId;
		private String covariateName;
		private String analysisName;
		private String timeWindow;
		private long count;
		private double avg;
		private double stdev;
		private double min;
		private double p10;
		private double p25;
		private double median;
		private double p75;
		private double p90;
		private double max;

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

		public String getCohortName() {
			return cohortName;
		}

		public void setCohortName(String cohortName) {
			this.cohortName = cohortName;
		}

		public long getCovariateId() {
			return covariateId;
		}

		public void setCovariateId(long covariateId) {
			this.covariateId = covariateId;
		}

		public String getCovariateName() {
			return covariateName;
		}

		public void setCovariateName(String covariateName) {
			this.covariateName = covariateName;
		}

		public String getAnalysisName() {
			return analysisName;
		}

		public void setAnalysisName(String analysisName) {
			this.analysisName = analysisName;
		}

		public String getTimeWindow() {
			return timeWindow;
		}

		public void setTimeWindow(String timeWindow) {
			this.timeWindow = timeWindow;
		}

		public long getCount() {
			return count;
		}

		public void setCount(long count) {
			this.count = count;
		}

		public double getAvg() {
			return avg;
		}

		public void setAvg(double avg) {
			this.avg = avg;
		}

		public double getStdev() {
			return stdev;
		}

		public void setStdev(double stdev) {
			this.stdev = stdev;
		}

		public double getMin() {
			return min;
		}

		public void setMin(double min) {
			this.min = min;
		}

		public double getP10() {
			return p10;
		}

		public void setP10(double p10) {
			this.p10 = p10;
		}

		public double getP25() {
			return p25;
		}

		public void setP25(double p25) {
			this.p25 = p25;
		}

		public double getMedian() {
			return median;
		}

		public void setMedian(double median) {
			this.median = median;
		}

		public double getP75() {
			return p75;
		}

		public void setP75(double p75) {
			this.p75 = p75;
		}

		public double getP90() {
			return p90;
		}

		public void setP90(double p90) {
			this.p90 = p90;
		}

		public double getMax() {
			return max;
		}

		public void setMax(double max) {
			this.max = max;
		}

		public DistributionStat dataSource(final String value) {
			this.dataSource = value;
			return this;
		}

		public DistributionStat cohortId(final int value) {
			this.cohortId = value;
			return this;
		}

		public DistributionStat cohortName(final String value) {
			this.cohortName = value;
			return this;
		}

		public DistributionStat covariateId(final long value) {
			this.covariateId = value;
			return this;
		}

		public DistributionStat covariateName(final String value) {
			this.covariateName = value;
			return this;
		}

		public DistributionStat analysisName(final String value) {
			this.analysisName = value;
			return this;
		}

		public DistributionStat timeWindow(final String value) {
			this.timeWindow = value;
			return this;
		}

		public DistributionStat count(final long value) {
			this.count = value;
			return this;
		}

		public DistributionStat avg(final double value) {
			this.avg = value;
			return this;
		}

		public DistributionStat stdev(final double value) {
			this.stdev = value;
			return this;
		}

		public DistributionStat min(final double value) {
			this.min = value;
			return this;
		}

		public DistributionStat p10(final double value) {
			this.p10 = value;
			return this;
		}

		public DistributionStat p25(final double value) {
			this.p25 = value;
			return this;
		}

		public DistributionStat median(final double value) {
			this.median = value;
			return this;
		}

		public DistributionStat p75(final double value) {
			this.p75 = value;
			return this;
		}

		public DistributionStat p90(final double value) {
			this.p90 = value;
			return this;
		}

		public DistributionStat max(final double value) {
			this.max = value;
			return this;
		}
	
	}
	
	public static class OutcomeSummaryStat {

		private String dataSource;
		private long targetCohortId;
		private String targetCohortName;
		private long outcomeCohortId;
		private String outcomeCohortName;
		private int atRiskPP;
		private int casesPP;
		private Double personTimePP;
		private Double incidenceProportionPP;
		private Double incidenceRatePP;
		private int casesITT;
		private Double personTimeITT;
		private Double incidenceProportionITT;
		private Double incidenceRateITT;

		public OutcomeSummaryStat() {
		}

		public String getDataSource() {
			return dataSource;
		}

		public long getTargetCohortId() {
			return targetCohortId;
		}

		public String getTargetCohortName() {
			return targetCohortName;
		}

		public long getOutcomeCohortId() {
			return outcomeCohortId;
		}

		public String getOutcomeCohortName() {
			return outcomeCohortName;
		}

		public int getAtRiskPP() {
			return atRiskPP;
		}

		public int getCasesPP() {
			return casesPP;
		}

		public Double getPersonTimePP() {
			return personTimePP;
		}

		public Double getIncidenceProportionPP() {
			return incidenceProportionPP;
		}

		public Double getIncidenceRatePP() {
			return incidenceRatePP;
		}

		public int getCasesITT() {
			return casesITT;
		}

		public Double getPersonTimeITT() {
			return personTimeITT;
		}

		public Double getIncidenceProportionITT() {
			return incidenceProportionITT;
		}

		public Double getIncidenceRateITT() {
			return incidenceRateITT;
		}

		public OutcomeSummaryStat dataSource(final String value) {
			this.dataSource = value;
			return this;
		}

		public OutcomeSummaryStat targetCohortId(final long value) {
			this.targetCohortId = value;
			return this;
		}

		public OutcomeSummaryStat targetCohortName(final String value) {
			this.targetCohortName = value;
			return this;
		}

		public OutcomeSummaryStat outcomeCohortId(final long value) {
			this.outcomeCohortId = value;
			return this;
		}

		public OutcomeSummaryStat outcomeCohortName(final String value) {
			this.outcomeCohortName = value;
			return this;
		}

		public OutcomeSummaryStat atRiskPP(final int value) {
			this.atRiskPP = value;
			return this;
		}

		public OutcomeSummaryStat casesPP(final int value) {
			this.casesPP = value;
			return this;
		}

		public OutcomeSummaryStat personTimePP(final Double value) {
			this.personTimePP = value;
			return this;
		}

		public OutcomeSummaryStat incidenceProportionPP(final Double value) {
			this.incidenceProportionPP = value;
			return this;
		}

		public OutcomeSummaryStat incidenceRatePP(final Double value) {
			this.incidenceRatePP = value;
			return this;
		}

		public OutcomeSummaryStat casesITT(final int value) {
			this.casesITT = value;
			return this;
		}

		public OutcomeSummaryStat personTimeITT(final Double value) {
			this.personTimeITT = value;
			return this;
		}

		public OutcomeSummaryStat incidenceProportionITT(final Double value) {
			this.incidenceProportionITT = value;
			return this;
		}

		public OutcomeSummaryStat incidenceRateITT(final Double value) {
			this.incidenceRateITT = value;
			return this;
		}
	}

	public static class EffectEstimateStat {

		private int analysisId;
		private String dataSource;
		private long targetCohortId;
		private String targetCohortName;
		private long comparatorCohortId;
		private String comparatorCohortName;
		private long outcomeCohortId;
		private String outcomeCohortName;
		private int atRisk;
		private int casesPP;
		private Double personTimePP;
		private Double relativeRiskPP;
		private Double lb95PP;
		private Double ub95PP;
		private int casesITT;
		private Double personTimeITT;
		private Double relativeRiskITT;
		private Double lb95ITT;
		private Double ub95ITT;

		public EffectEstimateStat() {
		}

		public int getAnalysisId() {
			return analysisId;
		}

		public void setAnalysisId(int analysisId) {
			this.analysisId = analysisId;
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

		public String getTargetCohortName() {
			return targetCohortName;
		}

		public void setTargetCohortName(String targetCohortName) {
			this.targetCohortName = targetCohortName;
		}

		public long getComparatorCohortId() {
			return comparatorCohortId;
		}

		public void setComparatorCohortId(long comparatorCohortId) {
			this.comparatorCohortId = comparatorCohortId;
		}

		public String getComparatorCohortName() {
			return comparatorCohortName;
		}

		public void setComparatorCohortName(String comparatorCohortName) {
			this.comparatorCohortName = comparatorCohortName;
		}

		public long getOutcomeCohortId() {
			return outcomeCohortId;
		}

		public void setOutcomeCohortId(long outcomeCohortId) {
			this.outcomeCohortId = outcomeCohortId;
		}

		public String getOutcomeCohortName() {
			return outcomeCohortName;
		}

		public void setOutcomeCohortName(String outcomeCohortName) {
			this.outcomeCohortName = outcomeCohortName;
		}

		public int getAtRisk() {
			return atRisk;
		}

		public void setAtRisk(int atRisk) {
			this.atRisk = atRisk;
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

		public Double getRelativeRiskPP() {
			return relativeRiskPP;
		}

		public void setRelativeRiskPP(Double relativeRiskPP) {
			this.relativeRiskPP = relativeRiskPP;
		}

		public Double getLb95PP() {
			return lb95PP;
		}

		public void setLb95PP(Double lb95PP) {
			this.lb95PP = lb95PP;
		}

		public Double getUb95PP() {
			return ub95PP;
		}

		public void setUb95PP(Double ub95PP) {
			this.ub95PP = ub95PP;
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

		public Double getRelativeRiskITT() {
			return relativeRiskITT;
		}

		public void setRelativeRiskITT(Double relativeRiskITT) {
			this.relativeRiskITT = relativeRiskITT;
		}

		public Double getLb95ITT() {
			return lb95ITT;
		}

		public void setLb95ITT(Double lb95ITT) {
			this.lb95ITT = lb95ITT;
		}

		public Double getUb95ITT() {
			return ub95ITT;
		}

		public void setUb95ITT(Double ub95ITT) {
			this.ub95ITT = ub95ITT;
		}

		public EffectEstimateStat analysisId(final int value) {
			this.analysisId = value;
			return this;
		}

		public EffectEstimateStat dataSource(final String value) {
			this.dataSource = value;
			return this;
		}

		public EffectEstimateStat targetCohortId(final long value) {
			this.targetCohortId = value;
			return this;
		}

		public EffectEstimateStat targetCohortName(final String value) {
			this.targetCohortName = value;
			return this;
		}

		public EffectEstimateStat comparatorCohortId(final long value) {
			this.comparatorCohortId = value;
			return this;
		}

		public EffectEstimateStat comparatorCohortName(final String value) {
			this.comparatorCohortName = value;
			return this;
		}

		public EffectEstimateStat outcomeCohortId(final long value) {
			this.outcomeCohortId = value;
			return this;
		}

		public EffectEstimateStat outcomeCohortName(final String value) {
			this.outcomeCohortName = value;
			return this;
		}

		public EffectEstimateStat atRisk(final int value) {
			this.atRisk = value;
			return this;
		}

		public EffectEstimateStat casesPP(final int value) {
			this.casesPP = value;
			return this;
		}

		public EffectEstimateStat personTimePP(final Double value) {
			this.personTimePP = value;
			return this;
		}

		public EffectEstimateStat relativeRiskPP(final Double value) {
			this.relativeRiskPP = value;
			return this;
		}

		public EffectEstimateStat lb95PP(final Double value) {
			this.lb95PP = value;
			return this;
		}

		public EffectEstimateStat ub95PP(final Double value) {
			this.ub95PP = value;
			return this;
		}

		public EffectEstimateStat casesITT(final int value) {
			this.casesITT = value;
			return this;
		}

		public EffectEstimateStat personTimeITT(final Double value) {
			this.personTimeITT = value;
			return this;
		}

		public EffectEstimateStat relativeRiskITT(final Double value) {
			this.relativeRiskITT = value;
			return this;
		}

		public EffectEstimateStat lb95ITT(final Double value) {
			this.lb95ITT = value;
			return this;
		}

		public EffectEstimateStat ub95ITT(final Double value) {
			this.ub95ITT = value;
			return this;
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
		result.covariates = report.getCovariates().stream().collect(Collectors.toList());
		result.cohorts = cohorts.values().stream().collect(Collectors.toList());
		result.content = report.getContent().stream().collect(Collectors.toList());
		result.sources = report.getSources().stream().map(s -> {
			return new ReportSourceDTO(s.getSource().getId(), s.getSource().getName(), s.isActive());
		}).collect(Collectors.toList());

		return result;
	}

	private ReportDTO save(ReportDTO report) {
		return save(report, ReportStatus.DRAFT);
	}
	
	private ReportDTO save(ReportDTO report, ReportStatus status) {
		Date currentTime = Calendar.getInstance().getTime();
		Report reportEntity;

		if (report.getId() != null) {
			reportEntity = reportRepository.findOne(report.getId());
			reportEntity.setModifiedDate(currentTime);
			reportEntity.setModifiedBy(security.getSubject());
			reportEntity.setStatus(status);
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

	private String buildCovariatePrevalenceQuery(List<Long> cohorts, List<Integer> sources, List<Long> covariateIds) {

		String covaraiteIdList = StringUtils.join(covariateIds, ",");
		String cohortIdList = StringUtils.join(cohorts, ",");
		String sourceIdList = StringUtils.join(sources, ",");

		String covariateQuery = QUERY_COVARIATE_PREVALENCE_STATS;
		covariateQuery = StringUtils.replace(covariateQuery, "@covariate_id_list", covaraiteIdList);
		covariateQuery = StringUtils.replace(covariateQuery, "@cohort_id_list", cohortIdList);
		covariateQuery = StringUtils.replace(covariateQuery, "@source_id_list", sourceIdList);

		return covariateQuery;
	}
	
	private String buildCovariateDistributionQuery(List<Long> cohorts, List<Integer> sources, List<Long> covariateIds) {

		String covaraiteIdList = StringUtils.join(covariateIds, ",");
		String cohortIdList = StringUtils.join(cohorts, ",");
		String sourceIdList = StringUtils.join(sources, ",");

		String covariateQuery = QUERY_COVARIATE_DISTRIBUTION_STATS;
		covariateQuery = StringUtils.replace(covariateQuery, "@covariate_id_list", covaraiteIdList);
		covariateQuery = StringUtils.replace(covariateQuery, "@cohort_id_list", cohortIdList);
		covariateQuery = StringUtils.replace(covariateQuery, "@source_id_list", sourceIdList);

		return covariateQuery;
	}	

	private String buildIRStatQuery(List<ReportCohortPair> cohortPairs, List<Integer> sources) {

		String irStatQuery = QUERY_OUTCOME_STATS;
		List<String> pairClauses = cohortPairs.stream()
			.map(p -> {
				return String.format("(os.target_cohort_id = %d AND os.outcome_cohort_id = %d)", p.getTarget().getId(), p.getOutcome().getId());
			})
			.collect(Collectors.toList());

		irStatQuery = StringUtils.replace(irStatQuery, "@pair_clauses", StringUtils.join(pairClauses, " OR "));
		irStatQuery = StringUtils.replace(irStatQuery, "@source_id_list", StringUtils.join(sources, ","));

		return irStatQuery;
	}

	private String buildCCAStatQuery(List<ReportCohortPair> cohortPairs, List<Integer> sources) {

		String ccaQuery = QUERY_CCA_STATS;
		List<String> pairClauses = cohortPairs.stream()
			.map(p -> {
				return String.format("(cca.target_cohort_id = %d AND cca.outcome_cohort_id = %d)", p.getTarget().getId(), p.getOutcome().getId());
			})
			.collect(Collectors.toList());

		ccaQuery = StringUtils.replace(ccaQuery, "@pair_clauses", StringUtils.join(pairClauses, " OR "));
		ccaQuery = StringUtils.replace(ccaQuery, "@source_id_list", StringUtils.join(sources, ","));

		return ccaQuery;
	}

	private String buildSCCAStatQuery(List<ReportCohortPair> cohortPairs, List<Integer> sources) {

		String ccaQuery = QUERY_SCCA_STATS;
		List<String> pairClauses = cohortPairs.stream()
			.map(p -> {
				return String.format("(scca.target_cohort_id = %d AND scca.outcome_cohort_id = %d)", p.getTarget().getId(), p.getOutcome().getId());
			})
			.collect(Collectors.toList());

		ccaQuery = StringUtils.replace(ccaQuery, "@pair_clauses", StringUtils.join(pairClauses, " OR "));
		ccaQuery = StringUtils.replace(ccaQuery, "@source_id_list", StringUtils.join(sources, ","));

		return ccaQuery;
	}

	public List<PrevalenceStat> getReportCovariatePrevalence(Report report) {

		// Collect list of covariateIds
		List<Long> covariateIds = report.getCovariates().stream().filter(c -> c.getCovariateSection() != CovariateSection.DISTRIBUTIONS)
			.map(ReportCovariate::getCovariateId).collect(Collectors.toList());

		// Collect list of cohort IDs
		List<Long> cohorts = report.getCohortPairs().stream().map(rc -> rc.getTarget().getId()).distinct().collect(Collectors.toList());

		// Use the order of sources that are returned from the Report entity, but only include isActive()
		List<Integer> activeSourceIds = report.getSources().stream().filter(source -> source.isActive()).map(rs -> rs.getSource().getId()).collect(Collectors.toList());

		// Get covaraite stats from each active datasource
		String covariateQuery = buildCovariatePrevalenceQuery(cohorts, activeSourceIds, covariateIds);
		covariateQuery = SqlRender.renderSql(covariateQuery,
			new String[]{"study_results_schema"},
			new String[]{this.getStudyResultsSchema()}
		);

		String translatedSql = SqlTranslate.translateSql(covariateQuery, "sql server", this.getStudyResultsDialect(), SessionUtils.sessionId(), this.getStudyResultsSchema());
		List<PrevalenceStat> covariateStats = this.getStudyResultsJdbcTemplate().query(translatedSql, (row, rowNum) -> {
			return new PrevalenceStat(row.getString("source_key"),
				row.getInt("cohort_definition_id"),
				row.getLong("covariate_id"),
				row.getString("covariate_name"),
				row.getString("analysis_name"),
				row.getString("time_window"),
				row.getLong("count_value"),
				row.getDouble("stat_value")
			);
		});

		return covariateStats;
	}

	public List<DistributionStat> getReportCovariateDistStats(Report report) {

		// Collect list of covariateIds
		List<Long> covariateIds = report.getCovariates().stream().filter(c -> c.getCovariateSection() == CovariateSection.DISTRIBUTIONS)
			.map(ReportCovariate::getCovariateId).collect(Collectors.toList());

		// Collect list of cohort IDs
		List<Long> cohorts = report.getCohortPairs().stream().map(rc -> rc.getTarget().getId()).distinct().collect(Collectors.toList());

		// Use the order of sources that are returned from the Report entity, but only include isActive()
		List<Integer> activeSourceIds = report.getSources().stream().filter(source -> source.isActive()).map(rs -> rs.getSource().getId()).collect(Collectors.toList());

		// Get covaraite stats from each active datasource
		String covariateQuery = buildCovariateDistributionQuery(cohorts, activeSourceIds, covariateIds);
		covariateQuery = SqlRender.renderSql(covariateQuery,
			new String[]{"study_results_schema"},
			new String[]{this.getStudyResultsSchema()}
		);

		String translatedSql = SqlTranslate.translateSql(covariateQuery, "sql server", this.getStudyResultsDialect(), SessionUtils.sessionId(), this.getStudyResultsSchema());
		List<DistributionStat> covariateStats = this.getStudyResultsJdbcTemplate().query(translatedSql, (row, rowNum) -> {
			return new DistributionStat()
				.dataSource(row.getString("source_key"))
				.cohortId(row.getInt("cohort_definition_id"))
				.cohortName(row.getString("cohort_definition_name"))
				.covariateId(row.getLong("covariate_id"))
				.covariateName(row.getString("covariate_name"))
				.analysisName(row.getString("analysis_name"))
				.timeWindow(row.getString("time_window"))
				.count(Float.valueOf(row.getFloat("count_value")).longValue())
				.avg(row.getDouble("avg_value"))
				.stdev(row.getDouble("stdev_value"))
				.min(row.getDouble("min_value"))
				.p10(row.getDouble("p10_value"))
				.p25(row.getDouble("p25_value"))
				.median(row.getDouble("median_value"))
				.p75(row.getDouble("p75_value"))
				.p90(row.getDouble("p90_value"))
				.max(row.getDouble("max_value"));
		});

		return covariateStats;
	}

	
	public List<OutcomeSummaryStat> getReportIR(List<ReportCohortPair> activePairs, List<ReportSource> activeSources) {

		// Use the order of sources that are returned from the Report entity, but only include isActive()
		List<Integer> activeSourceIds = activeSources.stream().map(rs -> rs.getSource().getId()).collect(Collectors.toList());

		String outcomeQuery = buildIRStatQuery(activePairs, activeSourceIds);
		outcomeQuery = SqlRender.renderSql(outcomeQuery,
			new String[]{"study_results_schema"},
			new String[]{this.getStudyResultsSchema()}
		);

		String translatedSql = SqlTranslate.translateSql(outcomeQuery, "sql server", this.getStudyResultsDialect(), SessionUtils.sessionId(), this.getStudyResultsSchema());

		List<OutcomeSummaryStat> outcomeStats = this.getStudyResultsJdbcTemplate().query(translatedSql, (row, rowNum) -> {
			return new OutcomeSummaryStat()
				.dataSource(row.getString("source_key"))
				.targetCohortId(row.getLong("target_cohort_id"))
				.targetCohortName(row.getString("target_cohort_name"))
				.outcomeCohortId(row.getLong("outcome_cohort_id"))
				.outcomeCohortName(row.getString("outcome_cohort_name"))
				.atRiskPP(row.getInt("at_risk_pp"))
				.casesPP(row.getInt("cases_pp"))
				.personTimePP(row.getDouble("pt_pp"))
				.incidenceProportionPP(row.getDouble("ip_pp"))
				.incidenceRatePP(row.getDouble("ir_pp"))
				.casesITT(row.getInt("cases_itt"))
				.personTimeITT(row.getDouble("pt_itt"))
				.incidenceProportionITT(row.getDouble("ip_itt"))
				.incidenceRateITT(row.getDouble("ir_itt"));
		});

		return outcomeStats;
	}

	public List<EffectEstimateStat> getReportCCA(List<ReportCohortPair> activePairs, List<ReportSource> activeSources) {
		// Use the order of sources that are returned from the Report entity, but only include isActive()
		List<Integer> activeSourceIds = activeSources.stream().map(rs -> rs.getSource().getId()).collect(Collectors.toList());

		String ccaQuery = buildCCAStatQuery(activePairs, activeSourceIds);
		ccaQuery = SqlRender.renderSql(ccaQuery,
			new String[]{"study_results_schema"},
			new String[]{this.getStudyResultsSchema()}
		);

		String translatedSql = SqlTranslate.translateSql(ccaQuery, "sql server", this.getStudyResultsDialect(), SessionUtils.sessionId(), this.getStudyResultsSchema());

		List<EffectEstimateStat> ccaStats = this.getStudyResultsJdbcTemplate().query(translatedSql, (row, rowNum) -> {
			return new EffectEstimateStat()
				.dataSource(row.getString("source_key"))
				.targetCohortId(row.getLong("target_cohort_id"))
				.targetCohortName(row.getString("target_cohort_name"))
				.comparatorCohortId(row.getLong("compare_cohort_id"))
				.comparatorCohortName(row.getString("compare_cohort_name"))
				.outcomeCohortId(row.getLong("outcome_cohort_id"))
				.outcomeCohortName(row.getString("outcome_cohort_name"))
				.atRisk(row.getInt("at_risk"))
				.casesPP(row.getInt("cases_pp"))
				.personTimePP(row.getDouble("pt_pp"))
				.casesITT(row.getInt("cases_itt"))
				.personTimeITT(row.getDouble("pt_itt"))
				.relativeRiskPP(row.getDouble("relative_risk_pp"))
				.lb95PP(row.getDouble("lb_95_pp"))
				.ub95PP(row.getDouble("ub_95_pp"))
				.relativeRiskITT(row.getDouble("relative_risk_itt"))
				.lb95ITT(row.getDouble("lb_95_itt"))
				.ub95ITT(row.getDouble("ub_95_itt"));
		});
		return ccaStats;
	}

	public List<EffectEstimateStat> getReportSCCA(List<ReportCohortPair> activePairs, List<ReportSource> activeSources) {
		// Use the order of sources that are returned from the Report entity, but only include isActive()
		List<Integer> activeSourceIds = activeSources.stream().map(rs -> rs.getSource().getId()).collect(Collectors.toList());

		String sccaQuery = buildSCCAStatQuery(activePairs, activeSourceIds);
		sccaQuery = SqlRender.renderSql(sccaQuery,
			new String[]{"study_results_schema"},
			new String[]{this.getStudyResultsSchema()}
		);

		String translatedSql = SqlTranslate.translateSql(sccaQuery, "sql server", this.getStudyResultsDialect(), SessionUtils.sessionId(), this.getStudyResultsSchema());

		List<EffectEstimateStat> sccaStats = this.getStudyResultsJdbcTemplate().query(translatedSql, (row, rowNum) -> {
			return new EffectEstimateStat()
				.dataSource(row.getString("source_key"))
				.targetCohortId(row.getLong("target_cohort_id"))
				.targetCohortName(row.getString("target_cohort_name"))
				.outcomeCohortId(row.getLong("outcome_cohort_id"))
				.outcomeCohortName(row.getString("outcome_cohort_name"))
				.atRisk(row.getInt("at_risk"))
				.casesPP(row.getInt("cases_pp"))
				.personTimePP(row.getDouble("pt_pp"))
				.casesITT(row.getInt("cases_itt"))
				.personTimeITT(row.getDouble("pt_itt"))
				.relativeRiskPP(row.getDouble("relative_risk_pp"))
				.lb95PP(row.getDouble("lb_95_pp"))
				.ub95PP(row.getDouble("ub_95_pp"))
				.relativeRiskITT(row.getDouble("relative_risk_itt"))
				.lb95ITT(row.getDouble("lb_95_itt"))
				.ub95ITT(row.getDouble("ub_95_itt"));
		});

		return sccaStats;
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

	@PUT
	@Path("/{reportId}/publish")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Transactional
	public ReportDTO publishReport(ReportDTO report) {
		return save(report, ReportStatus.PUBLISHED);
	}

	@PUT
	@Path("/{reportId}/delete")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Transactional
	public ReportDTO deleteReport(ReportDTO report) {
		return save(report, ReportStatus.DELETED);
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
		jp.setIgnorePagination(Boolean.TRUE);

		// stream output to client
		StreamingOutput output = (out) -> {

			try {
				HtmlExporter exporter = new HtmlExporter();
				SimpleHtmlReportConfiguration htmlConfig = new SimpleHtmlReportConfiguration();
				htmlConfig.setEmbedImage(true);
				htmlConfig.setZoomRatio(1.75f);
				exporter.setConfiguration(htmlConfig);

				exporter.setExporterOutput(new SimpleHtmlExporterOutput(out));
				exporter.setExporterInput(new SimpleExporterInput(jp.toJasperPrint()));
				exporter.exportReport();
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
