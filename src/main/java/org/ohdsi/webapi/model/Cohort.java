package org.ohdsi.webapi.model;

import java.util.Date;

public class Cohort {	
	
	public static final String COHORT_DEFINITION_ID = "COHORT_DEFINITION_ID";
	public static final String SUBJECT_ID = "SUBJECT_ID";
	public static final String COHORT_START_DATE = "COHORT_START_DATE";
	public static final String COHORT_END_DATE = "COHORT_END_DATE";
	
	private int cohortDefinitionId;
	
	private int subjectId;
	
	private Date cohortStartDate;
	
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
