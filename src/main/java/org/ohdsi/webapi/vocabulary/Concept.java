package org.ohdsi.webapi.vocabulary;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.Date;
import java.util.Objects;

@JsonInclude()
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({
        "CONCEPT_ID", "CONCEPT_NAME", "STANDARD_CONCEPT",
        "STANDARD_CONCEPT_CAPTION", "INVALID_REASON", "INVALID_REASON_CAPTION",
        "CONCEPT_CODE", "DOMAIN_ID", "VOCABULARY_ID", "CONCEPT_CLASS_ID",
        "VALID_START_DATE", "VALID_END_DATE"
})
public class Concept extends org.ohdsi.circe.vocabulary.Concept {

    @JsonProperty("VALID_START_DATE")
    public Date validStartDate;

    @JsonProperty("VALID_END_DATE")
    public Date validEndDate;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Concept)) {
            return false;
        }
        final Concept other = (Concept) o;
        return Objects.equals(conceptId, other.conceptId) && Objects.equals(conceptName, other.conceptName) &&
                Objects.equals(standardConcept, other.standardConcept) && Objects.equals(invalidReason, other.invalidReason) &&
                Objects.equals(conceptCode, other.conceptCode) && Objects.equals(domainId, other.domainId) &&
                Objects.equals(vocabularyId, other.vocabularyId) && Objects.equals(conceptClassId, other.conceptClassId) &&
                Objects.equals(validStartDate, other.validStartDate) && Objects.equals(validEndDate, other.validEndDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(conceptId, conceptName, standardConcept, invalidReason, conceptCode,
                domainId, vocabularyId, conceptClassId, validStartDate, validEndDate);
    }
}
