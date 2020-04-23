package org.ohdsi.webapi.check.validator;

@FunctionalInterface
public interface ValueGetter<T> {
    Object get(T value);
}