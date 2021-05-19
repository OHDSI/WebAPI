package org.ohdsi.webapi.cohortdefinition.dto;

import org.ohdsi.webapi.versioning.dto.CohortVersionDTO;
import org.ohdsi.webapi.versioning.dto.VersionBaseDTO;

public class CohortVersionFullDTO {
    private CohortRawDTO cohortRawDTO;

    private VersionBaseDTO cohortVersionDTO;

    public CohortRawDTO getCohortRawDTO() {
        return cohortRawDTO;
    }

    public void setCohortRawDTO(CohortRawDTO cohortRawDTO) {
        this.cohortRawDTO = cohortRawDTO;
    }

    public VersionBaseDTO getCohortVersionDTO() {
        return cohortVersionDTO;
    }

    public void setCohortVersionDTO(VersionBaseDTO cohortVersionDTO) {
        this.cohortVersionDTO = cohortVersionDTO;
    }
}
