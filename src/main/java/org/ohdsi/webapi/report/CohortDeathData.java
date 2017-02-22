package org.ohdsi.webapi.report;

import java.util.List;

public class CohortDeathData {
	private List<ConceptQuartileRecord> agetAtDeath;
	
	private List<ConceptCountRecord> deathByType;
	
	private List<ConceptDecileRecord> prevalenceByGenderAgeYear;
	
	private List<PrevalenceRecord> prevalenceByMonth;

	/**
	 * @return the agetAtDeath
	 */
	public List<ConceptQuartileRecord> getAgetAtDeath() {
		return agetAtDeath;
	}

	/**
	 * @param agetAtDeath the agetAtDeath to set
	 */
	public void setAgetAtDeath(List<ConceptQuartileRecord> agetAtDeath) {
		this.agetAtDeath = agetAtDeath;
	}

	/**
	 * @return the deathByType
	 */
	public List<ConceptCountRecord> getDeathByType() {
		return deathByType;
	}

	/**
	 * @param deathByType the deathByType to set
	 */
	public void setDeathByType(List<ConceptCountRecord> deathByType) {
		this.deathByType = deathByType;
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
