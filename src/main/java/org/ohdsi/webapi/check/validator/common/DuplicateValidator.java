package org.ohdsi.webapi.check.validator.common;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import org.ohdsi.webapi.check.validator.Path;
import org.ohdsi.webapi.check.warning.WarningSeverity;

public class DuplicateValidator<T, V> extends PredicateValidator<Collection<? extends T>> {
    private static final String DUPLICATE = "duplicate values";

    private Function<T, V> elementGetter;

    public DuplicateValidator(Path path, WarningSeverity severity, String errorMessage, Predicate<Collection<? extends T>> predicate, Function<T, V> elementGetter) {

        super(path, severity, errorMessage, predicate);
        this.predicate = this::areAllUniqueValues;
        this.elementGetter = elementGetter;
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
