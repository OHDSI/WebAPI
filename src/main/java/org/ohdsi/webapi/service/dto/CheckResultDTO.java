package org.ohdsi.webapi.service.dto;

import org.ohdsi.circe.check.Warning;

import java.util.List;

public class CheckResultDTO {
    private Integer cohortDefinitionId;
    private List<Warning> warnings;

    public CheckResultDTO(Integer cohortDefinitionId, List<Warning> warnings) {

        this.cohortDefinitionId = cohortDefinitionId;
        this.warnings = warnings;
    }

    public Integer getCohortDefinitionId() {

        return cohortDefinitionId;
    }

    public void setCohortDefinitionId(Integer cohortDefinitionId) {

        this.cohortDefinitionId = cohortDefinitionId;
    }

    public List<Warning> getWarnings() {

        return warnings;
    }

    public void setWarnings(List<Warning> warnings) {

        this.warnings = warnings;
    }
}

