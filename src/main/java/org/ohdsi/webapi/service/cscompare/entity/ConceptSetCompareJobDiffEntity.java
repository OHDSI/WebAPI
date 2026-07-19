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
import java.sql.Date;

@Entity
@Table(name = "concept_set_compare_job_diff")
public class ConceptSetCompareJobDiffEntity {

	@Id
	@GenericGenerator(
		name = "concept_set_compare_job_diff_generator",
		strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
		parameters = {
			@Parameter(name = "sequence_name", value = "concept_set_compare_job_diff_sequence"),
			@Parameter(name = "increment_size", value = "1")
		}
	)
	@GeneratedValue(generator = "concept_set_compare_job_diff_generator")
	private Integer id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "compare_job_id", nullable = false)
	private ConceptSetCompareJobEntity compareJob;

	@Column(name = "concept_set_id", nullable = false)
	private Integer conceptSetId;

	@Column(name = "concept_id", nullable = false)
	private Integer conceptId;

	@Column(name = "is_source_code", nullable = false)
	private Boolean isSourceCode = false;

	// Concept Set membership counts
	@Column(name = "concept_in_cs1_only")
	private Long conceptInCS1Only;

	@Column(name = "concept_in_cs2_only")
	private Long conceptInCS2Only;

	@Column(name = "concept_in_cs1_and_cs2")
	private Long conceptInCS1AndCS2;

	// Concept names from both vocabularies
	@Column(name = "vocab1_concept_name", length = 1000)
	private String vocab1ConceptName;

	@Column(name = "vocab2_concept_name", length = 1000)
	private String vocab2ConceptName;

	// Standard concept from both vocabularies
	@Column(name = "vocab1_standard_concept", length = 1)
	private String vocab1StandardConcept;

	@Column(name = "vocab2_standard_concept", length = 1)
	private String vocab2StandardConcept;

	// Invalid reason from both vocabularies
	@Column(name = "vocab1_invalid_reason", length = 1)
	private String vocab1InvalidReason;

	@Column(name = "vocab2_invalid_reason", length = 1)
	private String vocab2InvalidReason;

	// Concept code from both vocabularies
	@Column(name = "vocab1_concept_code", length = 50)
	private String vocab1ConceptCode;

	@Column(name = "vocab2_concept_code", length = 50)
	private String vocab2ConceptCode;

	// Domain ID from both vocabularies
	@Column(name = "vocab1_domain_id", length = 20)
	private String vocab1DomainId;

	@Column(name = "vocab2_domain_id", length = 20)
	private String vocab2DomainId;

	// Vocabulary ID from both vocabularies
	@Column(name = "vocab1_vocabulary_id", length = 20)
	private String vocab1VocabularyId;

	@Column(name = "vocab2_vocabulary_id", length = 20)
	private String vocab2VocabularyId;

	// Concept class ID from both vocabularies
	@Column(name = "vocab1_concept_class_id", length = 20)
	private String vocab1ConceptClassId;

	@Column(name = "vocab2_concept_class_id", length = 20)
	private String vocab2ConceptClassId;

	// Valid dates from both vocabularies
	@Column(name = "vocab1_valid_start_date")
	private Date vocab1ValidStartDate;

	@Column(name = "vocab2_valid_start_date")
	private Date vocab2ValidStartDate;

	@Column(name = "vocab1_valid_end_date")
	private Date vocab1ValidEndDate;

	@Column(name = "vocab2_valid_end_date")
	private Date vocab2ValidEndDate;

	// Mismatch flags
	@Column(name = "name_mismatch", nullable = false)
	private Boolean nameMismatch;

	@Column(name = "standard_concept_mismatch", nullable = false)
	private Boolean standardConceptMismatch;

	@Column(name = "invalid_reason_mismatch", nullable = false)
	private Boolean invalidReasonMismatch;

	@Column(name = "concept_code_mismatch", nullable = false)
	private Boolean conceptCodeMismatch;

	@Column(name = "domain_id_mismatch", nullable = false)
	private Boolean domainIdMismatch;

	@Column(name = "vocabulary_id_mismatch", nullable = false)
	private Boolean vocabularyIdMismatch;

	@Column(name = "concept_class_id_mismatch", nullable = false)
	private Boolean conceptClassIdMismatch;

	@Column(name = "valid_start_date_mismatch", nullable = false)
	private Boolean validStartDateMismatch;

	@Column(name = "valid_end_date_mismatch", nullable = false)
	private Boolean validEndDateMismatch;

	public ConceptSetCompareJobDiffEntity() {
	}

	// Getters and Setters
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

	public Integer getConceptId() {
		return conceptId;
	}

	public void setConceptId(Integer conceptId) {
		this.conceptId = conceptId;
	}

	public Boolean getIsSourceCode() {
		return isSourceCode;
	}

	public void setIsSourceCode(Boolean isSourceCode) {
		this.isSourceCode = isSourceCode;
	}

	public Long getConceptInCS1Only() {
		return conceptInCS1Only;
	}

	public void setConceptInCS1Only(Long conceptInCS1Only) {
		this.conceptInCS1Only = conceptInCS1Only;
	}

	public Long getConceptInCS2Only() {
		return conceptInCS2Only;
	}

	public void setConceptInCS2Only(Long conceptInCS2Only) {
		this.conceptInCS2Only = conceptInCS2Only;
	}

	public Long getConceptInCS1AndCS2() {
		return conceptInCS1AndCS2;
	}

	public void setConceptInCS1AndCS2(Long conceptInCS1AndCS2) {
		this.conceptInCS1AndCS2 = conceptInCS1AndCS2;
	}
	public String getVocab1ConceptName() {
		return vocab1ConceptName;
	}

	public void setVocab1ConceptName(String vocab1ConceptName) {
		this.vocab1ConceptName = vocab1ConceptName;
	}

	public String getVocab2ConceptName() {
		return vocab2ConceptName;
	}

	public void setVocab2ConceptName(String vocab2ConceptName) {
		this.vocab2ConceptName = vocab2ConceptName;
	}

	public String getVocab1StandardConcept() {
		return vocab1StandardConcept;
	}

	public void setVocab1StandardConcept(String vocab1StandardConcept) {
		this.vocab1StandardConcept = vocab1StandardConcept;
	}

	public String getVocab2StandardConcept() {
		return vocab2StandardConcept;
	}

	public void setVocab2StandardConcept(String vocab2StandardConcept) {
		this.vocab2StandardConcept = vocab2StandardConcept;
	}

	public String getVocab1InvalidReason() {
		return vocab1InvalidReason;
	}

	public void setVocab1InvalidReason(String vocab1InvalidReason) {
		this.vocab1InvalidReason = vocab1InvalidReason;
	}

	public String getVocab2InvalidReason() {
		return vocab2InvalidReason;
	}

	public void setVocab2InvalidReason(String vocab2InvalidReason) {
		this.vocab2InvalidReason = vocab2InvalidReason;
	}

	public String getVocab1ConceptCode() {
		return vocab1ConceptCode;
	}

	public void setVocab1ConceptCode(String vocab1ConceptCode) {
		this.vocab1ConceptCode = vocab1ConceptCode;
	}

	public String getVocab2ConceptCode() {
		return vocab2ConceptCode;
	}

	public void setVocab2ConceptCode(String vocab2ConceptCode) {
		this.vocab2ConceptCode = vocab2ConceptCode;
	}

	public String getVocab1DomainId() {
		return vocab1DomainId;
	}

	public void setVocab1DomainId(String vocab1DomainId) {
		this.vocab1DomainId = vocab1DomainId;
	}

	public String getVocab2DomainId() {
		return vocab2DomainId;
	}

	public void setVocab2DomainId(String vocab2DomainId) {
		this.vocab2DomainId = vocab2DomainId;
	}

	public String getVocab1VocabularyId() {
		return vocab1VocabularyId;
	}

	public void setVocab1VocabularyId(String vocab1VocabularyId) {
		this.vocab1VocabularyId = vocab1VocabularyId;
	}

	public String getVocab2VocabularyId() {
		return vocab2VocabularyId;
	}

	public void setVocab2VocabularyId(String vocab2VocabularyId) {
		this.vocab2VocabularyId = vocab2VocabularyId;
	}

	public String getVocab1ConceptClassId() {
		return vocab1ConceptClassId;
	}

	public void setVocab1ConceptClassId(String vocab1ConceptClassId) {
		this.vocab1ConceptClassId = vocab1ConceptClassId;
	}

	public String getVocab2ConceptClassId() {
		return vocab2ConceptClassId;
	}

	public void setVocab2ConceptClassId(String vocab2ConceptClassId) {
		this.vocab2ConceptClassId = vocab2ConceptClassId;
	}

	public Date getVocab1ValidStartDate() {
		return vocab1ValidStartDate;
	}

	public void setVocab1ValidStartDate(Date vocab1ValidStartDate) {
		this.vocab1ValidStartDate = vocab1ValidStartDate;
	}

	public Date getVocab2ValidStartDate() {
		return vocab2ValidStartDate;
	}

	public void setVocab2ValidStartDate(Date vocab2ValidStartDate) {
		this.vocab2ValidStartDate = vocab2ValidStartDate;
	}

	public Date getVocab1ValidEndDate() {
		return vocab1ValidEndDate;
	}

	public void setVocab1ValidEndDate(Date vocab1ValidEndDate) {
		this.vocab1ValidEndDate = vocab1ValidEndDate;
	}

	public Date getVocab2ValidEndDate() {
		return vocab2ValidEndDate;
	}

	public void setVocab2ValidEndDate(Date vocab2ValidEndDate) {
		this.vocab2ValidEndDate = vocab2ValidEndDate;
	}

	public Boolean getNameMismatch() {
		return nameMismatch;
	}

	public void setNameMismatch(Boolean nameMismatch) {
		this.nameMismatch = nameMismatch;
	}

	public Boolean getStandardConceptMismatch() {
		return standardConceptMismatch;
	}

	public void setStandardConceptMismatch(Boolean standardConceptMismatch) {
		this.standardConceptMismatch = standardConceptMismatch;
	}

	public Boolean getInvalidReasonMismatch() {
		return invalidReasonMismatch;
	}

	public void setInvalidReasonMismatch(Boolean invalidReasonMismatch) {
		this.invalidReasonMismatch = invalidReasonMismatch;
	}

	public Boolean getConceptCodeMismatch() {
		return conceptCodeMismatch;
	}

	public void setConceptCodeMismatch(Boolean conceptCodeMismatch) {
		this.conceptCodeMismatch = conceptCodeMismatch;
	}

	public Boolean getDomainIdMismatch() {
		return domainIdMismatch;
	}

	public void setDomainIdMismatch(Boolean domainIdMismatch) {
		this.domainIdMismatch = domainIdMismatch;
	}

	public Boolean getVocabularyIdMismatch() {
		return vocabularyIdMismatch;
	}

	public void setVocabularyIdMismatch(Boolean vocabularyIdMismatch) {
		this.vocabularyIdMismatch = vocabularyIdMismatch;
	}

	public Boolean getConceptClassIdMismatch() {
		return conceptClassIdMismatch;
	}

	public void setConceptClassIdMismatch(Boolean conceptClassIdMismatch) {
		this.conceptClassIdMismatch = conceptClassIdMismatch;
	}

	public Boolean getValidStartDateMismatch() {
		return validStartDateMismatch;
	}

	public void setValidStartDateMismatch(Boolean validStartDateMismatch) {
		this.validStartDateMismatch = validStartDateMismatch;
	}

	public Boolean getValidEndDateMismatch() {
		return validEndDateMismatch;
	}

	public void setValidEndDateMismatch(Boolean validEndDateMismatch) {
		this.validEndDateMismatch = validEndDateMismatch;
	}
}