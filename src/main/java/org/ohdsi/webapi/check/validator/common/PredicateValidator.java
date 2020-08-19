package org.ohdsi.webapi.check.validator.common;

import java.util.function.Predicate;
import org.ohdsi.webapi.check.validator.Context;
import org.ohdsi.webapi.check.validator.Path;
import org.ohdsi.webapi.check.validator.Validator;
import org.ohdsi.webapi.check.warning.WarningSeverity;

public class PredicateValidator<T> extends Validator<T> {
    private static final String INVALID = "invalid";

    protected Predicate<T> predicate;

    public PredicateValidator(Path path, WarningSeverity severity, String errorMessage, Predicate<T> predicate) {

        super(path, severity, errorMessage);
        this.predicate = predicate;
    }


    @Override
    public boolean validate(T value, Context context) {

        if (value == null) {
            return true;
        }

        boolean isValid = predicate.test(value);
        if (!isValid) {
            context.addWarning(getSeverity(), getErrorMessage(), path);
        }
        return isValid;
    }

    protected String getDefaultErrorMessage() {

        return INVALID;
    }
}
