package org.ohdsi.webapi.service.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AnnotationDTO extends AnnotationDetailsDTO {

    private String createdBy;
    private String createdDate;
    private String vocabularyVersion;
    private String conceptSetVersion;

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public String getVocabularyVersion() {
        return vocabularyVersion;
    }

    public void setVocabularyVersion(String vocabularyVersion) {
        this.vocabularyVersion = vocabularyVersion;
    }

    public String getConceptSetVersion() {
        return conceptSetVersion;
    }

    public void setConceptSetVersion(String conceptSetVersion) {
        this.conceptSetVersion = conceptSetVersion;
    }
}
