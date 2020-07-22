package org.ohdsi.webapi.check.validator.common;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

public class DuplicateValidator<T, V> extends PredicateValidator<Collection<? extends T>> {
    private static final String DUPLICATE = "%s - duplicate values";

    private Function<T, V> elementGetter;

    public DuplicateValidator() {

        setPredicate(this::areAllUniqueValues);
    }


    public DuplicateValidator<T, V> setElementGetter(Function<T, V> elementGetter) {

        this.elementGetter = elementGetter;
        return this;
    }

    protected String getDefaultErrorMessage() {

        return DUPLICATE;
    }

    private boolean areAllUniqueValues(Collection<? extends T> t) {

        Set<V> set = new HashSet<>();
        for (T value : t) {
            V apply = elementGetter.apply(value);
            if (!set.add(apply)) {
                return false;
            }
        }
        return true;
    }

}
