package org.ohdsi.webapi.check.validator;

import java.util.ArrayList;
import java.util.List;
import org.ohdsi.webapi.check.warning.WarningSeverity;

public class Context {

    private List<Warning> warnings = new ArrayList<>();

    public void addWarning(WarningSeverity severity, String message, Path path) {

        warnings.add(new Warning(severity, message, path));
    }

    public List<Warning> getWarnings() {

        return warnings;
    }

    public static class Warning {

        private WarningSeverity severity;
        private String message;
        private Path path;

        public Warning(WarningSeverity severity, String message, Path path) {

            this.severity = severity;
            this.message = message;
            this.path = path;
        }

        public WarningSeverity getSeverity() {

            return severity;
        }

        public String getMessage() {

            return message;
        }

        public Path getPath() {

            return path;
        }
    }


}
