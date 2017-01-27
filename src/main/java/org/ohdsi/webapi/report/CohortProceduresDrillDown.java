package org.ohdsi.webapi.report;

import java.util.List;

public class CohortProceduresDrillDown {
	private List<ConceptQuartileRecord> ageAtFirstOccurrence;
	
	private List<ConceptCountRecord> proceduresByType;
	
	private List<ConceptDecileRecord> prevalenceByGenderAgeYear;
	
	private List<PrevalenceRecord> prevalenceByMonth;

	/**
	 * @return the ageAtFirstOccurrence
	 */
	public List<ConceptQuartileRecord> getAgeAtFirstOccurrence() {
		return ageAtFirstOccurrence;
	}

	/**
	 * @param ageAtFirstOccurrence the ageAtFirstOccurrence to set
	 */
	public void setAgeAtFirstOccurrence(
			List<ConceptQuartileRecord> ageAtFirstOccurrence) {
		this.ageAtFirstOccurrence = ageAtFirstOccurrence;
	}

	/**
	 * @return the proceduresByType
	 */
	public List<ConceptCountRecord> getProceduresByType() {
		return proceduresByType;
	}

	/**
	 * @param proceduresByType the proceduresByType to set
	 */
	public void setProceduresByType(List<ConceptCountRecord> proceduresByType) {
		this.proceduresByType = proceduresByType;
	}

	/**
	 * @return the prevalenceByGenderAgeYear
	 */
	public List<ConceptDecileRecord> getPrevalenceByGenderAgeYear() {
		return prevalenceByGenderAgeYear;
	}

	/**
	 * @param prevalenceByGenderAgeYear the prevalenceByGenderAgeYear to set
	 */
	public void setPrevalenceByGenderAgeYear(
			List<ConceptDecileRecord> prevalenceByGenderAgeYear) {
		this.prevalenceByGenderAgeYear = prevalenceByGenderAgeYear;
	}

	/**
	 * @return the prevalenceByMonth
	 */
	public List<PrevalenceRecord> getPrevalenceByMonth() {
		return prevalenceByMonth;
	}

	/**
	 * @param prevalenceByMonth the prevalenceByMonth to set
	 */
	public void setPrevalenceByMonth(List<PrevalenceRecord> prevalenceByMonth) {
		this.prevalenceByMonth = prevalenceByMonth;
	}
}
