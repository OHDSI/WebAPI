/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ohdsi.webapi.prediction;

import java.io.Serializable;
import org.ohdsi.circe.vocabulary.ConceptSetExpression;
import org.ohdsi.webapi.service.dto.PatientLevelPredictionAnalysisDTO;

/**
 *
 * @author asena5
 */
public class PatientLevelPredictionAnalysisInfo extends PatientLevelPredictionAnalysisDTO implements Serializable {
		private String treatmentCaption;
		private String treatmentCohortDefinition;
		private String outcomeCaption;
		private String outcomeCohortDefinition;
		private String cvExclusionCaption;
		private ConceptSetExpression cvExclusionConceptSet;
		private String cvExclusionConceptSetSql;
		private String cvInclusionCaption;
		private ConceptSetExpression cvInclusionConceptSet;
		private String cvInclusionConceptSetSql;

	public PatientLevelPredictionAnalysisInfo() {
	}

	/**
	 * @return the treatmentCaption
	 */
	public String getTreatmentCaption() {
		return treatmentCaption;
	}

	/**
	 * @param treatmentCaption the treatmentCaption to set
	 */
	public void setTreatmentCaption(String treatmentCaption) {
		this.treatmentCaption = treatmentCaption;
	}

	/**
	 * @return the treatmentCohortDefinition
	 */
	public String getTreatmentCohortDefinition() {
		return treatmentCohortDefinition;
	}

	/**
	 * @param treatmentCohortDefinition the treatmentCohortDefinition to set
	 */
	public void setTreatmentCohortDefinition(String treatmentCohortDefinition) {
		this.treatmentCohortDefinition = treatmentCohortDefinition;
	}

	/**
	 * @return the outcomeCaption
	 */
	public String getOutcomeCaption() {
		return outcomeCaption;
	}

	/**
	 * @param outcomeCaption the outcomeCaption to set
	 */
	public void setOutcomeCaption(String outcomeCaption) {
		this.outcomeCaption = outcomeCaption;
	}

	/**
	 * @return the outcomeCohortDefinition
	 */
	public String getOutcomeCohortDefinition() {
		return outcomeCohortDefinition;
	}

	/**
	 * @param outcomeCohortDefinition the outcomeCohortDefinition to set
	 */
	public void setOutcomeCohortDefinition(String outcomeCohortDefinition) {
		this.outcomeCohortDefinition = outcomeCohortDefinition;
	}

	/**
	 * @return the cvExclusionCaption
	 */
	public String getCvExclusionCaption() {
		return cvExclusionCaption;
	}

	/**
	 * @param cvExclusionCaption the cvExclusionCaption to set
	 */
	public void setCvExclusionCaption(String cvExclusionCaption) {
		this.cvExclusionCaption = cvExclusionCaption;
	}

	/**
	 * @return the cvExclusionConceptSet
	 */
	public ConceptSetExpression getCvExclusionConceptSet() {
		return cvExclusionConceptSet;
	}

	/**
	 * @param cvExclusionConceptSet the cvExclusionConceptSet to set
	 */
	public void setCvExclusionConceptSet(ConceptSetExpression cvExclusionConceptSet) {
		this.cvExclusionConceptSet = cvExclusionConceptSet;
	}

	/**
	 * @return the cvExclusionConceptSetSql
	 */
	public String getCvExclusionConceptSetSql() {
		return cvExclusionConceptSetSql;
	}

	/**
	 * @param cvExclusionConceptSetSql the cvExclusionConceptSetSql to set
	 */
	public void setCvExclusionConceptSetSql(String cvExclusionConceptSetSql) {
		this.cvExclusionConceptSetSql = cvExclusionConceptSetSql;
	}

	/**
	 * @return the cvInclusionCaption
	 */
	public String getCvInclusionCaption() {
		return cvInclusionCaption;
	}

	/**
	 * @param cvInclusionCaption the cvInclusionCaption to set
	 */
	public void setCvInclusionCaption(String cvInclusionCaption) {
		this.cvInclusionCaption = cvInclusionCaption;
	}

	/**
	 * @return the cvInclusionConceptSet
	 */
	public ConceptSetExpression getCvInclusionConceptSet() {
		return cvInclusionConceptSet;
	}

	/**
	 * @param cvInclusionConceptSet the cvInclusionConceptSet to set
	 */
	public void setCvInclusionConceptSet(ConceptSetExpression cvInclusionConceptSet) {
		this.cvInclusionConceptSet = cvInclusionConceptSet;
	}

	/**
	 * @return the cvInclusionConceptSetSql
	 */
	public String getCvInclusionConceptSetSql() {
		return cvInclusionConceptSetSql;
	}

	/**
	 * @param cvInclusionConceptSetSql the cvInclusionConceptSetSql to set
	 */
	public void setCvInclusionConceptSetSql(String cvInclusionConceptSetSql) {
		this.cvInclusionConceptSetSql = cvInclusionConceptSetSql;
	}
	
}
