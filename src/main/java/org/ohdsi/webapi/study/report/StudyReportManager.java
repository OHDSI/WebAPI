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
import net.sf.dynamicreports.report.builder.group.CustomGroupBuilder;
import net.sf.dynamicreports.report.builder.style.BorderBuilder;
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
import org.ohdsi.webapi.service.StudyReportService.CovariateStat;
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
	private StyleBuilder subreportTitleStyle;
	private StyleBuilder measureStyle;
	private StyleBuilder columnStyle;
	private StyleBuilder columnStyleSmall;
	private StyleBuilder columnHeaderStyle;
	private StyleBuilder groupHeaderStyle;
	private StyleBuilder ctColumnStyle;
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

		titleStyle = stl.style().setFont(stl.fontArialBold().setFontSize(18))
			.setHorizontalTextAlignment(HorizontalTextAlignment.CENTER);

		subreportTitleStyle = stl.style().setFont(stl.fontArialBold().setFontSize(12));

		measureStyle = stl.style().setHorizontalTextAlignment(HorizontalTextAlignment.RIGHT)
			.setVerticalTextAlignment(VerticalTextAlignment.TOP)
			.setFont(stl.fontArial().setFontSize(10))
			.setBorder(stl.pen1Point())
			.setLeftIndent(5)
			.setRightIndent(5);

		columnHeaderStyle = stl.style().setHorizontalTextAlignment(HorizontalTextAlignment.CENTER)
			.setVerticalTextAlignment(VerticalTextAlignment.MIDDLE)
			.setFont(stl.fontArial().setFontSize(10))
			.setBorder(stl.pen1Point())
			.setBackgroundColor(Color.LIGHT_GRAY);
		
		columnStyle = stl.style().setHorizontalTextAlignment(HorizontalTextAlignment.RIGHT)
			.setVerticalTextAlignment(VerticalTextAlignment.MIDDLE)
			.setFont(stl.fontArial().setFontSize(10))
			.setBorder(stl.pen1Point());

		columnStyleSmall = stl.style().setHorizontalTextAlignment(HorizontalTextAlignment.RIGHT)
			.setVerticalTextAlignment(VerticalTextAlignment.MIDDLE)
			.setFont(stl.fontArial().setFontSize(8))
			.setBorder(stl.pen1Point())
			.setPadding(3);
		
		ctColumnStyle = stl.style(columnHeaderStyle);

		groupHeaderStyle = stl.style().setHorizontalTextAlignment(HorizontalTextAlignment.LEFT)
			.setVerticalTextAlignment(VerticalTextAlignment.MIDDLE)
			.setFont(stl.fontArial().setFontSize(10).setBold(true))
			.setTopPadding(5);
		
		footnoteStyle = stl.style().setFont(stl.fontArial().setFontSize(8)).setMarkup(Markup.STYLED);
		
	}

	public StudyReportManager() {
		initStyles();
	}

	private CrosstabBuilder getCovariateCrossTab() throws Exception {

		CrosstabRowGroupBuilder<String> rowGroup = ctab.rowGroup("name", String.class)
			.setShowTotal(false)
			.setHeaderStyle(stl.style(measureStyle).setMarkup(Markup.STYLED).setHorizontalTextAlignment(HorizontalTextAlignment.LEFT))
			.setHeaderWidth(140);
		
		CrosstabColumnGroupBuilder<String> columnGroup = ctab.columnGroup("dataSource", String.class)
			.setShowTotal(false)
			.setHeaderStyle(ctColumnStyle);

		CrosstabMeasureBuilder<Double> statValueMeasure = ctab.measure("%", "statValue", Double.class, Calculation.NOTHING);
		CrosstabMeasureBuilder<Long> countMeasure = ctab.measure("N", "count", Long.class, Calculation.NOTHING);
		
		statValueMeasure.setStyle(measureStyle).setPattern("#0.00%");
		countMeasure.setStyle(measureStyle);
		
		CrosstabBuilder crosstab = ctab.crosstab()
			.rowGroups(rowGroup)
			.columnGroups(columnGroup)
			.measures(countMeasure, statValueMeasure)
			//.setStyle(stl.style().setBorder(stl.pen1Point()).setLeftIndent(5).setRightIndent(5))
			.setCellWidth(120);

		return crosstab;
	}

	private JasperReportBuilder getImageTest(String imagePath) {
		JasperReportBuilder imgTest = report();
		String filename = resourcePath + imagePath;
		// add a column for an image.
		imgTest.addSummary(cmp.image(filename).setHeight(200));
		return imgTest;
	}

	private JasperReportBuilder getIRReport(Map<Long,StudyCohort> cohortLookup) {
		// rendering of reports goes cohort
		TextColumnBuilder<String> dataSourceCol = col.column("Data Source", "dataSource", type.stringType());
		TextColumnBuilder<Integer> atRiskCol = col.column("At Risk", "atRiskPP", type.integerType()).setFixedWidth(50);
		TextColumnBuilder<Integer> casesPPCol = col.column("Cases", "casesPP", type.integerType()).setFixedWidth(50);
		TextColumnBuilder<Double> personTimePPCol = col.column("TAR", "personTimePP", type.doubleType()).setPattern("#,##0.0");
		TextColumnBuilder<Double> proportionPPCol = col.column("IP", "incidenceProportionPP", type.doubleType()).setFixedWidth(30).setPattern("#0.00");
		TextColumnBuilder<Double> ratePPCol = col.column("IR", "incidenceRatePP",type.doubleType()).setFixedWidth(30).setPattern("#0.00");
		TextColumnBuilder<Integer> casesITTCol = col.column("Cases", "casesITT", type.integerType()).setFixedWidth(50);
		TextColumnBuilder<Double> personTimeITTCol = col.column("TAR", "personTimeITT", type.doubleType()).setPattern("#,##0.0");
		TextColumnBuilder<Double> proportionITTCol = col.column("IP", "incidenceProportionITT", type.doubleType()).setFixedWidth(30).setPattern("#0.00");
		TextColumnBuilder<Double> rateITTCol = col.column("IR", "incidenceRateITT",type.doubleType()).setFixedWidth(30).setPattern("#0.00");
		
		ColumnTitleGroupBuilder ppGroup = grid.titleGroup("Per Protocol", casesPPCol,personTimePPCol,proportionPPCol, ratePPCol).setTitleFixedWidth(180);
		ColumnTitleGroupBuilder ittGroup = grid.titleGroup("Intent to Treat", casesITTCol,personTimeITTCol,proportionITTCol, rateITTCol).setTitleFixedWidth(180);

		CustomGroupBuilder cohortGroup = grp.group(new OutcomeGroupExpression())
			.keepTogether();
		
		JasperReportBuilder irReport = report()
			.fields(
				field("targetCohortName", String.class),
				field("outcomeCohortName", String.class)
			)
			.columnGrid(dataSourceCol, atRiskCol, ppGroup, ittGroup)
			.columns(dataSourceCol,atRiskCol,
				casesPPCol,personTimePPCol,proportionPPCol, ratePPCol,
				casesITTCol,personTimeITTCol,proportionITTCol, rateITTCol
			)
			.groupBy(cohortGroup)
			.setColumnStyle(this.columnStyleSmall)
			.setColumnTitleStyle(this.columnHeaderStyle)
			.setColumnHeaderStyle(this.columnHeaderStyle)
			.setGroupStyle(this.groupHeaderStyle)
//			.setGroupTitleStyle(this.groupHeaderStyle)
			;
		return irReport;
	}
	
	private JasperReportBuilder getRelativeRiskReport(boolean isSelfControl) {

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
				field("ub95", Double.class),
				field("comparatorCohortName", String.class)
			)
			.columns(dataSourceCol,atRiskCol, casesCol,personTimeCol, relativeRiskCol, ciCol)
			.setColumnStyle(this.columnStyleSmall)
			.setColumnTitleStyle(this.columnHeaderStyle)
			.setColumnHeaderStyle(this.columnHeaderStyle)
		;
		
		if (!isSelfControl) {
			ccaReport.groupBy(grp.group("comparatorCohortName", String.class).keepTogether());
		}
		return ccaReport;
	}
	
	private JasperReportBuilder getCCADiagnosticsReport() {

		ImageBuilder psImage = cmp.image(new PSImageExpression());
		ImageBuilder cbImage = cmp.image(new CovariateBalanceImageExpression());
		ImageBuilder calibrationImage = cmp.image(new CalibrationImageExpression());

		TextColumnBuilder<String> dataSourceCol = col.column("Data Source", "dataSource", type.stringType());
		JasperReportBuilder ccaDiagnosticsReport = report()
			.fields(
				field("comparatorCohortName", String.class)
			)
			.columns(dataSourceCol,
				col.componentColumn("Preference Score", cmp.centerVertical(psImage)),
				col.componentColumn("Covariate Balance", cmp.centerVertical(cbImage)),
				col.componentColumn("Calibration", cmp.centerVertical(calibrationImage)))
			.setColumnStyle(stl.style(this.columnStyleSmall).setBorder(stl.pen(0.0f, LineStyle.SOLID)))
			.setColumnTitleStyle(this.columnHeaderStyle)
			.setColumnHeaderStyle(this.columnHeaderStyle)
			.groupBy(grp.group("comparatorCohortName", String.class).keepTogether());
		
		return ccaDiagnosticsReport;
	}
	
	public JasperReportBuilder getMainReport(Report report) throws Exception {

		// create container report to host embedded report sections
		JasperReportBuilder mainReport = report();
		int tableIndex = 1;
		
		mainReport.setPageMargin(margin(40));

		VerticalListBuilder contentBuilder = cmp.verticalList().setGap(10);

		contentBuilder.add(cmp.text("Concatenated reports"));
		
		//contentBuilder.add(cmp.subreport(getImageTest("sample/calibration_plot.png")));		

		contentBuilder.add(cmp.text("Cohort Characterization"));

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
				}
			});
		
		// Comparators

		Comparator<CovariateStat> demographicComparator = Comparator.comparing(cs -> demographicCovariateLookup.get(cs.getCovariateId()));
		demographicComparator = demographicComparator.thenComparing(cs -> sourceLookup.get(cs.getDataSource()));
		
		Comparator<CovariateStat> conditionComparator = Comparator.comparing(cs -> conditionCovariateLookup.get(cs.getCovariateId()));
		conditionComparator = conditionComparator.thenComparing(cs -> sourceLookup.get(cs.getDataSource()));
		
		Comparator<CovariateStat> drugComparator = Comparator.comparing(cs -> drugCovariateLookup.get(cs.getCovariateId()));
		drugComparator = drugComparator.thenComparing(cs -> sourceLookup.get(cs.getDataSource()));
		
		Comparator<CovariateStat> procedureComparator = Comparator.comparing(cs -> procedureCovariateLookup.get(cs.getCovariateId()));
		procedureComparator = procedureComparator.thenComparing(cs -> sourceLookup.get(cs.getDataSource()));

		// Covariate Stats
		List<CovariateStat> covariateStats = studyReportService.getReportCovariates(report);
		
		for (ReportCohortPair cohortPair : activePairs) {
			contentBuilder.add(cmp.text(String.format("<b>Cohort:</b> %s", cohortPair.getTarget().getName())).setMarkup(Markup.STYLED));
				
			CrosstabBuilder crosstab;
			JasperReportBuilder subReport;

			// For each section, create list of covariates to pass into crosstab, 
			// which must be sorted by the covariate ordinal, then by datasource ordinal
			
			// DEMOGRAPHICS
			List<CovariateStat> demographicStats = covariateStats.stream()
				.filter(cs -> demographicCovariateLookup.containsKey(cs.getCovariateId()) && cs.getCohortId() == cohortPair.getTarget().getId())
				.sorted(demographicComparator)
				.collect(Collectors.toList());

			crosstab = getCovariateCrossTab()
				.setDataSource(new JRBeanCollectionDataSource(demographicStats))
				.setDataPreSorted(true);

			subReport = report()
				.title(cmp.text(String.format("Table %da. Demographics", tableIndex)).setMarkup(Markup.STYLED))
				.summary(demographicStats.size() > 0 ? crosstab : cmp.text("No demographic covariates selected."));

			contentBuilder.add(cmp.subreport(subReport));
			
			// Conditions
			List<CovariateStat> conditionStats = covariateStats.stream()
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
				.title(cmp.text(String.format("Table %db. Conditions", tableIndex)).setMarkup(Markup.STYLED))
				.summary(conditionStats.size() > 0 ? cmp.verticalList(crosstab, 
					cmp.text(footnoteText).setStyle(this.footnoteStyle)).setGap(3)
					: cmp.text("No condition covariates selected."));
			
			contentBuilder.add(cmp.subreport(subReport));
			
			// Drugs
			List<CovariateStat> drugStats = covariateStats.stream()
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
				.title(cmp.text(String.format("Table %dc. Drugs", tableIndex)).setMarkup(Markup.STYLED))
				.summary(drugStats.size() > 0 ? cmp.verticalList(crosstab, 
					cmp.text(drugFootnoteText).setStyle(this.footnoteStyle)).setGap(3)
					: cmp.text("No drug covariates selected."));

			contentBuilder.add(cmp.subreport(subReport));
			
			// Procedures
			List<CovariateStat> procedureStats = covariateStats.stream()
				.filter(cs -> procedureCovariateLookup.containsKey(cs.getCovariateId()) && cs.getCohortId() == cohortPair.getTarget().getId())
				.sorted(procedureComparator)
				.collect(Collectors.toList());

			crosstab = getCovariateCrossTab()
				.setDataSource(new JRBeanCollectionDataSource(procedureStats))
				.setDataPreSorted(true);

			subReport = report()
				.title(cmp.text(String.format("Table %dd. Procedures", tableIndex)).setMarkup(Markup.STYLED))
				.summary(procedureStats.size() > 0 ? crosstab : cmp.text("No procedure covariates selected."));
			
			contentBuilder.add(cmp.subreport(subReport));
			
			tableIndex++;
			
		}
		/* END: Covariate reports */

		/* BEGIN: IR reports */
		
		// Get report
		JasperReportBuilder irReport = getIRReport(cohortLookup);

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
		
		irReport.title(cmp.text(String.format("Table %d. Outcome Summary (Target, Outcome, Source)", tableIndex)).setMarkup(Markup.STYLED))
			.setDataSource(outcomeStats);
		tableIndex++;
		
		contentBuilder.add(cmp.subreport(irReport));
		
		/* END: IR reports */
		
		/* BEGIN: Effect Estimate reports */

		// effect estimate comparator: by pair index then by datasource index
		Comparator<StudyReportService.EffectEstimateStat> eeComparator = 
			Comparator.comparing(ees -> {
				ReportCohortPair targetPair = activePairs.stream().filter(p -> { 
					return (p.getTarget().getId() == ees.getTargetCohortId() && p.getOutcome().getId() == ees.getOutcomeCohortId());
				}).findFirst().get();
				return activePairs.indexOf(targetPair);
			});
		eeComparator = eeComparator.thenComparing(ees -> sourceLookup.get(ees.getDataSource()));
		eeComparator = eeComparator.thenComparing(ees -> ees.getComparatorCohortName());
		
		// CCA Reports
		 
		contentBuilder.add(cmp.text("Cohort Comparision Anaysis Results"));

		// get all outcome statistics across all active databases, and sort
		List<StudyReportService.EffectEstimateStat> ccaStats = studyReportService.getReportCCA(activePairs, activeSources);
		ccaStats.sort(eeComparator);
		
		List<RelativeRiskRow> ccaPPRows = new ArrayList<>();
		List<RelativeRiskRow> ccaITTRows = new ArrayList<>();
		
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
		
		char ccaSubTableIndex = 'a';
		for (ReportCohortPair p : activePairs) {
			JasperReportBuilder rrReport = getRelativeRiskReport(false);
			List<RelativeRiskRow> rrRows = ccaPPRows.stream()
				.filter(s -> {return Objects.equals(s.getTargetCohortId(), p.getTarget().getId()) && Objects.equals(s.getOutcomeCohortId(), p.getOutcome().getId());})
				.collect(Collectors.toList());
			
			rrReport.title(cmp.text(String.format("Table %d%s. Per Protocol Relative Risk Summary: %s - %s)", 
				tableIndex, 
				String.valueOf(ccaSubTableIndex), 
				p.getTarget().getName(), 
				p.getOutcome().getName())).setMarkup(Markup.STYLED))
				.setDataSource(rrRows);
			contentBuilder.add(cmp.subreport(rrReport));
			ccaSubTableIndex++;
		}
		tableIndex++;
		
		ccaSubTableIndex = 'a';
		for (ReportCohortPair p : activePairs) {
			JasperReportBuilder rrReport = getRelativeRiskReport(false);
			List<RelativeRiskRow> rrRows = ccaITTRows.stream()
				.filter(s -> {return Objects.equals(s.getTargetCohortId(), p.getTarget().getId()) && Objects.equals(s.getOutcomeCohortId(), p.getOutcome().getId());})
				.collect(Collectors.toList());
			
			rrReport.title(cmp.text(String.format("Table %d%s. Intent To Treat Relative Risk Summary: %s - %s)", 
				tableIndex, 
				String.valueOf(ccaSubTableIndex), 
				p.getTarget().getName(), 
				p.getOutcome().getName())).setMarkup(Markup.STYLED))
				.setDataSource(rrRows);
			contentBuilder.add(cmp.subreport(rrReport));
			ccaSubTableIndex++;
		}		
		tableIndex++;
		
		// CCA Diagnostic reports
		char ccaDiagSubTableIndex = 'a';
		for (ReportCohortPair p : activePairs) {
			JasperReportBuilder ccaDiag = getCCADiagnosticsReport();
			List<RelativeRiskRow> rrRows = ccaPPRows.stream()
				.filter(s -> {return Objects.equals(s.getTargetCohortId(), p.getTarget().getId()) && Objects.equals(s.getOutcomeCohortId(), p.getOutcome().getId());})
				.collect(Collectors.toList());
			
			ccaDiag.title(cmp.text(String.format("Table %d%s. Per Protocol Diagnostics: %s - %s)", 
				tableIndex, 
				String.valueOf(ccaDiagSubTableIndex), 
				p.getTarget().getName(), 
				p.getOutcome().getName())).setMarkup(Markup.STYLED))
				.setDataSource(rrRows);
			contentBuilder.add(cmp.subreport(ccaDiag));
			ccaDiagSubTableIndex++;
		}
		tableIndex++;
		
		
		
		
		contentBuilder.add(cmp.text("Cohort Comparision Diagnostics"));
		
		
		// SCCA Reports
		
		contentBuilder.add(cmp.text("Self Control Cohort Anaysis Results"));

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
		
		char sccaSubTableIndex = 'a';
		for (ReportCohortPair p : activePairs) {
			JasperReportBuilder rrReport = getRelativeRiskReport(true);
			List<RelativeRiskRow> rrRows = sccaPPRows.stream()
				.filter(s -> {return Objects.equals(s.getTargetCohortId(), p.getTarget().getId()) && Objects.equals(s.getOutcomeCohortId(), p.getOutcome().getId());})
				.collect(Collectors.toList());
			
			rrReport.title(cmp.text(String.format("Table %d%s. Per Protocol Relative Risk Summary: %s - %s)", 
				tableIndex, 
				String.valueOf(sccaSubTableIndex), 
				p.getTarget().getName(), 
				p.getOutcome().getName())).setMarkup(Markup.STYLED))
				.setDataSource(rrRows);
			contentBuilder.add(cmp.subreport(rrReport));
			sccaSubTableIndex++;
		}
		tableIndex++;
		
		sccaSubTableIndex = 'a';
		for (ReportCohortPair p : activePairs) {
			JasperReportBuilder rrReport = getRelativeRiskReport(true);
			List<RelativeRiskRow> rrRows = sccaITTRows.stream()
				.filter(s -> {return Objects.equals(s.getTargetCohortId(), p.getTarget().getId()) && Objects.equals(s.getOutcomeCohortId(), p.getOutcome().getId());})
				.collect(Collectors.toList());
			
			rrReport.title(cmp.text(String.format("Table %d%s. Intent To Treat Relative Risk Summary: %s - %s)", 
				tableIndex, 
				String.valueOf(sccaSubTableIndex), 
				p.getTarget().getName(), 
				p.getOutcome().getName())).setMarkup(Markup.STYLED))
				.setDataSource(rrRows);
			contentBuilder.add(cmp.subreport(rrReport));
			sccaSubTableIndex++;
		}		
		tableIndex++;
		
		/* END: Effect Estimate reports */
		
		
		mainReport.title(contentBuilder);
		return mainReport;
	}


}
