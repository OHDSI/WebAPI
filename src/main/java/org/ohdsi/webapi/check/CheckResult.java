package org.ohdsi.webapi.check;

import org.ohdsi.webapi.check.warning.Warning;

import java.util.List;

public class CheckResult {
    private List<Warning> warnings;

    public CheckResult(List<Warning> warnings) {
        this.warnings = warnings;
    }

    public List<Warning> getWarnings() {
        return warnings;
    }

    public void setWarnings(List<Warning> warnings) {
        this.warnings = warnings;
    }
}
