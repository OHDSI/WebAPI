package org.ohdsi.webapi.check.validator.common;

import java.util.Collection;
import org.ohdsi.webapi.check.validator.Context;
import org.ohdsi.webapi.check.validator.Path;
import org.ohdsi.webapi.check.validator.Validator;
import org.ohdsi.webapi.check.warning.WarningSeverity;

public class IterableForEachValidator<T> extends Validator<Collection<? extends T>> {
    final private Validator<T> validator;

    public IterableForEachValidator(Path path, WarningSeverity severity, String errorMessage, Validator<T> validator) {

        super(path, severity, errorMessage);
        this.validator = validator;
    }

    @Override
    public boolean validate(Collection<? extends T> value, Context context) {

        if (value == null) {
            return true;
        }

        return value.stream()
                .map(item -> validator.validate(item, context))
                .reduce(true, (left, right) -> left && right);
    }

}
