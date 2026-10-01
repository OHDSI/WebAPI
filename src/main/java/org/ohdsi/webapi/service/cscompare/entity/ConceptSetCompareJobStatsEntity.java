package org.ohdsi.webapi.service.cscompare.entity;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

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
@Table(name = "CONCEPT_SET_COMPARE_JOB_STATS")
public class ConceptSetCompareJobStatsEntity implements Serializable {

	@Id
	@Column(name = "ID")
	@GenericGenerator(
		name = "concept_set_compare_job_stats_generator",
		strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
		parameters = {
			@Parameter(name = "sequence_name", value = "CONCEPT_SET_COMPARE_JOB_STATS_SEQUENCE"),
			@Parameter(name = "initial_value", value = "1"),
			@Parameter(name = "increment_size", value = "1")
		}
	)
	@GeneratedValue(generator = "concept_set_compare_job_stats_generator")
	private Integer id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "COMPARE_JOB_ID", nullable = false)
	private ConceptSetCompareJobEntity compareJob;

	@Column(name = "CONCEPT_SET_ID", nullable = false)
	private Integer conceptSetId;

	@Column(name = "CS1_INCLUDED_CONCEPTS_COUNT", nullable = false)
	private Integer cs1IncludedConceptsCount;

	@Column(name = "CS1_INCLUDED_SOURCE_CODES_COUNT", nullable = false)
	private Integer cs1IncludedSourceCodesCount;

	@Column(name = "CS2_INCLUDED_CONCEPTS_COUNT", nullable = false)
	private Integer cs2IncludedConceptsCount;

	@Column(name = "CS2_INCLUDED_SOURCE_CODES_COUNT", nullable = false)
	private Integer cs2IncludedSourceCodesCount;

	@Column(name = "HAS_DIFFERENCES", nullable = false)
	private Boolean hasDifferences;

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

	public Integer getConceptSetId() {
		return conceptSetId;
	}

	public void setConceptSetId(Integer conceptSetId) {
		this.conceptSetId = conceptSetId;
	}

	public Integer getCs1IncludedConceptsCount() {
		return cs1IncludedConceptsCount;
	}

	public void setCs1IncludedConceptsCount(Integer cs1IncludedConceptsCount) {
		this.cs1IncludedConceptsCount = cs1IncludedConceptsCount;
	}

	public Integer getCs1IncludedSourceCodesCount() {
		return cs1IncludedSourceCodesCount;
	}

	public void setCs1IncludedSourceCodesCount(Integer cs1IncludedSourceCodesCount) {
		this.cs1IncludedSourceCodesCount = cs1IncludedSourceCodesCount;
	}

	public Integer getCs2IncludedConceptsCount() {
		return cs2IncludedConceptsCount;
	}

	public void setCs2IncludedConceptsCount(Integer cs2IncludedConceptsCount) {
		this.cs2IncludedConceptsCount = cs2IncludedConceptsCount;
	}

	public Integer getCs2IncludedSourceCodesCount() {
		return cs2IncludedSourceCodesCount;
	}

	public void setCs2IncludedSourceCodesCount(Integer cs2IncludedSourceCodesCount) {
		this.cs2IncludedSourceCodesCount = cs2IncludedSourceCodesCount;
	}

	public Boolean getHasDifferences() {
		return hasDifferences;
	}

	public void setHasDifferences(Boolean hasDifferences) {
		this.hasDifferences = hasDifferences;
	}
}