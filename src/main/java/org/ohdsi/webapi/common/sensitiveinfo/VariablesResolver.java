package org.ohdsi.webapi.common.sensitiveinfo;

import java.util.Map;

@FunctionalInterface
public interface VariablesResolver<T> {

  Map<String, Object> resolveVariables(T data);
}
