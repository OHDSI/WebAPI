package org.ohdsi.webapi.service.dto;

import java.util.List;

public class ConceptSetMetaDataDTO {
    
    private List<MetaDataDTO> newMetadata;
    
    private List<MetaDataDTO> removeMetadata;
    
    public List<MetaDataDTO> getNewMetadata() {
        return newMetadata;
    }
    
    public void setNewMetadata(List<MetaDataDTO> newMetadata) {
        this.newMetadata = newMetadata;
    }
    
    public List<MetaDataDTO> getRemoveMetadata() {
        return removeMetadata;
    }
    
    public void setRemoveMetadata(List<MetaDataDTO> removeMetadata) {
        this.removeMetadata = removeMetadata;
    }
    
}
