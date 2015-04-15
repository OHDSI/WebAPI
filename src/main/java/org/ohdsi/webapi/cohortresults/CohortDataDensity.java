package org.ohdsi.webapi.cohortresults;

import java.util.List;

public class CohortDataDensity {

	private List<SeriesPerPerson> recordsPerPerson;
	private List<SeriesPerPerson> totalRecords;
	private List<ConceptQuartileRecord> conceptsPerPerson;
	
	/**
	 * @return the recordsPerPerson
	 */
	public List<SeriesPerPerson> getRecordsPerPerson() {
		return recordsPerPerson;
	}
	/**
	 * @param recordsPerPerson the recordsPerPerson to set
	 */
	public void setRecordsPerPerson(List<SeriesPerPerson> recordsPerPerson) {
		this.recordsPerPerson = recordsPerPerson;
	}
	/**
	 * @return the totalRecords
	 */
	public List<SeriesPerPerson> getTotalRecords() {
		return totalRecords;
	}
	/**
	 * @param totalRecords the totalRecords to set
	 */
	public void setTotalRecords(List<SeriesPerPerson> totalRecords) {
		this.totalRecords = totalRecords;
	}
	/**
	 * @return the conceptsPerPerson
	 */
	public List<ConceptQuartileRecord> getConceptsPerPerson() {
		return conceptsPerPerson;
	}
	/**
	 * @param conceptsPerPerson the conceptsPerPerson to set
	 */
	public void setConceptsPerPerson(List<ConceptQuartileRecord> conceptsPerPerson) {
		this.conceptsPerPerson = conceptsPerPerson;
	}
}
