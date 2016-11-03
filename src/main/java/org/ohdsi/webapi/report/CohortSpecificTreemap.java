package org.ohdsi.webapi.report;

import java.util.List;

public class CohortSpecificTreemap {
	private List<HierarchicalConceptRecord> conditionOccurrencePrevalence;
	private List<HierarchicalConceptRecord> procedureOccurrencePrevalence;
	private List<HierarchicalConceptRecord> drugEraPrevalence;

	/**
	 * @return the conditionOccurrencePrevalence
	 */
	public List<HierarchicalConceptRecord> getConditionOccurrencePrevalence() {
		return conditionOccurrencePrevalence;
	}
	/**
	 * @param conditionOccurrencePrevalence the conditionOccurrencePrevalence to set
	 */
	public void setConditionOccurrencePrevalence(
			List<HierarchicalConceptRecord> conditionOccurrencePrevalence) {
		this.conditionOccurrencePrevalence = conditionOccurrencePrevalence;
	}
	/**
	 * @return the procedureOccurrencePrevalence
	 */
	public List<HierarchicalConceptRecord> getProcedureOccurrencePrevalence() {
		return procedureOccurrencePrevalence;
	}
	/**
	 * @param procedureOccurrencePrevalence the procedureOccurrencePrevalence to set
	 */
	public void setProcedureOccurrencePrevalence(
			List<HierarchicalConceptRecord> procedureOccurrencePrevalence) {
		this.procedureOccurrencePrevalence = procedureOccurrencePrevalence;
	}
	/**
	 * @return the drugEraPrevalence
	 */
	public List<HierarchicalConceptRecord> getDrugEraPrevalence() {
		return drugEraPrevalence;
	}
	/**
	 * @param drugEraPrevalence the drugEraPrevalence to set
	 */
	public void setDrugEraPrevalence(
			List<HierarchicalConceptRecord> drugEraPrevalence) {
		this.drugEraPrevalence = drugEraPrevalence;
	}
	
	
}
