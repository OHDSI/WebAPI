package org.ohdsi.webapi.check.validator.common;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

public class DuplicateValidator<T, V> extends PredicateValidator<Collection<? extends T>> {
    private static final String DUPLICATE = "%s - duplicate values";

    private Function<T, V> elementGetter;

    public DuplicateValidator() {
        setPredicate(t -> {
            Set<V> set = new HashSet<V>();
            for (T value : t) {
                set.add(elementGetter.apply(value));
            }
            return set.size() == t.size();
        });
    }

    public DuplicateValidator<T, V> setElementGetter(Function<T, V> elementGetter) {
        this.elementGetter = elementGetter;
        return this;
    }

    protected String getDefaultErrorMessage() {
        return DUPLICATE;
    }
}
