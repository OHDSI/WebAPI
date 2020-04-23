package org.ohdsi.webapi.check.validator.common;

import org.ohdsi.webapi.check.validator.ValueGetter;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class DuplicateValidator<T> extends PredicateValidator<Collection<? extends T>> {
    private static final String DUPLICATE = "%s - duplicate values";

    private ValueGetter<T> elementGetter;

    public DuplicateValidator() {
        setPredicate(t -> {
            Set<Object> set = new HashSet<>();
            for (T value : t) {
                set.add(elementGetter.get(value));
            }
            return set.size() == t.size();
        });
    }

    public DuplicateValidator<T> setElementGetter(ValueGetter<T> elementGetter) {
        this.elementGetter = elementGetter;
        return this;
    }

    protected String getDefaultErrorMessage() {
        return DUPLICATE;
    }
}
