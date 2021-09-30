package org.ohdsi.webapi.check.builder;

import org.ohdsi.webapi.check.validator.Path;
import org.ohdsi.webapi.check.warning.WarningSeverity;

import java.util.function.Function;
import java.util.function.Supplier;

public abstract class ValidatorBaseBuilder<T, V, R extends ValidatorBaseBuilder<T, V, R>> {

    protected Path basePath;
    protected String attrName;
    protected Function<T, String> attrNameValueGetter;
    protected WarningSeverity severity;
    protected String errorMessage;

    public abstract V build();

    public R basePath(Path basePath) {
        this.basePath = basePath;
        return (R)this;
    }

    public R attrName(String attrName) {

        this.attrName = attrName;
        return (R)this;
    }

    public R errorMessage(String errorMessage) {

        this.errorMessage = errorMessage;
        return (R)this;
    }

    public R severity(WarningSeverity severity) {
        this.severity = severity;
        return (R)this;
    }

    public R attrNameValueGetter(Function<T, String> attrNameValueGetter) {
        this.attrNameValueGetter = attrNameValueGetter;
        return (R)this;
    }

    protected Path createChildPath() {
        return Path.createPath(this.basePath, this.attrName);
    }

    public Path getBasePath() {
        return basePath;
    }

    public String getErrorMessage() {

        return errorMessage;
    }

    public String getAttrName() {
        return attrName;
    }

    public WarningSeverity getSeverity() {
        return severity;
    }
}
