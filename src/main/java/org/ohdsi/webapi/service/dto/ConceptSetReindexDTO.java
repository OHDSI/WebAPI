package org.ohdsi.webapi.service.dto;

import org.ohdsi.webapi.conceptset.search.ConceptSetReindexStatus;

public class ConceptSetReindexDTO {
    private ConceptSetReindexStatus status;
    private int maxCount;
    private int doneCount;
    private long executionId;

    public ConceptSetReindexDTO(final ConceptSetReindexStatus status) {
        this.status = status;
    }

    public ConceptSetReindexDTO(final ConceptSetReindexStatus status, final long executionId) {
        this.status = status;
        this.executionId = executionId;
    }

    public ConceptSetReindexStatus getStatus() {
        return status;
    }

    public void setStatus(final ConceptSetReindexStatus status) {
        this.status = status;
    }

    public int getMaxCount() {
        return maxCount;
    }

    public void setMaxCount(int maxCount) {
        this.maxCount = maxCount;
    }

    public int getDoneCount() {
        return doneCount;
    }

    public void setDoneCount(int doneCount) {
        this.doneCount = doneCount;
    }

    public long getExecutionId() {
        return executionId;
    }

    public void setExecutionId(long executionId) {
        this.executionId = executionId;
    }
}
