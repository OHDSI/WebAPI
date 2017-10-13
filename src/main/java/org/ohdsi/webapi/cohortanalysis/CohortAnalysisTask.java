package org.ohdsi.webapi.cohortanalysis;

import java.util.List;

import static org.ohdsi.webapi.util.SecurityUtils.whitelist;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ohdsi.webapi.source.Source;

public class CohortAnalysisTask {

	private static final Log log = LogFactory.getLog(CohortAnalysisTasklet.class);

	private String jobName;

	private Source source;

	private String sourceKey;

	private int smallCellCount;

	private boolean runHeraclesHeel;

	private boolean cohortPeriodOnly;
	
	private List<String> visualizations;

	private List<String> cohortDefinitionIds;

	private List<String> analysisIds;

	private List<String> conditionConceptIds;

	private List<String> drugConceptIds;

	private List<String> procedureConceptIds;

	private List<String> observationConceptIds;

	private List<String> measurementConceptIds;

	public String getSourceKey() {
		return sourceKey;
	}

	public void setSourceKey(String sourceKey) {
		this.sourceKey = sourceKey;
	}

	public Source getSource() {
		return source;
	}

	public void setSource(Source source) {
		this.source = source;
	}

	/**
	 * @return the smallCellCount
	 */
	public int getSmallCellCount() {
		return smallCellCount;
	}

	/**
	 * @return the jobName
	 */
	public String getJobName() {
		return jobName;
	}

	/**
	 * @param jobName the jobName to set
	 */
	public void setJobName(String jobName) {
		this.jobName = jobName;
	}

	/**
	 * @param smallCellCount the smallCellCount to set
	 */
	public void setSmallCellCount(int smallCellCount) {
		this.smallCellCount = smallCellCount;
	}

	/**
	 * @return the runHeraclesHeel
	 */
	public boolean runHeraclesHeel() {
		return runHeraclesHeel;
	}

	/**
	 * @param runHeraclesHeel the runHeraclesHeel to set
	 */
	public void setRunHeraclesHeel(boolean runHeraclesHeel) {
		this.runHeraclesHeel = runHeraclesHeel;
	}

	/**
	 * @return the cohortDefinitionId
	 */
	public List<String> getCohortDefinitionIds() {
		return cohortDefinitionIds;
	}

	/**
	 * @param cohortDefinitionIds the cohortDefinitionIds to set
	 */
	public void setCohortDefinitionIds(List<String> cohortDefinitionIds) {
		this.cohortDefinitionIds = cohortDefinitionIds;
	}

	/**
	 * @return the analysisId
	 */
	public List<String> getAnalysisIds() {
		return analysisIds;
	}

	/**
	 * @param analysisIds the analysisIds to set
	 */
	public void setAnalysisIds(List<String> analysisIds) {
		this.analysisIds = analysisIds;
	}

	/**
	 * @return the conditionConceptIds
	 */
	public List<String> getConditionConceptIds() {
		return conditionConceptIds;
	}

	/**
	 * @param conditionConceptIds the conditionConceptIds to set
	 */
	public void setConditionConceptIds(List<String> conditionConceptIds) {
		this.conditionConceptIds = conditionConceptIds;
	}

	/**
	 * @return the drugConceptIds
	 */
	public List<String> getDrugConceptIds() {
		return drugConceptIds;
	}

	/**
	 * @param drugConceptIds the drugConceptIds to set
	 */
	public void setDrugConceptIds(List<String> drugConceptIds) {
		this.drugConceptIds = drugConceptIds;
	}

	/**
	 * @return the procedureConceptIds
	 */
	public List<String> getProcedureConceptIds() {
		return procedureConceptIds;
	}

	/**
	 * @param procedureConceptIds the procedureConceptIds to set
	 */
	public void setProcedureConceptIds(List<String> procedureConceptIds) {
		this.procedureConceptIds = procedureConceptIds;
	}

	/**
	 * @return the observationConceptIds
	 */
	public List<String> getObservationConceptIds() {
		return observationConceptIds;
	}

	/**
	 * @param observationConceptIds the observationConceptIds to set
	 */
	public void setObservationConceptIds(List<String> observationConceptIds) {
		this.observationConceptIds = observationConceptIds;
	}

	/**
	 * @return the measurementConceptIds
	 */
	public List<String> getMeasurementConceptIds() {
		return measurementConceptIds;
	}

	/**
	 * @param measurementConceptIds the measurementConceptIds to set
	 */
	public void setMeasurementConceptIds(List<String> measurementConceptIds) {
		this.measurementConceptIds = measurementConceptIds;
	}

	/**
	 * @return the runHeraclesHeel
	 */
	public boolean isRunHeraclesHeel() {
		return runHeraclesHeel;
	}


	/**
	 * @return the cohortPeriodOnly
	 */
	public boolean isCohortPeriodOnly() {
		return cohortPeriodOnly;
	}

	/**
	 * @param cohortPeriodOnly the cohortPeriodOnly to set
	 */
	public void setCohortPeriodOnly(boolean cohortPeriodOnly) {
		this.cohortPeriodOnly = cohortPeriodOnly;
	}


	/**
	 * @return the visualizations
	 */
	public List<String> getVisualizations() {
		return visualizations;
	}

	/**
	 * @param visualizations the visualizations to set
	 */
	public void setVisualizations(List<String> visualizations) {
		this.visualizations = visualizations;
	}

	@Override
	public String toString() {
			ObjectMapper mapper = new ObjectMapper();
			try {
				return mapper.writeValueAsString(this);
			} catch (Exception e) {
				log.error(whitelist(e));
			}
		return super.toString();

	}
}
