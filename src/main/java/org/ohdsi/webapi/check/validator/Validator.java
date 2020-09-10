package org.ohdsi.webapi.check.validator;

import org.ohdsi.webapi.check.warning.WarningSeverity;

public abstract class Validator<T> {
    private final static String DEFAULT_ERROR_MESSAGE = "error";
    private final static WarningSeverity DEFAULT_SEVERITY = WarningSeverity.CRITICAL;

    protected Path path;
    protected WarningSeverity severity;
    protected String errorMessage;

    public Validator(Path path, WarningSeverity severity, String errorMessage) {

        this.path = path;
        this.severity = severity;
        this.errorMessage = errorMessage;
    }

    public abstract boolean validate(T value, Context context);

    protected String getErrorMessage() {

        return this.errorMessage != null ?
                this.errorMessage :
                getDefaultErrorMessage();
    }

    protected WarningSeverity getSeverity() {

        return this.severity != null ?
                this.severity :
                getDefaultSeverity();
    }

    protected String getDefaultErrorMessage() {

        return DEFAULT_ERROR_MESSAGE;
    }

    protected WarningSeverity getDefaultSeverity() {
        return DEFAULT_SEVERITY;
    }


}
