package org.ohdsi.webapi.check.validator;

import org.ohdsi.webapi.check.warning.WarningSeverity;

import java.util.function.Function;

public abstract class Validator<T> {
    private final static String DEFAULT_ERROR_MESSAGE = "error";
    private final static WarningSeverity DEFAULT_SEVERITY = WarningSeverity.CRITICAL;

    protected Path path;
    protected WarningSeverity severity;
    protected String errorMessage;
    protected Function<T, String> attrNameValueGetter;

    public Validator(Path path, WarningSeverity severity, String errorMessage) {

        this.path = path;
        this.severity = severity;
        this.errorMessage = errorMessage;
    }

    public Validator(Path path, WarningSeverity severity, String errorMessage, Function<T, String> attrNameValueGetter) {
        this(path, severity, errorMessage);
        this.attrNameValueGetter = attrNameValueGetter;
    }


    public abstract boolean validate(T value, Context context);

    protected String getErrorMessage(T value, Object... params) {
        StringBuilder sb = new StringBuilder();
        if (this.attrNameValueGetter != null) {
            sb.append("(")
                    .append(this.attrNameValueGetter.apply(value))
                    .append(") - ");
        }
        String msg = this.errorMessage != null ? this.errorMessage : getDefaultErrorMessage();
        if (params.length > 0) {
            msg = String.format(msg, params);
        }
        sb.append(msg);
        return sb.toString();
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
