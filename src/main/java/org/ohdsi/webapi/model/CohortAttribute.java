package org.ohdsi.webapi.model;

import java.util.Date;

public class CohortAttribute {
	
	public static final String COHORT_DEFINITION_ID = "COHORT_DEFINITION_ID";
	public static final String COHORT_START_DATE = "COHORT_START_DATE";
	public static final String COHORT_END_DATE = "COHORT_END_DATE";
	public static final String SUBJECT_ID = "SUBJECT_ID";
	public static final String ATTRIBUTE_DEFINITION_ID = "ATTRIBUTE_DEFINITION_ID";
	public static final String VALUE_AS_NUMBER = "VALUE_AS_NUMBER";
	public static final String VALUE_AS_CONCEPT_ID = "VALUE_AS_CONCEPT_ID";

	private int cohortDefinitionId;
	
	private Date cohortStartDate;
	
	private Date cohortEndDate;
	
	private int subjectId;
	
	private int attributeDefinitionId;
	
	private double valueAsNumber;
	
	private int valueAsConceptId;

	public int getCohortDefinitionId() {
		return cohortDefinitionId;
	}

	public void setCohortDefinitionId(int cohortDefinitionId) {
		this.cohortDefinitionId = cohortDefinitionId;
	}

	public Date getCohortStartDate() {
		return cohortStartDate;
	}

	public void setCohortStartDate(Date cohortStartDate) {
		this.cohortStartDate = cohortStartDate;
	}

	public Date getCohortEndDate() {
		return cohortEndDate;
	}

	public void setCohortEndDate(Date cohrotEndDate) {
		this.cohortEndDate = cohrotEndDate;
	}

	public int getSubjectId() {
		return subjectId;
	}

	public void setSubjectId(int subjectId) {
		this.subjectId = subjectId;
	}

	public int getAttributeDefinitionId() {
		return attributeDefinitionId;
	}

	public void setAttributeDefinitionId(int attributeDefinitionId) {
		this.attributeDefinitionId = attributeDefinitionId;
	}

	public double getValueAsNumber() {
		return valueAsNumber;
	}

	public void setValueAsNumber(double valueAsNumber) {
		this.valueAsNumber = valueAsNumber;
	}

	public int getValueAsConceptId() {
		return valueAsConceptId;
	}

	public void setValueAsConceptId(int valueAsConceptId) {
		this.valueAsConceptId = valueAsConceptId;
	}
}
