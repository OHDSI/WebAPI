package org.ohdsi.webapi.check.warning;

@FunctionalInterface
public interface WarningReporter {
    void add(WarningSeverity severity, String template, Object... params);
}

