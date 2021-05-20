package org.ohdsi.webapi.cohortdefinition.dto;

import org.ohdsi.webapi.versioning.dto.VersionDTO;

public class CohortVersionFullDTO {
    private CohortRawDTO cohortRawDTO;

    private VersionDTO cohortVersionDTO;

    public CohortRawDTO getCohortRawDTO() {
        return cohortRawDTO;
    }

    public void setCohortRawDTO(CohortRawDTO cohortRawDTO) {
        this.cohortRawDTO = cohortRawDTO;
    }

    public VersionDTO getCohortVersionDTO() {
        return cohortVersionDTO;
    }

    public void setCohortVersionDTO(VersionDTO cohortVersionDTO) {
        this.cohortVersionDTO = cohortVersionDTO;
    }
}
