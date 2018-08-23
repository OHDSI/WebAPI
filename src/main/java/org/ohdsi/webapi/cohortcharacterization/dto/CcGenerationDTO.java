package org.ohdsi.webapi.cohortcharacterization.dto;

public class CcGenerationDTO {
    private Long id;
    private String status;
    private String sourceKey;

    public Long getId() {

        return id;
    }

    public void setId(final Long id) {

        this.id = id;
    }

    public String getStatus() {

        return status;
    }

    public void setStatus(final String status) {

        this.status = status;
    }

    public String getSourceKey() {

        return sourceKey;
    }

    public void setSourceKey(final String sourceKey) {

        this.sourceKey = sourceKey;
    }
}
