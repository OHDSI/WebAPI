package org.ohdsi.webapi.check.validator;

@FunctionalInterface
public interface ValueAccessor<T> {
    Object get(T value);
}