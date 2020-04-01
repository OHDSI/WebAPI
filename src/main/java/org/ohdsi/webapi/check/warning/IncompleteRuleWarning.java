package org.ohdsi.webapi.check.warning;

import org.ohdsi.circe.check.WarningSeverity;
import org.ohdsi.circe.check.warnings.BaseWarning;

public class IncompleteRuleWarning extends BaseWarning {
    private static final String INCOMPLETE_ERROR = "Inclusion rule %s.";

    private final String ruleName;

    public IncompleteRuleWarning(WarningSeverity severity, String ruleName) {
        super(severity);
        this.ruleName = ruleName;
    }

    public String getRuleName() {

        return ruleName;
    }

    @Override
    public String toMessage() {

        return String.format(INCOMPLETE_ERROR, ruleName);
    }
}
