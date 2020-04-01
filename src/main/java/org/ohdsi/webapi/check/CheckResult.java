package org.ohdsi.webapi.check;

import org.ohdsi.webapi.check.warning.Warning;

import java.io.Serializable;
import java.util.List;

public class CheckResult<T extends Serializable> {
    private List<Warning> warnings;
    private T id;

    public CheckResult(T id, List<Warning> warnings) {
        this.id = id;
        this.warnings = warnings;
    }

    public List<Warning> getWarnings() {
        return warnings;
    }

    public void setWarnings(List<Warning> warnings) {
        this.warnings = warnings;
    }

    public T getId() {
        return id;
    }

    public void setId(T id) {
        this.id = id;
    }
}
