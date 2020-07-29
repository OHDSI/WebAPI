package org.ohdsi.webapi.check;

import org.ohdsi.webapi.check.warning.Warning;

import java.util.List;

public interface Checker<T> {
    List<Warning> check(T value);
}
