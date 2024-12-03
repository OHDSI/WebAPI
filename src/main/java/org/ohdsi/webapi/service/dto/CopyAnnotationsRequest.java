package org.ohdsi.webapi.service.dto;

public class CopyAnnotationsRequest {

    private int sourceConceptSetId;
    private int targetConceptSetId;

    public int getSourceConceptSetId() {
        return sourceConceptSetId;
    }

    public int getTargetConceptSetId() {
        return targetConceptSetId;
    }

    public void setSourceConceptSetId(int sourceConceptSetId) {
        this.sourceConceptSetId = sourceConceptSetId;
    }

    public void setTargetConceptSetId(int targetConceptSetId) {
        this.targetConceptSetId = targetConceptSetId;
    }
}
