package org.ohdsi.webapi.service.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AnnotationDTO {

    private Integer id;
    private String createdBy;
    private String createdDate;
    private String vocabularyVersion;
    private Integer conceptSetVersion;
    private String searchData;
    private String copiedFromConceptSetIds;
    private Integer conceptId;

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

    public Integer getConceptSetVersion() {
        return conceptSetVersion;
    }

    public void setConceptSetVersion(Integer conceptSetVersion) {
        this.conceptSetVersion = conceptSetVersion;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getSearchData() {
        return searchData;
    }

    public void setSearchData(String searchData) {
        this.searchData = searchData;
    }

    public Integer getConceptId() {
        return conceptId;
    }

    public void setConceptId(Integer conceptId) {
        this.conceptId = conceptId;
    }

    public String getCopiedFromConceptSetIds() {
        return copiedFromConceptSetIds;
    }

    public void setCopiedFromConceptSetIds(String copiedFromConceptSetIds) {
        this.copiedFromConceptSetIds = copiedFromConceptSetIds;
    }
}
