package org.ohdsi.webapi.check.validator;

import org.ohdsi.webapi.check.warning.WarningReporter;
import org.ohdsi.webapi.check.warning.WarningSeverity;

public abstract class Validator<T> {
    private final static String DEFAULT_ERROR_TEMPLATE = "%s - error";
    protected Path path;
    protected WarningReporter reporter;
    protected WarningSeverity severity = WarningSeverity.CRITICAL;
    private String errorTemplate;

    public Validator() {
    }

    public void setSeverity(WarningSeverity severity) {
        this.severity = severity;
    }

    public Validator setErrorTemplate(String errorTemplate) {
        this.errorTemplate = "%s - " + errorTemplate;
        return this;
    }

    protected String getDefaultErrorTemplate() {
        return DEFAULT_ERROR_TEMPLATE;
    }

    protected String getErrorTemplate() {
        return this.errorTemplate != null ? this.errorTemplate : getDefaultErrorTemplate();
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

    protected void fillErrorReport() {
        reporter.add(this.severity, getErrorTemplate(), path.getPath());
    }

    protected void fillErrorReport(String template) {
        reporter.add(this.severity, template, path.getPath());
    }

    public void build() {
        // do nothing
    }
}
