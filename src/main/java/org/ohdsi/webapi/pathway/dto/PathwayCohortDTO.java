package org.ohdsi.webapi.pathway.dto;

import org.ohdsi.webapi.cohortdefinition.dto.CohortMetadataDTO;

public class PathwayCohortDTO extends CohortMetadataDTO {

    private Integer cohortDefinitionId;

    public Integer getCohortDefinitionId() {

        return cohortDefinitionId;
    }

    public void setCohortDefinitionId(Integer cohortDefinitionId) {

        this.cohortDefinitionId = cohortDefinitionId;
    }
}
