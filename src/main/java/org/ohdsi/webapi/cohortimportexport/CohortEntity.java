package org.ohdsi.webapi.cohortimportexport;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity(name = "CohortEntity")
@Table(name = "cohort")
public class CohortEntity implements Serializable {

	private static final long serialVersionUID = 7736489323230370316L;
	
	
	@Column(name = "cohort_definition_id")
	private Long cohortDefinitionId;
	
	@Id
	@Column(name = "subject_id")
	private Long subjectId;

	@Column(name = "cohort_start_date")
	private Date cohortStartDate;

	@Column(name = "cohort_end_date")
	private Date cohortEndDate;

	public Long getCohortDefinitionId() {
		return cohortDefinitionId;
	}

	

	public Long getSubjectId() {
		return subjectId;
	}



	public void setSubjectId(Long subjectId) {
		this.subjectId = subjectId;
	}



	public void setCohortDefinitionId(Long cohortDefinitionId) {
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

	public void setCohortEndDate(Date cohortEndDate) {
		this.cohortEndDate = cohortEndDate;
	}
	
	

}
