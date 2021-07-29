package org.ohdsi.webapi.service.dto;

import org.ohdsi.circe.check.Warning;

import java.util.List;

public class CheckResultDTO {
    private List<Warning> warnings;

    public CheckResultDTO(List<Warning> warnings) {
        this.warnings = warnings;
    }

    public List<Warning> getWarnings() {

        return warnings;
    }

    public void setWarnings(List<Warning> warnings) {

        this.warnings = warnings;
    }
}

