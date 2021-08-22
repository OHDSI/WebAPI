package org.ohdsi.webapi.check.warning;

public class WarningUtils {
    public static Warning convertCirceWarning(org.ohdsi.circe.check.Warning circeWarning) {
        WarningSeverity severity = convertCirceWarningSeverity(
                ((org.ohdsi.circe.check.warnings.BaseWarning) circeWarning).getSeverity());
        Warning warning;
        if (circeWarning instanceof org.ohdsi.circe.check.warnings.ConceptSetWarning) {
            warning = new ConceptSetWarning(severity, circeWarning.toMessage(), ((org.ohdsi.circe.check.warnings.ConceptSetWarning) circeWarning).getConceptSet());
        } else {
            warning = new DefaultWarning(severity, circeWarning.toMessage());
        }
        return warning;
    }

    private static WarningSeverity convertCirceWarningSeverity(
            org.ohdsi.circe.check.WarningSeverity circeWarningSeverity) {
        WarningSeverity severity = WarningSeverity.WARNING;
        switch (circeWarningSeverity) {
            case CRITICAL: {
                severity = WarningSeverity.CRITICAL;
                break;
            }
            case INFO: {
                severity = WarningSeverity.INFO;
                break;
            }
        }
        return severity;
    }
}
