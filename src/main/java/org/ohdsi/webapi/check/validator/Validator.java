package org.ohdsi.webapi.check.validator;

import org.ohdsi.webapi.check.warning.WarningReporter;
import org.ohdsi.webapi.check.warning.WarningSeverity;

public abstract class Validator<T> {
    private final static String DEFAULT_ERROR_MESSAGE = "%s - error";
    protected Path path;
    protected WarningReporter reporter;
    protected WarningSeverity severity = WarningSeverity.CRITICAL;
    private String errorMessage;

    protected void fillErrorReport() {
        reporter.add(this.severity, getErrorMessage(), path.getPath());
    }

    public void configure() {
        // do nothing
    }

    public void setSeverity(WarningSeverity severity) {
        this.severity = severity;
    }

    public Validator<T> setErrorMessage(String errorMessage) {
        this.errorMessage = "%s - " + errorMessage;
        return this;
    }

    protected String getDefaultErrorMessage() {
        return DEFAULT_ERROR_MESSAGE;
    }

    protected String getErrorMessage() {
        return this.errorMessage != null ? this.errorMessage : getDefaultErrorMessage();
    }

    public abstract boolean validate(T value);

    protected Path createPath(String attrName) {
        return Path.createPath(this.path, attrName);
    }

    protected Path createPath() {
        return Path.createPath(this.path, null);
    }

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }

    public WarningReporter getReporter() {
        return reporter;
    }

    public void setReporter(WarningReporter reporter) {
        this.reporter = reporter;
    }

    public boolean isErrorMessageInitial() {
        return getDefaultErrorMessage().equals(this.errorMessage);
    }
}
