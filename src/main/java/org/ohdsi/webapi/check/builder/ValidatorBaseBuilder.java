package org.ohdsi.webapi.check.builder;

import org.ohdsi.webapi.check.validator.Path;
import org.ohdsi.webapi.check.warning.WarningSeverity;

public abstract class ValidatorBaseBuilder<T, V, R extends ValidatorBaseBuilder<T, V, R>> {

    protected Path basePath;
    protected String attrName;
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

    protected Path createChildPath() {
        return Path.createPath(this.basePath, this.attrName);
    }

    public Path getBasePath() {
        return basePath;
    }

    public String getErrorMessage() {

        return errorMessage;
    }
}
