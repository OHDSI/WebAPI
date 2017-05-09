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
import static net.sf.dynamicreports.report.builder.DynamicReports.*;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.base.expression.AbstractSimpleExpression;
import net.sf.dynamicreports.report.builder.column.TextColumnBuilder;
import net.sf.dynamicreports.report.builder.component.VerticalListBuilder;
import net.sf.dynamicreports.report.builder.crosstab.CrosstabBuilder;
import net.sf.dynamicreports.report.builder.crosstab.CrosstabColumnGroupBuilder;
import net.sf.dynamicreports.report.builder.crosstab.CrosstabMeasureBuilder;
import net.sf.dynamicreports.report.builder.crosstab.CrosstabRowGroupBuilder;
import net.sf.dynamicreports.report.builder.grid.ColumnTitleGroupBuilder;
import net.sf.dynamicreports.report.builder.group.CustomGroupBuilder;
import net.sf.dynamicreports.report.builder.style.StyleBuilder;
import net.sf.dynamicreports.report.constant.Calculation;
import net.sf.dynamicreports.report.constant.HorizontalTextAlignment;
import net.sf.dynamicreports.report.constant.Markup;
import net.sf.dynamicreports.report.constant.VerticalTextAlignment;
import net.sf.dynamicreports.report.definition.ReportParameters;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.ohdsi.webapi.service.StudyReportService;
import org.ohdsi.webapi.service.StudyReportService.CovariateStat;
import org.ohdsi.webapi.study.StudyCohort;
import org.springframework.beans.factory.annotation.Autowired;
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

	private StyleBuilder titleStyle;
	private StyleBuilder subreportTitleStyle;
	private StyleBuilder measureStyle;
	private StyleBuilder columnStyle;
	private StyleBuilder columnStyleSmall;
	private StyleBuilder columnHeaderStyle;
	private StyleBuilder groupHeaderStyle;
	private StyleBuilder ctColumnStyle;

	private class OutcomeGroupExpression extends AbstractSimpleExpression<String> {
		
		private Map<Long, StudyCohort> cohortLookup;
		
		public OutcomeGroupExpression(Map<Long, StudyCohort> cohortLookup) {
			this.cohortLookup = cohortLookup;
		}
		
		@Override
		public String evaluate(ReportParameters reportParameters) {
	
			Long targetCohortId = reportParameters.getValue("targetCohortId");
			Long outcomeCohortId = reportParameters.getValue("outcomeCohortId");
			return String.format("%s - %s", 
				cohortLookup.get(targetCohortId).getName(), 
				cohortLookup.get(outcomeCohortId).getName());
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
		
	}

	public StudyReportManager() {
		initStyles();
	}

	private CrosstabBuilder getCovariateCrossTab() throws Exception {

		CrosstabRowGroupBuilder<String> rowGroup = ctab.rowGroup("name", String.class)
			.setShowTotal(false)
			.setHeaderStyle(measureStyle);
		
		CrosstabColumnGroupBuilder<String> columnGroup = ctab.columnGroup("dataSource", String.class)
			.setShowTotal(false)
			.setHeaderStyle(ctColumnStyle);

		CrosstabMeasureBuilder<Double> statValueMeasure = ctab.measure("Value", "statValue", Double.class, Calculation.NOTHING);
		CrosstabMeasureBuilder<Long> countMeasure = ctab.measure("Count", "count", Long.class, Calculation.NOTHING);
		
		statValueMeasure.setStyle(measureStyle).setPattern("#0.00%");
		countMeasure.setStyle(measureStyle);
		
		CrosstabBuilder crosstab = ctab.crosstab()
			.rowGroups(rowGroup)
			.columnGroups(columnGroup)
			.measures(statValueMeasure, countMeasure)
			//.setStyle(stl.style().setBorder(stl.pen1Point()).setLeftIndent(5).setRightIndent(5))
			.setCellWidth(120);

		return crosstab;
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

		CustomGroupBuilder cohortGroup = grp.group(new OutcomeGroupExpression(cohortLookup))
			.keepTogether();
		
		JasperReportBuilder irReport = report()
			.fields(
				field("targetCohortId", Long.class),
				field("outcomeCohortId", Long.class)
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
	
	public JasperReportBuilder getMainReport(Report report) throws Exception {

		// create container report to host embedded report sections
		JasperReportBuilder mainReport = report();
		int tableIndex = 1;
		
		mainReport.setPageMargin(margin(40));

		VerticalListBuilder contentBuilder = cmp.verticalList();

		contentBuilder.add(cmp.text("Concatenated reports"));

		/* BEGIN: Covariate reports */
		
		contentBuilder.add(cmp.text("Cohort Characterization"));

		// create cohort lookup id->cohort
		Map<Long, StudyCohort> cohortLookup = new HashMap<>();
		report.getCohortPairs().forEach(p -> {
			if (!cohortLookup.containsKey(p.getTarget().getId()))
				cohortLookup.put(p.getTarget().getId(), p.getTarget());
			if (!cohortLookup.containsKey(p.getOutcome().getId()))
				cohortLookup.put(p.getOutcome().getId(), p.getOutcome());
		});
		
		// get active datasources and create a lookup to resolve source key to order
		List<ReportSource> activeSources = report.getSources().stream().filter(s -> s.isActive()).collect(Collectors.toList());
		Map<String, Integer> sourceLookup = IntStream.range(0, activeSources.size()).boxed()
			.collect(Collectors.toMap(i -> activeSources.get(i).getSource().getKey(), i -> i));

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

		for (ReportCohortPair cohortPair : report.getCohortPairs()) {
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
				.summary(crosstab);

			contentBuilder.add(cmp.subreport(subReport));
			
			// Conditions
			List<CovariateStat> conditionStats = covariateStats.stream()
				.filter(cs -> conditionCovariateLookup.containsKey(cs.getCovariateId()) && cs.getCohortId() == cohortPair.getTarget().getId())
				.sorted(conditionComparator)
				.collect(Collectors.toList());

			crosstab = getCovariateCrossTab()
				.setDataSource(new JRBeanCollectionDataSource(conditionStats))
				.setDataPreSorted(true);

			subReport = report()
				.title(cmp.text(String.format("Table %db. Conditions", tableIndex)).setMarkup(Markup.STYLED))
				.summary(crosstab);

			contentBuilder.add(cmp.subreport(subReport));
			
			// Drugs
			List<CovariateStat> drugStats = covariateStats.stream()
				.filter(cs -> drugCovariateLookup.containsKey(cs.getCovariateId()) && cs.getCohortId() == cohortPair.getTarget().getId())
				.sorted(drugComparator)
				.collect(Collectors.toList());

			crosstab = getCovariateCrossTab()
				.setDataSource(new JRBeanCollectionDataSource(drugStats))
				.setDataPreSorted(true);

			subReport = report()
				.title(cmp.text(String.format("Table %dc. Drugs", tableIndex)).setMarkup(Markup.STYLED))
				.summary(crosstab);

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
				.summary(crosstab);
			
			contentBuilder.add(cmp.subreport(subReport));
			
			tableIndex++;
			
		}
		/* END: Covariate reports */

		/* BEGIN: IR reports */

		// get all outcome statistics across all databases
		List<StudyReportService.OutcomeSummaryStat> outcomeStats = studyReportService.getReportOutcomes(report);
		
		JasperReportBuilder irReport = getIRReport(cohortLookup);

		irReport.title(cmp.text(String.format("Table %d. Outcome Summary (Target, Outcome, Source)", tableIndex)).setMarkup(Markup.STYLED))
			.setDataSource(outcomeStats);
		tableIndex++;
		
		contentBuilder.add(cmp.subreport(irReport));
		
		/* END: IR reports */
		
		mainReport.title(contentBuilder);
		return mainReport;
	}


}
