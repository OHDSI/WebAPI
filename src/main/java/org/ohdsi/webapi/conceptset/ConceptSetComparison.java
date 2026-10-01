package org.ohdsi.webapi.conceptset;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.sql.Date;

/**
 *
 * @author Anthony Sena <https://github.com/ohdsi>
 */
@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConceptSetComparison {
	@JsonProperty("isSourceCode")
	public boolean isSourceCode;
	
	@JsonProperty("conceptId")
	public Long conceptId;

	// Concept Set membership flags
	@JsonProperty("conceptInCS1Only")
	public Long conceptInCS1Only;

	@JsonProperty("conceptInCS2Only")
	public Long conceptInCS2Only;

	@JsonProperty("conceptInCS1AndCS2")
	public Long conceptInCS1AndCS2;

	// Concept names from both vocabularies
	@JsonProperty("vocab1ConceptName")
	public String vocab1ConceptName;

	@JsonProperty("vocab2ConceptName")
	public String vocab2ConceptName;

	// Standard concept from both vocabularies
	@JsonProperty("vocab1StandardConcept")
	public String vocab1StandardConcept;

	@JsonProperty("vocab2StandardConcept")
	public String vocab2StandardConcept;

	@JsonProperty("standardConcept")
	public String standardConcept;

	// Invalid reason from both vocabularies
	@JsonProperty("vocab1InvalidReason")
	public String vocab1InvalidReason;

	@JsonProperty("vocab2InvalidReason")
	public String vocab2InvalidReason;

	@JsonProperty("invalidReason")
	public String invalidReason;

	// Concept code from both vocabularies
	@JsonProperty("vocab1ConceptCode")
	public String vocab1ConceptCode;

	@JsonProperty("vocab2ConceptCode")
	public String vocab2ConceptCode;

	@JsonProperty("conceptCode")
	public String conceptCode;

	// Domain ID from both vocabularies
	@JsonProperty("vocab1DomainId")
	public String vocab1DomainId;

	@JsonProperty("vocab2DomainId")
	public String vocab2DomainId;

	@JsonProperty("domainId")
	public String domainId;

	// Vocabulary ID from both vocabularies
	@JsonProperty("vocab1VocabularyId")
	public String vocab1VocabularyId;

	@JsonProperty("vocab2VocabularyId")
	public String vocab2VocabularyId;

	@JsonProperty("vocabularyId")
	public String vocabularyId;

	// Concept class ID from both vocabularies
	@JsonProperty("vocab1ConceptClassId")
	public String vocab1ConceptClassId;

	@JsonProperty("vocab2ConceptClassId")
	public String vocab2ConceptClassId;

	@JsonProperty("conceptClassId")
	public String conceptClassId;

	// Valid start date from both vocabularies
	@JsonProperty("vocab1ValidStartDate")
	public Date vocab1ValidStartDate;

	@JsonProperty("vocab2ValidStartDate")
	public Date vocab2ValidStartDate;

	@JsonProperty("validStartDate")
	public Date validStartDate;

	// Valid end date from both vocabularies
	@JsonProperty("vocab1ValidEndDate")
	public Date vocab1ValidEndDate;

	@JsonProperty("vocab2ValidEndDate")
	public Date vocab2ValidEndDate;

	@JsonProperty("validEndDate")
	public Date validEndDate;

	// Mismatch flags for each field
	@JsonProperty("nameMismatch")
	public boolean nameMismatch;

	@JsonProperty("standardConceptMismatch")
	public boolean standardConceptMismatch;

	@JsonProperty("invalidReasonMismatch")
	public boolean invalidReasonMismatch;

	@JsonProperty("conceptCodeMismatch")
	public boolean conceptCodeMismatch;

	@JsonProperty("domainIdMismatch")
	public boolean domainIdMismatch;

	@JsonProperty("vocabularyIdMismatch")
	public boolean vocabularyIdMismatch;

	@JsonProperty("conceptClassIdMismatch")
	public boolean conceptClassIdMismatch;

	@JsonProperty("validStartDateMismatch")
	public boolean validStartDateMismatch;

	@JsonProperty("validEndDateMismatch")
	public boolean validEndDateMismatch;

	// Vocabulary info for vocab 1
	@JsonProperty("vocab1SourceKey")
	public String vocab1SourceKey;

	@JsonProperty("vocab1SourceName")
	public String vocab1SourceName;

	@JsonProperty("vocab1SourceVersion")
	public String vocab1SourceVersion;

	// Vocabulary info for vocab 2
	@JsonProperty("vocab2SourceKey")
	public String vocab2SourceKey;

	@JsonProperty("vocab2SourceName")
	public String vocab2SourceName;

	@JsonProperty("vocab2SourceVersion")
	public String vocab2SourceVersion;
}