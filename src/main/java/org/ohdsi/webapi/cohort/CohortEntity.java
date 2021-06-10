package org.ohdsi.webapi.cohort;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

@Entity(name = "CohortEntity")
@Table(name = "cohort")
@IdClass(CohortEntity.class)
public class CohortEntity implements Serializable {

	private static final long serialVersionUID = 7736489323230370316L;
	
	@Id
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



	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((cohortDefinitionId == null) ? 0 : cohortDefinitionId
						.hashCode());
		result = prime * result
				+ ((subjectId == null) ? 0 : subjectId.hashCode());
		return result;
	}



	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CohortEntity other = (CohortEntity) obj;
		if (cohortDefinitionId == null) {
			if (other.cohortDefinitionId != null)
				return false;
		} else if (!cohortDefinitionId.equals(other.cohortDefinitionId))
			return false;
		if (subjectId == null) {
			if (other.subjectId != null)
				return false;
		} else if (!subjectId.equals(other.subjectId))
			return false;
		return true;
	}
	
	

}
