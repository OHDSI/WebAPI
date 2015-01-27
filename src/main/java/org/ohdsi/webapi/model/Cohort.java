package org.ohdsi.webapi.model;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Cohort {	
	
	public static final String COHORT_DEFINITION_ID = "COHORT_DEFINITION_ID";
	public static final String SUBJECT_ID = "SUBJECT_ID";
	public static final String COHORT_START_DATE = "COHORT_START_DATE";
	public static final String COHORT_END_DATE = "COHORT_END_DATE";
	
	@JsonProperty(COHORT_DEFINITION_ID)
	private int cohortDefinitionId;
	
	@JsonProperty(SUBJECT_ID)
	private int subjectId;
	
	@JsonProperty(COHORT_START_DATE)
	private Date cohortStartDate;
	
	@JsonProperty(COHORT_END_DATE)
	private Date cohortEndDate;

	public int getCohortDefinitionId() {
		return cohortDefinitionId;
	}

	public void setCohortDefinitionId(int cohortDefinitionId) {
		this.cohortDefinitionId = cohortDefinitionId;
	}

	public int getSubjectId() {
		return subjectId;
	}

	public void setSubjectId(int subjectId) {
		this.subjectId = subjectId;
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

	public void setCohortEndDate(Date cohortEndDate) {
		this.cohortEndDate = cohortEndDate;
	}
}
