package org.ohdsi.webapi.service.dto;

import org.ohdsi.webapi.conceptset.search.ConceptSetReindexStatus;

public class ConceptSetReindexDTO {
    private ConceptSetReindexStatus status;

    public ConceptSetReindexDTO(final ConceptSetReindexStatus status) {
        this.status = status;
    }

    public ConceptSetReindexStatus getStatus() {
        return status;
    }

    public void setStatus(final ConceptSetReindexStatus status) {
        this.status = status;
    }
}
