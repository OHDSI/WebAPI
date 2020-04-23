package org.ohdsi.webapi.check.validator;

@FunctionalInterface
public interface ValueGetter<T, V> {
    V get(T value);
}