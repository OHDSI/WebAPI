package org.ohdsi.webapi.pathway.dto;

import org.ohdsi.webapi.cohortdefinition.dto.CohortMetadataDTO;

public class PathwayCohortDTO extends CohortMetadataDTO {

    private Integer code;
    private Integer pathwayCohortId;

    public Integer getCode() {

        return code;
    }

    public void setCode(Integer code) {

        this.code = code;
    }

    public Integer getPathwayCohortId() {

        return pathwayCohortId;
    }

    public void setPathwayCohortId(Integer pathwayCohortId) {

        this.pathwayCohortId = pathwayCohortId;
    }
}
