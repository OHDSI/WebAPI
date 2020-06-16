package org.ohdsi.webapi.check.validator.common;

import org.ohdsi.webapi.check.validator.Validator;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

public class NotNullNotEmptyValidator<T> extends Validator<T> {
    private static final String NULL_OR_EMPTY = "%s - null or empty";

    @Override
    public boolean validate(T value) {
        boolean isValid = true;
        if (Objects.isNull(value)) {
            isValid = false;
        } else {
            if (value instanceof Collection) {
                isValid = ((Collection) value).size() > 0;
            } else if (value instanceof String) {
                isValid = !((String) value).isEmpty();
            } else if (value.getClass().isArray()) {
                isValid = Array.getLength(value) > 0;
            } else if (value instanceof Map) {
                isValid = ((Map) value).size() > 0;
            }
        }
        if (!isValid) {
            fillErrorReport();
        }
        return isValid;
    }

    protected String getDefaultErrorMessage() {
        return NULL_OR_EMPTY;
    }
}
