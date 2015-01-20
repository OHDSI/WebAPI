package org.ohdsi.webapi.model;

import java.util.Date;

public class CohortDefinition {

	public static final String COHORT_DEFINITION_ID = "COHORT_DEFINITION_ID";
	public static final String COHORT_DEFINITION_NAME = "COHORT_DEFINITION_NAME";
	public static final String COHORT_DEFINITION_DESCRIPTION = "COHORT_DEFINITION_DESCRIPTION";
	public static final String DEFINITION_TYPE_CONCEPT_ID = "DEFINITION_TYPE_CONCEPT_ID";
	public static final String COHORT_DEFINITION_SYNTAX = "COHORT_DEFINITION_SYNTAX";
	public static final String SUBJECT_CONCEPT_ID = "SUBJECT_CONCEPT_ID";
	public static final String COHORT_INITIATION_DATE = "COHORT_INITIATION_DATE";
	
	private int cohortDefinitionId;
	
	private String cohortDefinitionName;
	
	private String cohortDefinitionDescription;
	
	private int definitionTypeConceptId;
	
	private String cohortDefinitionSyntax;
	
	private int subjectConceptId;
	
	private Date cohortInitiationDate;

	public int getCohortDefinitionId() {
		return cohortDefinitionId;
	}

	public void setCohortDefinitionId(int cohortDefinitionId) {
		this.cohortDefinitionId = cohortDefinitionId;
	}

	public String getCohortDefinitionName() {
		return cohortDefinitionName;
	}

	public void setCohortDefinitionName(String cohortDefinitionName) {
		this.cohortDefinitionName = cohortDefinitionName;
	}

	public String getCohortDefinitionDescription() {
		return cohortDefinitionDescription;
	}

	public void setCohortDefinitionDescription(String cohortDefinitionDescription) {
		this.cohortDefinitionDescription = cohortDefinitionDescription;
	}

	public int getDefinitionTypeConceptId() {
		return definitionTypeConceptId;
	}

	public void setDefinitionTypeConceptId(int definitionTypeConceptId) {
		this.definitionTypeConceptId = definitionTypeConceptId;
	}

	public String getCohortDefinitionSyntax() {
		return cohortDefinitionSyntax;
	}

	public void setCohortDefinitionSyntax(String cohortDefinitionSyntax) {
		this.cohortDefinitionSyntax = cohortDefinitionSyntax;
	}

	public int getSubjectConceptId() {
		return subjectConceptId;
	}

	public void setSubjectConceptId(int subjectConceptId) {
		this.subjectConceptId = subjectConceptId;
	}

	public Date getCohortInitiationDate() {
		return cohortInitiationDate;
	}

	public void setCohortInitiationDate(Date cohortInitiationDate) {
		this.cohortInitiationDate = cohortInitiationDate;
	}
	
	
}
