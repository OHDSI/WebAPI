package org.ohdsi.webapi.service.dto;

import org.ohdsi.webapi.conceptset.metadata.ConceptSetMetaDataRepository;

public class MetaDataDTO {
    private Integer id;
    
    private String searchData;
    
    private String relatedConcepts;
    
    private String conceptHierarchy;
    
    private String conceptSetData;
    
    private String conceptData;
    
    private Integer conceptId;

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
    
    public String getRelatedConcepts() {
        return relatedConcepts;
    }
    
    public void setRelatedConcepts(String relatedConcepts) {
        this.relatedConcepts = relatedConcepts;
    }
    
    public String getConceptHierarchy() {
        return conceptHierarchy;
    }
    
    public void setConceptHierarchy(String conceptHierarchy) {
        this.conceptHierarchy = conceptHierarchy;
    }
    
    public String getConceptSetData() {
        return conceptSetData;
    }
    
    public void setConceptSetData(String conceptSetData) {
        this.conceptSetData = conceptSetData;
    }
    
    public String getConceptData() {
        return conceptData;
    }
    
    public void setConceptData(String conceptData) {
        this.conceptData = conceptData;
    }
    
    public Integer getConceptId() {
        return conceptId;
    }
    
    public void setConceptId(Integer conceptId) {
        this.conceptId = conceptId;
    }

}
