/*
 * Copyright 2017 cknoll1.
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
package org.ohdsi.webapi.study.report;

import java.awt.Color;
import java.util.ArrayList;
import static net.sf.dynamicreports.report.builder.DynamicReports.*;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.base.expression.AbstractSimpleExpression;
import net.sf.dynamicreports.report.builder.column.TextColumnBuilder;
import net.sf.dynamicreports.report.builder.component.ImageBuilder;
import net.sf.dynamicreports.report.builder.component.VerticalListBuilder;
import net.sf.dynamicreports.report.builder.crosstab.CrosstabBuilder;
import net.sf.dynamicreports.report.builder.crosstab.CrosstabColumnGroupBuilder;
import net.sf.dynamicreports.report.builder.crosstab.CrosstabMeasureBuilder;
import net.sf.dynamicreports.report.builder.crosstab.CrosstabRowGroupBuilder;
import net.sf.dynamicreports.report.builder.grid.ColumnTitleGroupBuilder;
import net.sf.dynamicreports.report.builder.style.StyleBuilder;
import net.sf.dynamicreports.report.constant.Calculation;
import net.sf.dynamicreports.report.constant.HorizontalTextAlignment;
import net.sf.dynamicreports.report.constant.LineStyle;
import net.sf.dynamicreports.report.constant.Markup;
import net.sf.dynamicreports.report.constant.VerticalTextAlignment;
import net.sf.dynamicreports.report.definition.ReportParameters;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.apache.commons.lang3.StringUtils;
import org.ohdsi.webapi.service.StudyReportService;
import org.ohdsi.webapi.service.StudyReportService.PrevalenceStat;
import org.ohdsi.webapi.service.StudyReportService.DistributionStat;
import org.ohdsi.webapi.study.StudyCohort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 *
 * @author cknoll1
 */
@Component("studyReportManager")
@Scope("prototype")
public class StudyReportManager {

	@Autowired
	private StudyReportService studyReportService;
	
	@Value("${studyresults.resourcepath}") 
	private String resourcePath;

	private StyleBuilder titleStyle;
	private StyleBuilder subTitleStyle;
	private StyleBuilder headingStyle;
	private StyleBuilder subHeadingStyle;
	
	private StyleBuilder columnHeaderStyle;
	private StyleBuilder groupHeaderStyle;
	private StyleBuilder columnStyle;
	private StyleBuilder columnBorderlessStyle;
	private StyleBuilder columnStyleSmall;
	private StyleBuilder crosstabCellStyle;
	private StyleBuilder crosstabRowStyle;

	private StyleBuilder footnoteStyle;
	

	private class OutcomeGroupExpression extends AbstractSimpleExpression<String> {
		
		public OutcomeGroupExpression() {
		}
		
		@Override
		public String evaluate(ReportParameters reportParameters) {
			return String.format("%s - %s", 
				reportParameters.getValue("targetCohortName"), 
				reportParameters.getValue("outcomeCohortName"));
		}
	}
	
	private class CIExpression extends AbstractSimpleExpression<String> {
		@Override
		public String evaluate(ReportParameters reportParameters) {
			java.text.DecimalFormat df = new java.text.DecimalFormat("#.00");
			Double rr = reportParameters.getValue("relativeRisk");
			Double lb95 = reportParameters.getValue("lb95");
			Double ub95 = reportParameters.getValue("ub95");
			return String.format("%s (%s - %s)", df.format(rr), df.format(lb95), df.format(ub95));
		}   		
	}

	private class PSImageExpression extends AbstractSimpleExpression<String> {
		@Override
		public String evaluate(ReportParameters reportParameters) {
			return String.format("%s/%s", resourcePath, "sample/preference_score.png");
		}   		
	}	
	
	
	private class CovariateBalanceImageExpression extends AbstractSimpleExpression<String> {
		@Override
		public String evaluate(ReportParameters reportParameters) {
			return String.format("%s/%s", resourcePath, "sample/covariate_balance.png");
		}   		
	}	

	private class CalibrationImageExpression extends AbstractSimpleExpression<String> {
		@Override
		public String evaluate(ReportParameters reportParameters) {
			return String.format("%s/%s", resourcePath, "sample/calibration_plot.png");
		}   		
	}
	
	public static class OutcomeGroupBy {
		public String targetCohortName;
		public String outcomeCohortName;

		public OutcomeGroupBy(String targetCohortName, String outcomeCohortName) {
			this.targetCohortName = targetCohortName;
			this.outcomeCohortName = outcomeCohortName;
		}

		@Override
		public int hashCode() {
			int hash = 7;
			hash = 53 * hash + Objects.hashCode(this.targetCohortName);
			hash = 53 * hash + Objects.hashCode(this.outcomeCohortName);
			return hash;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			final OutcomeGroupBy other = (OutcomeGroupBy) obj;
			if (!Objects.equals(this.targetCohortName, other.targetCohortName)) {
				return false;
			}
			if (!Objects.equals(this.outcomeCohortName, other.outcomeCohortName)) {
				return false;
			}
			return true;
		}
		
		
	}
		
	
	private static class RelativeRiskGroupBy {
		public String targetCohortName;
		public String comparatorCohortName;
		public String outcomeCohortName;

		public RelativeRiskGroupBy(String targetCohortName, String comparatorCohortName, String outcomeCohortName) {
			this.targetCohortName = targetCohortName;
			this.comparatorCohortName = comparatorCohortName;
			this.outcomeCohortName = outcomeCohortName;
		}

		@Override
		public int hashCode() {
			int hash = 3;
			hash = 59 * hash + Objects.hashCode(this.targetCohortName);
			hash = 59 * hash + Objects.hashCode(this.comparatorCohortName);
			hash = 59 * hash + Objects.hashCode(this.outcomeCohortName);
			return hash;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			final RelativeRiskGroupBy other = (RelativeRiskGroupBy) obj;
			if (!Objects.equals(this.targetCohortName, other.targetCohortName)) {
				return false;
			}
			if (!Objects.equals(this.comparatorCohortName, other.comparatorCohortName)) {
				return false;
			}
			if (!Objects.equals(this.outcomeCohortName, other.outcomeCohortName)) {
				return false;
			}
			return true;
		}
	}
	
	public static class RelativeRiskRow {
		private String dataSource;
		private Long targetCohortId;
		private String targetCohortName;
		private Long comparatorCohortId;
		private String comparatorCohortName;
		private Long outcomeCohortId;
		private String outcomeCohortName;
		private int atRisk;
		private int cases;
		private Double personTime;
		private Double relativeRisk;
		private Double lb95;
		private Double ub95;

		public RelativeRiskRow() {
		}

		public String getDataSource() {
			return dataSource;
		}

		public void setDataSource(String dataSource) {
			this.dataSource = dataSource;
		}

		public Long getTargetCohortId() {
			return targetCohortId;
		}

		public void setTargetCohortId(Long targetCohortId) {
			this.targetCohortId = targetCohortId;
		}

		public String getTargetCohortName() {
			return targetCohortName;
		}

		public void setTargetCohortName(String targetCohortName) {
			this.targetCohortName = targetCohortName;
		}

		public Long getComparatorCohortId() {
			return comparatorCohortId;
		}

		public void setComparatorCohortId(Long comparatorCohortId) {
			this.comparatorCohortId = comparatorCohortId;
		}

		public String getComparatorCohortName() {
			return comparatorCohortName;
		}

		public void setComparatorCohortName(String comparatorCohortName) {
			this.comparatorCohortName = comparatorCohortName;
		}

		public Long getOutcomeCohortId() {
			return outcomeCohortId;
		}

		public void setOutcomeCohortId(Long outcomeCohortId) {
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

		public int getCases() {
			return cases;
		}

		public void setCases(int cases) {
			this.cases = cases;
		}

		public Double getPersonTime() {
			return personTime;
		}

		public void setPersonTime(Double personTime) {
			this.personTime = personTime;
		}

		public Double getRelativeRisk() {
			return relativeRisk;
		}

		public void setRelativeRisk(Double relativeRisk) {
			this.relativeRisk = relativeRisk;
		}

		public Double getLb95() {
			return lb95;
		}

		public void setLb95(Double lb95) {
			this.lb95 = lb95;
		}

		public Double getUb95() {
			return ub95;
		}

		public void setUb95(Double ub95) {
			this.ub95 = ub95;
		}

		public RelativeRiskRow dataSource(final String value) {
			this.dataSource = value;
			return this;
		}

		public RelativeRiskRow targetCohortId(final Long value) {
			this.targetCohortId = value;
			return this;
		}

		public RelativeRiskRow targetCohortName(final String value) {
			this.targetCohortName = value;
			return this;
		}

		public RelativeRiskRow comparatorCohortId(final Long value) {
			this.comparatorCohortId = value;
			return this;
		}

		public RelativeRiskRow comparatorCohortName(final String value) {
			this.comparatorCohortName = value;
			return this;
		}

		public RelativeRiskRow outcomeCohortId(final Long value) {
			this.outcomeCohortId = value;
			return this;
		}

		public RelativeRiskRow outcomeCohortName(final String value) {
			this.outcomeCohortName = value;
			return this;
		}

		public RelativeRiskRow atRisk(final int value) {
			this.atRisk = value;
			return this;
		}

		public RelativeRiskRow cases(final int value) {
			this.cases = value;
			return this;
		}

		public RelativeRiskRow personTime(final Double value) {
			this.personTime = value;
			return this;
		}

		public RelativeRiskRow relativeRisk(final Double value) {
			this.relativeRisk = value;
			return this;
		}

		public RelativeRiskRow lb95(final Double value) {
			this.lb95 = value;
			return this;
		}

		public RelativeRiskRow ub95(final Double value) {
			this.ub95 = value;
			return this;
		}
		
	}
	
	private void initStyles() {

		StyleBuilder rootStyle = stl.style(1).setName("default").setFont(stl.fontArial()).setFontSize(10).setPadding(2).setMarkup(Markup.STYLED);
		
		titleStyle = stl.style(rootStyle).setName("title").setBold(Boolean.TRUE).setFontSize(14);
		subTitleStyle = stl.style(titleStyle).setName("subTitle").setFontSize(12);

		headingStyle = stl.style(rootStyle).setName("heading").setFontSize(12);
		subHeadingStyle = stl.style(headingStyle).setName("subHeading").setFontSize(9).setPadding(stl.padding().setTop(0).setBottom(0));
		
		columnStyle = stl.style(rootStyle).setName("column").setHorizontalTextAlignment(HorizontalTextAlignment.RIGHT)
			.setVerticalTextAlignment(VerticalTextAlignment.MIDDLE)
			.setBorder(stl.pen1Point());

		columnStyleSmall = stl.style(columnStyle).setName("columnSmall")
			.setFont(stl.fontArial().setFontSize(8));

		columnBorderlessStyle = stl.style(columnStyleSmall).setName("columnBorderless").setBorder(stl.pen(0.0f, LineStyle.SOLID));

		columnHeaderStyle = stl.style(columnStyle).setName("columnHeader").setHorizontalTextAlignment(HorizontalTextAlignment.CENTER)
			.setVerticalTextAlignment(VerticalTextAlignment.MIDDLE)
			.setBackgroundColor(Color.LIGHT_GRAY);

		groupHeaderStyle = stl.style(rootStyle).setHorizontalTextAlignment(HorizontalTextAlignment.LEFT)
			.setVerticalTextAlignment(VerticalTextAlignment.MIDDLE)
			.setBold(true)
			.setTopPadding(5);
		
		crosstabCellStyle = stl.style(columnStyle).setName("ctCell")
			.setVerticalTextAlignment(VerticalTextAlignment.TOP)
			.setLeftIndent(4)
			.setRightIndent(4);
		
		crosstabRowStyle = stl.style(crosstabCellStyle).setName("ctRow").setMarkup(Markup.STYLED).setHorizontalTextAlignment(HorizontalTextAlignment.LEFT);

		footnoteStyle = stl.style(rootStyle).setName("footnote").setFontSize(8).setMarkup(Markup.STYLED);
		
	}

	public StudyReportManager() {
		initStyles();
	}

	private CrosstabBuilder getCovariateCrossTab() throws Exception {

		CrosstabRowGroupBuilder<String> rowGroup = ctab.rowGroup("name", String.class)
			.setShowTotal(false)
			.setHeaderStyle(crosstabRowStyle)
			.setHeaderWidth(140);
		
		CrosstabColumnGroupBuilder<String> columnGroup = ctab.columnGroup("dataSource", String.class)
			.setShowTotal(false)
			.setHeaderStyle(columnHeaderStyle);

		CrosstabMeasureBuilder<Double> statValueMeasure = ctab.measure("%", "statValue", Double.class, Calculation.NOTHING);
		statValueMeasure.setPattern("#0.00%");
		CrosstabMeasureBuilder<Long> countMeasure = ctab.measure("N", "count", Long.class, Calculation.NOTHING);
		countMeasure.setPattern("#,###");
		
		statValueMeasure.setStyle(crosstabCellStyle);
		countMeasure.setStyle(crosstabCellStyle);
		
		CrosstabBuilder crosstab = ctab.crosstab()
			.rowGroups(rowGroup)
			.columnGroups(columnGroup)
			.measures(countMeasure, statValueMeasure)
			.setCellWidth(120);
		return crosstab;
	}

	private JasperReportBuilder getCovariateDistReport() {
		
		// rendering of reports goes cohort
		TextColumnBuilder<String> dataSourceCol = col.column("Data Source", "dataSource", type.stringType());
		//TextColumnBuilder<String> covariateCol = col.column("Covariate", "covariateName", type.stringType());
		TextColumnBuilder<Long> countCol = col.column("N", "count", type.longType());
		TextColumnBuilder<Double> avgCol = col.column("AVG", "avg", type.doubleType());
		TextColumnBuilder<Double> stdevCol = col.column("STDEV", "stdev", type.doubleType());
		TextColumnBuilder<Double> minCol = col.column("MIN", "min", type.doubleType());
		TextColumnBuilder<Double> p10Col = col.column("P10", "p10", type.doubleType());
		TextColumnBuilder<Double> p25Col = col.column("P25", "p25", type.doubleType());
		TextColumnBuilder<Double> medianCol = col.column("Median", "median", type.doubleType());
		TextColumnBuilder<Double> p75Col = col.column("P75", "p75", type.doubleType());
		TextColumnBuilder<Double> p90Col = col.column("P90", "p90", type.doubleType());
		TextColumnBuilder<Double> maxCol = col.column("MAX", "max", type.doubleType());

		JasperReportBuilder distReport = report()
			.columns(dataSourceCol, countCol, avgCol, stdevCol, minCol, p10Col, p25Col, medianCol, p75Col, p90Col, maxCol)
			.groupBy(grp.group("covariateName", String.class))
			.setColumnStyle(this.columnStyleSmall)
			.setColumnTitleStyle(this.columnHeaderStyle)
			.setGroupStyle(this.groupHeaderStyle);
		return distReport;		
	}
	
	private JasperReportBuilder getIRReport() {
		// rendering of reports goes cohort
		TextColumnBuilder<String> dataSourceCol = col.column("Data Source", "dataSource", type.stringType());
		TextColumnBuilder<Integer> atRiskCol = col.column("At Risk", "atRiskPP", type.integerType()).setFixedWidth(125);
		TextColumnBuilder<Integer> casesPPCol = col.column("Cases", "casesPP", type.integerType()).setFixedWidth(50);
		// TextColumnBuilder<Double> personTimePPCol = col.column("TAR", "personTimePP", type.doubleType()).setPattern("#,##0.0");
		TextColumnBuilder<Double> proportionPPCol = col.column("IP", "incidenceProportionPP", type.doubleType()).setFixedWidth(50).setPattern("#0.00");
		//TextColumnBuilder<Double> ratePPCol = col.column("IR", "incidenceRatePP",type.doubleType()).setFixedWidth(30).setPattern("#0.00");
		TextColumnBuilder<Integer> casesITTCol = col.column("Cases", "casesITT", type.integerType()).setFixedWidth(50);
		//TextColumnBuilder<Double> personTimeITTCol = col.column("TAR", "personTimeITT", type.doubleType()).setPattern("#,##0.0");
		TextColumnBuilder<Double> proportionITTCol = col.column("IP", "incidenceProportionITT", type.doubleType()).setFixedWidth(50).setPattern("#0.00");
		//TextColumnBuilder<Double> rateITTCol = col.column("IR", "incidenceRateITT",type.doubleType()).setFixedWidth(30).setPattern("#0.00");
		
//		ColumnTitleGroupBuilder ppGroup = grid.titleGroup("Per Protocol", casesPPCol,personTimePPCol,proportionPPCol, ratePPCol).setTitleFixedWidth(180);
//		ColumnTitleGroupBuilder ittGroup = grid.titleGroup("Intent to Treat", casesITTCol,personTimeITTCol,proportionITTCol, rateITTCol).setTitleFixedWidth(180);

		ColumnTitleGroupBuilder ppGroup = grid.titleGroup("Per Protocol", casesPPCol,proportionPPCol).setTitleFixedWidth(100);
		ColumnTitleGroupBuilder ittGroup = grid.titleGroup("Intent to Treat", casesITTCol,proportionITTCol).setTitleFixedWidth(100);

		JasperReportBuilder irReport = report()
			.fields(
				field("targetCohortName", String.class),
				field("outcomeCohortName", String.class)
			)
			.columnGrid(dataSourceCol, atRiskCol, ppGroup, ittGroup)
			.columns(dataSourceCol,atRiskCol,
//				casesPPCol,personTimePPCol,proportionPPCol, ratePPCol,
//				casesITTCol,personTimeITTCol,proportionITTCol, rateITTCol
				casesPPCol,proportionPPCol, 
				casesITTCol,proportionITTCol
			)
			.setColumnStyle(this.columnStyleSmall)
			.setColumnTitleStyle(this.columnHeaderStyle)
			.setGroupStyle(this.groupHeaderStyle);
		
		return irReport;
	}
	
	private JasperReportBuilder getRelativeRiskReport() {

		// rendering of reports goes cohort
		TextColumnBuilder<String> dataSourceCol = col.column("Data Source", "dataSource", type.stringType());
		TextColumnBuilder<Integer> atRiskCol = col.column("At Risk", "atRisk", type.integerType()).setFixedWidth(50);
		TextColumnBuilder<Integer> casesCol = col.column("Cases", "cases", type.integerType()).setFixedWidth(50);
		TextColumnBuilder<Double> personTimeCol = col.column("TAR", "personTime", type.doubleType()).setPattern("#,##0.0").setFixedWidth(75);
		TextColumnBuilder<Double> relativeRiskCol = col.column("RR", "relativeRisk",type.doubleType()).setFixedWidth(35).setPattern("#0.00");
		TextColumnBuilder<String> ciCol = col.column("95% CI", new CIExpression()).setFixedWidth(75);
		
		JasperReportBuilder ccaReport = report()
			.fields(
				field("lb95", Double.class),
				field("ub95", Double.class)
			)
			.columns(dataSourceCol,atRiskCol, casesCol,personTimeCol, relativeRiskCol, ciCol)
			.setColumnStyle(this.columnStyleSmall)
			.setColumnTitleStyle(this.columnHeaderStyle)
		;

		return ccaReport;
	}
	
	private JasperReportBuilder getCCADiagnosticsReport() {

		ImageBuilder psImage = cmp.image(new PSImageExpression());
		ImageBuilder cbImage = cmp.image(new CovariateBalanceImageExpression());
		ImageBuilder calibrationImage = cmp.image(new CalibrationImageExpression());

		TextColumnBuilder<String> dataSourceCol = col.column("Data Source", "dataSource", type.stringType());
		JasperReportBuilder ccaDiagnosticsReport = report()
			.columns(dataSourceCol,
				col.componentColumn("Preference Score", psImage).setStyle(columnBorderlessStyle),
				col.componentColumn("Covariate Balance", cbImage).setStyle(columnBorderlessStyle),
				col.componentColumn("Calibration", calibrationImage).setStyle(columnBorderlessStyle))
			.setColumnStyle(this.columnBorderlessStyle)
			.setColumnTitleStyle(this.columnHeaderStyle);
		
		return ccaDiagnosticsReport;
	}
	
	private JasperReportBuilder getSCCADiagnosticsReport() {

		ImageBuilder calibrationImage = cmp.image(new CalibrationImageExpression());

		TextColumnBuilder<String> dataSourceCol = col.column("Data Source", "dataSource", type.stringType());
		JasperReportBuilder sccaDiagnosticsReport = report()
			.columns(dataSourceCol,
				col.componentColumn("Calibration", calibrationImage).setStyle(columnBorderlessStyle))
			.setColumnStyle(this.columnBorderlessStyle)
			.setColumnTitleStyle(this.columnHeaderStyle);
		
		return sccaDiagnosticsReport;
	}
	
	public JasperReportBuilder getMainReport(Report report) throws Exception {

		// create container report to host embedded report sections
		JasperReportBuilder mainReport = report();
		int tableIndex = 1;
		
		mainReport.setPageMargin(margin(40));

		VerticalListBuilder contentBuilder = cmp.verticalList().setGap(10);

		contentBuilder.add(cmp.text(String.format("I. Cohort Characterization")).setStyle(this.headingStyle));

		// find active cohort pairs
		List<ReportCohortPair> activePairs = report.getCohortPairs().stream().filter(ReportCohortPair::isActive).collect(Collectors.toList());

		// create cohort lookup id->cohort
		Map<Long, StudyCohort> cohortLookup = new HashMap<>();
		activePairs.forEach(p -> {
			if (!cohortLookup.containsKey(p.getTarget().getId()))
				cohortLookup.put(p.getTarget().getId(), p.getTarget());
			if (!cohortLookup.containsKey(p.getOutcome().getId()))
				cohortLookup.put(p.getOutcome().getId(), p.getOutcome());
		});
		
		// get active datasources and create a lookup to resolve source key to order
		List<ReportSource> activeSources = report.getSources().stream().filter(s -> s.isActive()).collect(Collectors.toList());
		Map<String, Integer> sourceLookup = IntStream.range(0, activeSources.size()).boxed()
			.collect(Collectors.toMap(i -> activeSources.get(i).getSource().getKey(), i -> i));

		/* BEGIN: Covariate reports */
		
		// create map and comparator for each type of covariates to sort the data for crosstabs
		// separate Maps/Comparators are needed because the raw data does not have the covariate section 
		// nor the ordering the data should appear
		
		Map<Long,Integer> demographicCovariateLookup = new HashMap<>();
		Map<Long,Integer> conditionCovariateLookup = new HashMap<>();
		Map<Long,Integer> drugCovariateLookup = new HashMap<>();
		Map<Long,Integer> procedureCovariateLookup = new HashMap<>();
		Map<Long,Integer> measureCovariateLookup = new HashMap<>();
		Map<Long, Integer> distCovariateLookup = new HashMap<>();
		
		IntStream.range(0, report.getCovariates().size())
			.forEach(i -> {
				ReportCovariate rc = report.getCovariates().get(i);
				switch (rc.getCovariateSection())
				{
					case DEMOGRAPHICS:
						demographicCovariateLookup.put(rc.getCovariateId(), i);
						break;
					case CONDITIONS:
						conditionCovariateLookup.put(rc.getCovariateId(), i);
						break;
					case DRUGS:
						drugCovariateLookup.put(rc.getCovariateId(), i);
						break;
					case PROCEDURES:
						procedureCovariateLookup.put(rc.getCovariateId(), i);
						break;
					case MEASUREMENTS:
						measureCovariateLookup.put(rc.getCovariateId(), i);
						break;
					case DISTRIBUTIONS:
						distCovariateLookup.put(rc.getCovariateId(), i);
						break;
				}
			});
		
		// Comparators

		Comparator<PrevalenceStat> demographicComparator = Comparator.comparing(cs -> demographicCovariateLookup.get(cs.getCovariateId()));
		demographicComparator = demographicComparator.thenComparing(cs -> sourceLookup.get(cs.getDataSource()));
		
		Comparator<PrevalenceStat> conditionComparator = Comparator.comparing(cs -> conditionCovariateLookup.get(cs.getCovariateId()));
		conditionComparator = conditionComparator.thenComparing(cs -> sourceLookup.get(cs.getDataSource()));
		
		Comparator<PrevalenceStat> drugComparator = Comparator.comparing(cs -> drugCovariateLookup.get(cs.getCovariateId()));
		drugComparator = drugComparator.thenComparing(cs -> sourceLookup.get(cs.getDataSource()));
		
		Comparator<PrevalenceStat> procedureComparator = Comparator.comparing(cs -> procedureCovariateLookup.get(cs.getCovariateId()));
		procedureComparator = procedureComparator.thenComparing(cs -> sourceLookup.get(cs.getDataSource()));

		Comparator<PrevalenceStat> measureComparator = Comparator.comparing(cs -> measureCovariateLookup.get(cs.getCovariateId()));
		measureComparator = measureComparator.thenComparing(cs -> sourceLookup.get(cs.getDataSource()));
		
		Comparator<DistributionStat> distComparator = Comparator.comparing(cs -> distCovariateLookup.get(cs.getCovariateId()));
		distComparator = distComparator.thenComparing(cs -> sourceLookup.get(cs.getDataSource()));
		
		List<PrevalenceStat> prevalenceStats = studyReportService.getReportCovariatePrevalence(report);
		
		// we don't split the distribution stats into separate lists, so we can sort thie list once after fetch
		List<DistributionStat> distributionStats = studyReportService.getReportCovariateDistStats(report);
		distributionStats.sort(distComparator);
		
		for (ReportCohortPair cohortPair : activePairs) {
			contentBuilder.add(cmp.text(String.format("<b>Cohort:</b> %s", cohortPair.getTarget().getName())).setStyle(this.headingStyle));
				
			// Prevalence Stats

			CrosstabBuilder crosstab;
			JasperReportBuilder subReport;

			// For each section, create list of covariates to pass into crosstab, 
			// which must be sorted by the covariate ordinal, then by datasource ordinal
			
			// DEMOGRAPHICS
			List<PrevalenceStat> demographicStats = prevalenceStats.stream()
				.filter(cs -> demographicCovariateLookup.containsKey(cs.getCovariateId()) && cs.getCohortId() == cohortPair.getTarget().getId())
				.sorted(demographicComparator)
				.collect(Collectors.toList());

			crosstab = getCovariateCrossTab()
				.setDataSource(new JRBeanCollectionDataSource(demographicStats))
				.setDataPreSorted(true);

			subReport = report()
				.title(cmp.text(String.format("Table %da. Demographics", tableIndex)).setStyle(this.headingStyle))
				.summary(demographicStats.size() > 0 ? crosstab : cmp.text("No demographic covariates selected.").setStyle(this.subHeadingStyle));

			contentBuilder.add(cmp.subreport(subReport));
			
			// Conditions
			List<PrevalenceStat> conditionStats = prevalenceStats.stream()
				.filter(cs -> conditionCovariateLookup.containsKey(cs.getCovariateId()) && cs.getCohortId() == cohortPair.getTarget().getId())
				.sorted(conditionComparator)
				.collect(Collectors.toList());

			FootnoteManager conditionFootnoteManager = new FootnoteManager();
			String conditionsDefaultAnalysis = "Condition occurrence record for the verbatim concept observed during 365d on or prior to cohort index";
			conditionStats.forEach(s -> {
				if (!conditionsDefaultAnalysis.equals(s.getAnalysisName())) {
					s.setName(String.format("%s<sup>%s</sup>", s.getName(), conditionFootnoteManager.getFootnoteSymbol(s.getAnalysisName())));
				}
			});
			String conditionFootnotes = StringUtils.join(conditionFootnoteManager.getFootnotes().stream().map(f -> {
				return String.format("%s: %s", f.glyph, f.footnote);
			}).collect(Collectors.toList()),"\n");
			
			String footnoteText = "Note: using condition occurrence for the verbatim concept observed during 365d on or prior to cohort index";
			footnoteText = footnoteText + ((conditionFootnotes.length() > 0) ? " unless noted below:\n" + conditionFootnotes : ".");
			
			crosstab = getCovariateCrossTab()
				.setDataSource(new JRBeanCollectionDataSource(conditionStats))
				.setDataPreSorted(true);

			subReport = report()
				.title(cmp.text(String.format("Table %db. Conditions", tableIndex)).setStyle(this.headingStyle))
				.summary(conditionStats.size() > 0 ? cmp.verticalList(crosstab, 
					cmp.text(footnoteText).setStyle(this.footnoteStyle)).setGap(3)
					: cmp.text("No condition covariates selected.").setStyle(this.subHeadingStyle));
			
			contentBuilder.add(cmp.subreport(subReport));
			
			// Drugs
			List<PrevalenceStat> drugStats = prevalenceStats.stream()
				.filter(cs -> drugCovariateLookup.containsKey(cs.getCovariateId()) && cs.getCohortId() == cohortPair.getTarget().getId())
				.sorted(drugComparator)
				.collect(Collectors.toList());

			FootnoteManager drugFootnoteManager = new FootnoteManager();
			String drugDefaultAnalysis = "Drug exposure record for the verbatim concept observed during 365d on or prior to cohort index";
			drugStats.forEach(s -> {
				if (!drugDefaultAnalysis.equals(s.getAnalysisName())) {
					s.setName(String.format("%s<sup>%s</sup>", s.getName(), drugFootnoteManager.getFootnoteSymbol(s.getAnalysisName())));
				}
			});
			String drugFootnotes = StringUtils.join(drugFootnoteManager.getFootnotes().stream().map(f -> {
				return String.format("%s: %s", f.glyph, f.footnote);
			}).collect(Collectors.toList()),"\n");
			
			String drugFootnoteText = "Note: using drug exposure record for the verbatim concept observed during 365d on or prior to cohort index";
			drugFootnoteText = drugFootnoteText + ((drugFootnotes.length() > 0) ? " unless noted below:\n" + drugFootnotes : ".");
			
			crosstab = getCovariateCrossTab()
				.setDataSource(new JRBeanCollectionDataSource(drugStats))
				.setDataPreSorted(true);

			subReport = report()
				.title(cmp.text(String.format("Table %dc. Drugs", tableIndex)).setStyle(this.headingStyle))
				.summary(drugStats.size() > 0 ? cmp.verticalList(crosstab, 
					cmp.text(drugFootnoteText).setStyle(this.footnoteStyle)).setGap(3)
					: cmp.text("No drug covariates selected.").setStyle(this.subHeadingStyle));

			contentBuilder.add(cmp.subreport(subReport));
			
			// Procedures
			List<PrevalenceStat> procedureStats = prevalenceStats.stream()
				.filter(cs -> procedureCovariateLookup.containsKey(cs.getCovariateId()) && cs.getCohortId() == cohortPair.getTarget().getId())
				.sorted(procedureComparator)
				.collect(Collectors.toList());

			FootnoteManager procedureFootnoteManager = new FootnoteManager();
			String procedureDefaultAnalysis = "Procedure occurrence record for the verbatim concept observed during 365d on or prior to cohort index";
			procedureStats.forEach(s -> {
				if (!procedureDefaultAnalysis.equals(s.getAnalysisName())) {
					s.setName(String.format("%s<sup>%s</sup>", s.getName(), procedureFootnoteManager.getFootnoteSymbol(s.getAnalysisName())));
				}
			});
			String procedureFootnotes = StringUtils.join(procedureFootnoteManager.getFootnotes().stream().map(f -> {
				return String.format("%s: %s", f.glyph, f.footnote);
			}).collect(Collectors.toList()),"\n");
			
			String procedureFootnoteText = "Note: using procedure occurrence record for the verbatim concept observed during 365d on or prior to cohort index";
			procedureFootnoteText = procedureFootnoteText + ((procedureFootnotes.length() > 0) ? " unless noted below:\n" + procedureFootnotes : ".");

			crosstab = getCovariateCrossTab()
				.setDataSource(new JRBeanCollectionDataSource(procedureStats))
				.setDataPreSorted(true);

			subReport = report()
				.title(cmp.text(String.format("Table %dd. Procedures", tableIndex)).setStyle(this.headingStyle))
				.summary(procedureStats.size() > 0 ? cmp.verticalList(crosstab, 
					cmp.text(procedureFootnoteText).setStyle(this.footnoteStyle)).setGap(3)
					: cmp.text("No procedure covariates selected.").setStyle(this.subHeadingStyle));
			
			contentBuilder.add(cmp.subreport(subReport));

			// Measurements
			List<PrevalenceStat> measureStats = prevalenceStats.stream()
				.filter(cs -> measureCovariateLookup.containsKey(cs.getCovariateId()) && cs.getCohortId() == cohortPair.getTarget().getId())
				.sorted(measureComparator)
				.collect(Collectors.toList());

			FootnoteManager measurementFootnoteManager = new FootnoteManager();
			String measurementDefaultAnalysis = "Measurement record for the verbatim concept observed during 365d on or prior to cohort index";
			measureStats.forEach(s -> {
				if (!measurementDefaultAnalysis.equals(s.getAnalysisName())) {
					s.setName(String.format("%s<sup>%s</sup>", s.getName(), measurementFootnoteManager.getFootnoteSymbol(s.getAnalysisName())));
				}
			});
			String measurementFootnotes = StringUtils.join(measurementFootnoteManager.getFootnotes().stream().map(f -> {
				return String.format("%s: %s", f.glyph, f.footnote);
			}).collect(Collectors.toList()),"\n");
			
			String measurementFootnoteText = "Note: using measurement record for the verbatim concept observed during 365d on or prior to cohort index";
			measurementFootnoteText = measurementFootnoteText + ((measurementFootnotes.length() > 0) ? " unless noted below:\n" + measurementFootnotes : ".");
			
			crosstab = getCovariateCrossTab()
				.setDataSource(new JRBeanCollectionDataSource(measureStats))
				.setDataPreSorted(true);

			subReport = report()
				.title(cmp.text(String.format("Table %de. Measurements", tableIndex)).setStyle(this.headingStyle))
				.summary(measureStats.size() > 0 ? cmp.verticalList(crosstab, 
					cmp.text(measurementFootnoteText).setStyle(this.footnoteStyle)).setGap(3)
					: cmp.text("No measurement covariates selected.").setStyle(this.subHeadingStyle));

			contentBuilder.add(cmp.subreport(subReport));

			// Distribution Stats
			List<DistributionStat> cohortDistStats = distributionStats.stream()
				.filter(cs -> cs.getCohortId() == cohortPair.getTarget().getId())
				.collect(Collectors.toList());
			
			JasperReportBuilder distReport = getCovariateDistReport()
				.title(cmp.text(String.format("Table %df. Covariate Distribution", tableIndex)).setStyle(this.headingStyle))
				.setDataSource(cohortDistStats);
			
			contentBuilder.add(cmp.subreport(distReport));
			
			tableIndex++;
			
		}
		/* END: Covariate reports */

		/* BEGIN: IR reports */
		
		contentBuilder.add(cmp.text(String.format("II. Outcome Summary Results")).setStyle(this.headingStyle));
		
		// outcome comparator: by pair index then by datasource index
		Comparator<StudyReportService.OutcomeSummaryStat> outcomeComparator = 
			Comparator.comparing(oss -> {
				ReportCohortPair targetPair = activePairs.stream().filter(p -> { 
					return (p.getTarget().getId() == oss.getTargetCohortId() && p.getOutcome().getId() == oss.getOutcomeCohortId());
				}).findFirst().get();
				return activePairs.indexOf(targetPair);
			});
		outcomeComparator = outcomeComparator.thenComparing(oss -> sourceLookup.get(oss.getDataSource()));

		// get all outcome statistics across all active databases, and sort
		List<StudyReportService.OutcomeSummaryStat> outcomeStats = studyReportService.getReportIR(activePairs, activeSources);
		outcomeStats.sort(outcomeComparator);

		// group outcome stats by OutcomeGroupBy
		Map<OutcomeGroupBy, List<StudyReportService.OutcomeSummaryStat>> outcomeGroups = outcomeStats.stream()
			.collect(Collectors.groupingBy(r -> new OutcomeGroupBy(r.getTargetCohortName(), r.getOutcomeCohortName()),LinkedHashMap::new, Collectors.toList()));

		char outcomeSubTableIndex = 'a';
		for (OutcomeGroupBy key : outcomeGroups.keySet()) {
			// Get report
			JasperReportBuilder irReport = getIRReport();
			irReport.title(cmp.verticalList(
				cmp.text(String.format("Table %d%s. Outcome Summary", tableIndex, outcomeSubTableIndex)).setStyle(headingStyle),
				cmp.text(String.format("<b>Target:</b> %s", key.targetCohortName)).setStyle(subHeadingStyle),
				cmp.text(String.format("<b>0utcome:</b> %s", key.outcomeCohortName)).setStyle(subHeadingStyle)
			).setGap(0))
				.setDataSource(outcomeGroups.get(key));
			
			contentBuilder.add(cmp.subreport(irReport));
			outcomeSubTableIndex++;
		}
		tableIndex++;
		
		/* END: IR reports */
		
		/* BEGIN: Effect Estimate reports */

		// effect estimate comparator: by pair index then by datasource index
		// find the ReportCohortPair's index based on the ees record's targetCohortId, OutcomeCohortId, and use that index value as the sort value.
		Comparator<StudyReportService.EffectEstimateStat> eeComparator = 
			Comparator.comparing(ees -> {
				ReportCohortPair targetPair = activePairs.stream().filter(p -> { 
					return (p.getTarget().getId() == ees.getTargetCohortId() && p.getOutcome().getId() == ees.getOutcomeCohortId());
				}).findFirst().get();
				return activePairs.indexOf(targetPair);
			});
		// then use the datasource index as the next comparator
		eeComparator = eeComparator.thenComparing(ees -> sourceLookup.get(ees.getDataSource()));
		// finally order by comparator name
		eeComparator = eeComparator.thenComparing(ees -> ees.getComparatorCohortName());
		
		// CCA Reports
		 
		contentBuilder.add(cmp.text("III. Cohort Comparision Anaylsis Results").setStyle(this.headingStyle));

		// get all outcome statistics across all active databases, and sort
		List<StudyReportService.EffectEstimateStat> ccaStats = studyReportService.getReportCCA(activePairs, activeSources);
		ccaStats.sort(eeComparator);
		
		List<RelativeRiskRow> ccaPPRows = new ArrayList<>();
		List<RelativeRiskRow> ccaITTRows = new ArrayList<>();
		
		// split out ccaStats by PP and ITT rows
		ccaStats.forEach(ees -> {
			ccaPPRows.add(new RelativeRiskRow()
				.targetCohortId(ees.getTargetCohortId())
				.targetCohortName(ees.getTargetCohortName())
				.comparatorCohortId(ees.getComparatorCohortId())
				.comparatorCohortName(ees.getComparatorCohortName())
				.outcomeCohortId(ees.getOutcomeCohortId())
				.outcomeCohortName(ees.getOutcomeCohortName())
				.dataSource(ees.getDataSource())
				.atRisk(ees.getCasesPP())
				.personTime(ees.getPersonTimePP())
				.relativeRisk(ees.getRelativeRiskPP())
				.lb95(ees.getLb95PP())
				.ub95(ees.getUb95PP())
			);
			
			ccaITTRows.add(new RelativeRiskRow()
				.targetCohortId(ees.getTargetCohortId())
				.targetCohortName(ees.getTargetCohortName())
				.comparatorCohortId(ees.getComparatorCohortId())
				.comparatorCohortName(ees.getComparatorCohortName())
				.outcomeCohortId(ees.getOutcomeCohortId())
				.outcomeCohortName(ees.getOutcomeCohortName())
				.dataSource(ees.getDataSource())
				.atRisk(ees.getCasesITT())
				.personTime(ees.getPersonTimeITT())
				.relativeRisk(ees.getRelativeRiskITT())
				.lb95(ees.getLb95ITT())
				.ub95(ees.getUb95ITT())
			);
		});
		
		// per protocol relative risk stats, gruoped by RelativeRiskGroupBy
		Map<RelativeRiskGroupBy, List<RelativeRiskRow>> ppGroups = ccaPPRows.stream()
			.collect(Collectors.groupingBy(r -> new RelativeRiskGroupBy(r.targetCohortName, r.comparatorCohortName, r.outcomeCohortName),LinkedHashMap::new, Collectors.toList()));

		char ccaSubTableIndex = 'a';
		
		for (RelativeRiskGroupBy key : ppGroups.keySet()) {
			JasperReportBuilder rrReport = getRelativeRiskReport();
			rrReport.title(cmp.verticalList(
				cmp.text(String.format("Table %d%s. Relative Risk Summary", tableIndex, ccaSubTableIndex)).setStyle(headingStyle),
				cmp.text(String.format("<b>Target:</b> %s", key.targetCohortName)).setStyle(subHeadingStyle),
				cmp.text(String.format("<b>Comparator:</b> %s", key.comparatorCohortName)).setStyle(subHeadingStyle),
				cmp.text(String.format("<b>0utcome:</b> %s", key.outcomeCohortName)).setStyle(subHeadingStyle),
				cmp.text("<b>Time at Risk:</b> Per Protocol").setStyle(subHeadingStyle)
			).setGap(0))
				.setDataSource(ppGroups.get(key));
			contentBuilder.add(cmp.subreport(rrReport));
			ccaSubTableIndex++;			
		}
		
		// intent to treat relative risk stats, grouped by RelativeRiskGroupBy
		Map<RelativeRiskGroupBy, List<RelativeRiskRow>> ittGroups = ccaITTRows.stream()
			.collect(Collectors.groupingBy(r -> new RelativeRiskGroupBy(r.targetCohortName, r.comparatorCohortName, r.outcomeCohortName),LinkedHashMap::new, Collectors.toList()));

		for (RelativeRiskGroupBy key : ittGroups.keySet()) {
			JasperReportBuilder rrReport = getRelativeRiskReport();
			rrReport.title(cmp.verticalList(
				cmp.text(String.format("Table %d%s. Relative Risk Summary", tableIndex, ccaSubTableIndex)).setStyle(headingStyle),
				cmp.text(String.format("<b>Target:</b> %s", key.targetCohortName)).setStyle(subHeadingStyle),
				cmp.text(String.format("<b>Comparator:</b> %s", key.comparatorCohortName)).setStyle(subHeadingStyle),
				cmp.text(String.format("<b>0utcome:</b> %s", key.outcomeCohortName)).setStyle(subHeadingStyle),
				cmp.text("<b>Time at Risk:</b> Intent to Treat").setStyle(subHeadingStyle)
			).setGap(0))
				.setDataSource(ittGroups.get(key));
			contentBuilder.add(cmp.subreport(rrReport));
			ccaSubTableIndex++;			
		}		
		
		tableIndex++;

		// CCA Diagnostic reports

		// For the diagnostics report, we **assume** that every relative risk statistic has the corresponding diagnostic results generated.
		// Therefore, we use the same relative risk rows (and groups) to genrate the diagnostic reports, just resolving the row to an image
		
		contentBuilder.add(cmp.text("IV. Cohort Comparision Diagnostics").setStyle(this.headingStyle));

		char ccaDiagSubTableIndex = 'a';
		
		for (RelativeRiskGroupBy key : ppGroups.keySet()) {
			JasperReportBuilder ccaDiag = getCCADiagnosticsReport();
			ccaDiag.title(cmp.verticalList(
				cmp.text(String.format("Table %d%s. Risk Estimation Diagnostics", tableIndex, ccaDiagSubTableIndex)).setStyle(headingStyle),
				cmp.text(String.format("<b>Target:</b> %s", key.targetCohortName)).setStyle(subHeadingStyle),
				cmp.text(String.format("<b>Comparator:</b> %s", key.comparatorCohortName)).setStyle(subHeadingStyle),
				cmp.text(String.format("<b>0utcome:</b> %s", key.outcomeCohortName)).setStyle(subHeadingStyle),
				cmp.text("<b>Time at Risk:</b> Per Protocol").setStyle(subHeadingStyle)
			).setGap(0))
				.setDataSource(ppGroups.get(key));
			contentBuilder.add(cmp.subreport(ccaDiag));
			ccaDiagSubTableIndex++;			
		}
		
		for (RelativeRiskGroupBy key : ittGroups.keySet()) {
			JasperReportBuilder ccaDiag = getCCADiagnosticsReport();
			ccaDiag.title(cmp.verticalList(
				cmp.text(String.format("Table %d%s. Risk Estimation Diagnostics", tableIndex, ccaDiagSubTableIndex)).setStyle(headingStyle),
				cmp.text(String.format("<b>Target:</b> %s", key.targetCohortName)).setStyle(subHeadingStyle),
				cmp.text(String.format("<b>Comparator:</b> %s", key.comparatorCohortName)).setStyle(subHeadingStyle),
				cmp.text(String.format("<b>0utcome:</b> %s", key.outcomeCohortName)).setStyle(subHeadingStyle),
				cmp.text("<b>Time at Risk:</b> Intent to Treat").setStyle(subHeadingStyle)
			).setGap(0))
				.setDataSource(ittGroups.get(key));
			contentBuilder.add(cmp.subreport(ccaDiag));
			ccaDiagSubTableIndex++;			
		}
		
		tableIndex++;
		
		// SCCA Reports
		
		contentBuilder.add(cmp.text("V. Self-Control Cohort Analysis Results").setStyle(this.headingStyle));

		// get all outcome statistics across all active databases, and sort
		List<StudyReportService.EffectEstimateStat> sccaStats = studyReportService.getReportSCCA(activePairs, activeSources);
		sccaStats.sort(eeComparator);

		List<RelativeRiskRow> sccaPPRows = new ArrayList<>();
		List<RelativeRiskRow> sccaITTRows = new ArrayList<>();
		
		ccaStats.forEach(ees -> {
			sccaPPRows.add(new RelativeRiskRow()
				.targetCohortId(ees.getTargetCohortId())
				.targetCohortName(ees.getTargetCohortName())
				.outcomeCohortId(ees.getOutcomeCohortId())
				.outcomeCohortName(ees.getOutcomeCohortName())
				.dataSource(ees.getDataSource())
				.atRisk(ees.getCasesPP())
				.personTime(ees.getPersonTimePP())
				.relativeRisk(ees.getRelativeRiskPP())
				.lb95(ees.getLb95PP())
				.ub95(ees.getUb95PP())
			);
			
			sccaITTRows.add(new RelativeRiskRow()
				.targetCohortId(ees.getTargetCohortId())
				.targetCohortName(ees.getTargetCohortName())
				.outcomeCohortId(ees.getOutcomeCohortId())
				.outcomeCohortName(ees.getOutcomeCohortName())
				.dataSource(ees.getDataSource())
				.atRisk(ees.getCasesITT())
				.personTime(ees.getPersonTimeITT())
				.relativeRisk(ees.getRelativeRiskITT())
				.lb95(ees.getLb95ITT())
				.ub95(ees.getUb95ITT())
			);
		});
		
		
		// per protocol relative risk stats, gruoped by RelativeRiskGroupBy
		Map<RelativeRiskGroupBy, List<RelativeRiskRow>> sccaPpGroups = sccaPPRows.stream()
			.collect(Collectors.groupingBy(r -> new RelativeRiskGroupBy(r.targetCohortName, r.comparatorCohortName, r.outcomeCohortName),LinkedHashMap::new, Collectors.toList()));

		char sccaSubTableIndex = 'a';

		for (RelativeRiskGroupBy key : sccaPpGroups.keySet()) {
			JasperReportBuilder rrReport = getRelativeRiskReport();
			rrReport.title(cmp.verticalList(
				cmp.text(String.format("Table %d%s. Relative Risk Summary", tableIndex, sccaSubTableIndex)).setStyle(headingStyle),
				cmp.text(String.format("<b>Target:</b> %s", key.targetCohortName)).setStyle(subHeadingStyle),
				cmp.text(String.format("<b>0utcome:</b> %s", key.outcomeCohortName)).setStyle(subHeadingStyle),
				cmp.text("<b>Time at Risk:</b> Per Protocol").setStyle(subHeadingStyle)
			).setGap(0))
				.setDataSource(sccaPpGroups.get(key));
			contentBuilder.add(cmp.subreport(rrReport));
			sccaSubTableIndex++;			
		}
		
		// intent to treat relative risk stats, grouped by RelativeRiskGroupBy
		Map<RelativeRiskGroupBy, List<RelativeRiskRow>> sccaIttGroups = ccaITTRows.stream()
			.collect(Collectors.groupingBy(r -> new RelativeRiskGroupBy(r.targetCohortName, r.comparatorCohortName, r.outcomeCohortName),LinkedHashMap::new, Collectors.toList()));

		for (RelativeRiskGroupBy key : sccaIttGroups.keySet()) {
			JasperReportBuilder rrReport = getRelativeRiskReport();
			rrReport.title(cmp.verticalList(
				cmp.text(String.format("Table %d%s. Relative Risk Summary", tableIndex, sccaSubTableIndex)).setStyle(headingStyle),
				cmp.text(String.format("<b>Target:</b> %s", key.targetCohortName)).setStyle(subHeadingStyle),
				cmp.text(String.format("<b>0utcome:</b> %s", key.outcomeCohortName)).setStyle(subHeadingStyle),
				cmp.text("<b>Time at Risk:</b> Intent to Treat").setStyle(subHeadingStyle)
			).setGap(0))
				.setDataSource(sccaIttGroups.get(key));
			contentBuilder.add(cmp.subreport(rrReport));
			sccaSubTableIndex++;			
		}		
		
		tableIndex++;

		// CCA Diagnostic reports

		// For the diagnostics report, we **assume** that every relative risk statistic has the corresponding diagnostic results generated.
		// Therefore, we use the same relative risk rows (and groups) to genrate the diagnostic reports, just resolving the row to an image
		
		contentBuilder.add(cmp.text("IV. Cohort Comparision Diagnostics").setStyle(this.headingStyle));

		char sccaDiagSubTableIndex = 'a';
		
		for (RelativeRiskGroupBy key : sccaPpGroups.keySet()) {
			JasperReportBuilder sccaDiag = getSCCADiagnosticsReport();
			sccaDiag.title(cmp.verticalList(
				cmp.text(String.format("Table %d%s. Risk Estimation Diagnostics", tableIndex, sccaDiagSubTableIndex)).setStyle(headingStyle),
				cmp.text(String.format("<b>Target:</b> %s", key.targetCohortName)).setStyle(subHeadingStyle),
				cmp.text(String.format("<b>0utcome:</b> %s", key.outcomeCohortName)).setStyle(subHeadingStyle),
				cmp.text("<b>Time at Risk:</b> Per Protocol").setStyle(subHeadingStyle)
			).setGap(0))
				.setDataSource(sccaPpGroups.get(key));
			contentBuilder.add(cmp.subreport(sccaDiag));
			sccaDiagSubTableIndex++;			
		}
		
		for (RelativeRiskGroupBy key : sccaIttGroups.keySet()) {
			JasperReportBuilder sccaDiag = getSCCADiagnosticsReport();
			sccaDiag.title(cmp.verticalList(
				cmp.text(String.format("Table %d%s. Risk Estimation Diagnostics", tableIndex, sccaDiagSubTableIndex)).setStyle(headingStyle),
				cmp.text(String.format("<b>Target:</b> %s", key.targetCohortName)).setStyle(subHeadingStyle),
				cmp.text(String.format("<b>0utcome:</b> %s", key.outcomeCohortName)).setStyle(subHeadingStyle),
				cmp.text("<b>Time at Risk:</b> Intent to Treat").setStyle(subHeadingStyle)
			).setGap(0))
				.setDataSource(sccaIttGroups.get(key));
			contentBuilder.add(cmp.subreport(sccaDiag));
			sccaDiagSubTableIndex++;			
		}
		
		tableIndex++;

		/* END: Effect Estimate reports */
		
		mainReport.title(contentBuilder);
		return mainReport;
	}


}
