package org.ohdsi.webapi.check;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.ohdsi.webapi.check.warning.Warning;
import org.ohdsi.webapi.check.warning.WarningSeverity;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class CheckResult {
    private List<Warning> warnings;

    public CheckResult(List<Warning> warnings) {
        this.warnings = warnings;
    }

    @JsonProperty("warnings")
    public List<Warning> getWarnings() {
        return warnings;
    }

    public void setWarnings(List<Warning> warnings) {
        this.warnings = warnings;
    }

    public boolean hasCriticalErrors() {
        return getErrorsBySeverity(WarningSeverity.CRITICAL).size() > 0;
    }

    private List<Warning> getErrorsBySeverity(WarningSeverity severity) {
        if (Objects.nonNull(warnings) && Objects.nonNull(severity)) {
            return warnings.stream()
                    .filter(w -> severity.equals(w.getSeverity()))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}

