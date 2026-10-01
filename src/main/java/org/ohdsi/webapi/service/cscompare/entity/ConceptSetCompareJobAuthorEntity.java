package org.ohdsi.webapi.service.cscompare.entity;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.ohdsi.webapi.shiro.Entities.UserEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "CONCEPT_SET_COMPARE_JOB_AUTHOR")
public class ConceptSetCompareJobAuthorEntity implements Serializable {

	@Id
	@Column(name = "ID")
	@GenericGenerator(
		name = "concept_set_compare_job_author_generator",
		strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
		parameters = {
			@Parameter(name = "sequence_name", value = "CONCEPT_SET_COMPARE_JOB_AUTHOR_SEQUENCE"),
			@Parameter(name = "initial_value", value = "1"),
			@Parameter(name = "increment_size", value = "1")
		}
	)
	@GeneratedValue(generator = "concept_set_compare_job_author_generator")
	private Integer id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "COMPARE_JOB_ID", nullable = false)
	private ConceptSetCompareJobEntity compareJob;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "USER_ID", nullable = false)
	private UserEntity user;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public ConceptSetCompareJobEntity getCompareJob() {
		return compareJob;
	}

	public void setCompareJob(ConceptSetCompareJobEntity compareJob) {
		this.compareJob = compareJob;
	}

	public UserEntity getUser() {
		return user;
	}

	public void setUser(UserEntity user) {
		this.user = user;
	}
}