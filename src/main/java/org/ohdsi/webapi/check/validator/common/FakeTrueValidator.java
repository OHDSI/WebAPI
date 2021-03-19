package org.ohdsi.webapi.check.validator.common;

import org.ohdsi.webapi.check.validator.Context;
import org.ohdsi.webapi.check.validator.Validator;

public class FakeTrueValidator<T> extends Validator<T> {
    public FakeTrueValidator() {
        super(null, null, null);
    }

    @Override
    public boolean validate(T value, Context context) {
        return true;
    }
}
