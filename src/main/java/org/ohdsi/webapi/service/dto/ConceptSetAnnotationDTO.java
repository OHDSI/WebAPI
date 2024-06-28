package org.ohdsi.webapi.service.dto;

import java.util.List;

public class ConceptSetAnnotationDTO {
    
    private List<AnnotationDTO> newAnnotation;
    
    private List<AnnotationDTO> removeAnnotation;
    
    public List<AnnotationDTO> getNewAnnotation() {
        return newAnnotation;
    }
    
    public void setNewAnnotation(List<AnnotationDTO> newAnnotation) {
        this.newAnnotation = newAnnotation;
    }
    
    public List<AnnotationDTO> getRemoveAnnotation() {
        return removeAnnotation;
    }
    
    public void setRemoveAnnotation(List<AnnotationDTO> removeAnnotation) {
        this.removeAnnotation = removeAnnotation;
    }
    
}
