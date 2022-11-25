package org.ohdsi.webapi.service.dto;

public class ConceptSetReindexDTO {
    private String status;
    private int maxCount;
    private int doneCount;

    public ConceptSetReindexDTO() {
    }

    public ConceptSetReindexDTO(final String status, final int maxCount, final int doneCount) {
        this.status = status;
        this.maxCount = maxCount;
        this.doneCount = doneCount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(final String status) {
        this.status = status;
    }

    public int getMaxCount() {
        return maxCount;
    }

    public void setMaxCount(final int maxCount) {
        this.maxCount = maxCount;
    }

    public int getDoneCount() {
        return doneCount;
    }

    public void setDoneCount(final int doneCount) {
        this.doneCount = doneCount;
    }
}
