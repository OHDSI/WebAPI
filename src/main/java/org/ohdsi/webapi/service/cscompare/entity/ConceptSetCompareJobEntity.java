package org.ohdsi.webapi.service.cscompare.entity;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "CONCEPT_SET_COMPARE_JOB")
public class ConceptSetCompareJobEntity implements Serializable {

	@Id
	@Column(name = "ID")
	@GenericGenerator(
		name = "concept_set_compare_job_generator",
		strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
		parameters = {
			@Parameter(name = "sequence_name", value = "CONCEPT_SET_COMPARE_JOB_SEQUENCE"),
			@Parameter(name = "initial_value", value = "1"),
			@Parameter(name = "increment_size", value = "1")
		}
	)
	@GeneratedValue(generator = "concept_set_compare_job_generator")
	private Integer id;

	@Column(name = "EXECUTION_ID", unique = true)
	private Long executionId;

	@Column(name = "SOURCE_1_KEY", nullable = false, length = 50)
	private String source1Key;

	@Column(name = "SOURCE_2_KEY", nullable = false, length = 50)
	private String source2Key;

	@Column(name = "VOCAB_1_VERSION", length = 255)
	private String vocab1Version;

	@Column(name = "VOCAB_2_VERSION", length = 255)
	private String vocab2Version;

	@Column(name = "CREATED_FROM")
	private LocalDate createdFrom;

	@Column(name = "CREATED_TO")
	private LocalDate createdTo;

	@Column(name = "UPDATED_FROM")
	private LocalDate updatedFrom;

	@Column(name = "UPDATED_TO")
	private LocalDate updatedTo;

	@Column(name = "TAGS", length = 5000)
	private String tags;

	@Column(name = "SKIP_LOCKED", nullable = false)
	private Boolean skipLocked;

	@Column(name = "COMPARE_SOURCE_CODES", nullable = false)
	private Boolean compareSourceCodes;

	@Column(name = "CONCEPT_SETS_ANALYZED")
	private Integer conceptSetsAnalyzed;

	@Column(name = "CONCEPT_SETS_WITH_DIFFS")
	private Integer conceptSetsWithDiffs;

	@OneToMany(mappedBy = "compareJob", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
	private Set<ConceptSetCompareJobAuthorEntity> authors = new HashSet<>();

	@OneToMany(mappedBy = "compareJob", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private Set<ConceptSetCompareJobDiffEntity> differences = new HashSet<>();

	@OneToMany(mappedBy = "compareJob", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private Set<ConceptSetCompareJobStatsEntity> statistics = new HashSet<>();

	@Column(name = "concept_set_ids", length = 5000)
	private String conceptSetIds;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Long getExecutionId() {
		return executionId;
	}

	public void setExecutionId(Long executionId) {
		this.executionId = executionId;
	}

	public String getSource1Key() {
		return source1Key;
	}

	public void setSource1Key(String source1Key) {
		this.source1Key = source1Key;
	}

	public String getSource2Key() {
		return source2Key;
	}

	public void setSource2Key(String source2Key) {
		this.source2Key = source2Key;
	}

	public String getVocab1Version() {
		return vocab1Version;
	}

	public void setVocab1Version(String vocab1Version) {
		this.vocab1Version = vocab1Version;
	}

	public String getVocab2Version() {
		return vocab2Version;
	}

	public void setVocab2Version(String vocab2Version) {
		this.vocab2Version = vocab2Version;
	}

	public LocalDate getCreatedFrom() {
		return createdFrom;
	}

	public void setCreatedFrom(LocalDate createdFrom) {
		this.createdFrom = createdFrom;
	}

	public LocalDate getCreatedTo() {
		return createdTo;
	}

	public void setCreatedTo(LocalDate createdTo) {
		this.createdTo = createdTo;
	}

	public LocalDate getUpdatedFrom() {
		return updatedFrom;
	}

	public void setUpdatedFrom(LocalDate updatedFrom) {
		this.updatedFrom = updatedFrom;
	}

	public LocalDate getUpdatedTo() {
		return updatedTo;
	}

	public void setUpdatedTo(LocalDate updatedTo) {
		this.updatedTo = updatedTo;
	}

	public String getTags() {
		return tags;
	}

	public void setTags(String tags) {
		this.tags = tags;
	}

	public Boolean getSkipLocked() {
		return skipLocked;
	}

	public void setSkipLocked(Boolean skipLocked) {
		this.skipLocked = skipLocked;
	}

	public Boolean getCompareSourceCodes() {
		return compareSourceCodes;
	}

	public void setCompareSourceCodes(Boolean compareSourceCodes) {
		this.compareSourceCodes = compareSourceCodes;
	}

	public Integer getConceptSetsAnalyzed() {
		return conceptSetsAnalyzed;
	}

	public void setConceptSetsAnalyzed(Integer conceptSetsAnalyzed) {
		this.conceptSetsAnalyzed = conceptSetsAnalyzed;
	}

	public Integer getConceptSetsWithDiffs() {
		return conceptSetsWithDiffs;
	}

	public void setConceptSetsWithDiffs(Integer conceptSetsWithDiffs) {
		this.conceptSetsWithDiffs = conceptSetsWithDiffs;
	}

	public Set<ConceptSetCompareJobAuthorEntity> getAuthors() {
		return authors;
	}

	public void setAuthors(Set<ConceptSetCompareJobAuthorEntity> authors) {
		this.authors = authors;
	}

	public Set<ConceptSetCompareJobDiffEntity> getDifferences() {
		return differences;
	}

	public void setDifferences(Set<ConceptSetCompareJobDiffEntity> differences) {
		this.differences = differences;
	}

	public Set<ConceptSetCompareJobStatsEntity> getStatistics() {
		return statistics;
	}

	public void setStatistics(Set<ConceptSetCompareJobStatsEntity> statistics) {
		this.statistics = statistics;
	}

	public String getConceptSetIds() {
		return conceptSetIds;
	}

	public void setConceptSetIds(String conceptSetIds) {
		this.conceptSetIds = conceptSetIds;
	}

	// Helper methods for managing authors
	public void addAuthor(ConceptSetCompareJobAuthorEntity author) {
		authors.add(author);
		author.setCompareJob(this);
	}

	public void removeAuthor(ConceptSetCompareJobAuthorEntity author) {
		authors.remove(author);
		author.setCompareJob(null);
	}

	// Helper methods for managing differences
	public void addDifference(ConceptSetCompareJobDiffEntity difference) {
		differences.add(difference);
		difference.setCompareJob(this);
	}

	public void removeDifference(ConceptSetCompareJobDiffEntity difference) {
		differences.remove(difference);
		difference.setCompareJob(null);
	}

	// Helper methods for managing statistics
	public void addStatistic(ConceptSetCompareJobStatsEntity statistic) {
		statistics.add(statistic);
		statistic.setCompareJob(this);
	}

	public void removeStatistic(ConceptSetCompareJobStatsEntity statistic) {
		statistics.remove(statistic);
		statistic.setCompareJob(null);
	}
}