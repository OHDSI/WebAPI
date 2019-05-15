package org.ohdsi.webapi.common.sensitiveinfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public interface SensitiveInfoService<T> {

  T filterSensitiveInfo(T source, Map<String, Object> variables, boolean isAdmin);

  default T filterSensitiveInfo(T source, Map<String, Object> variables) {
    return filterSensitiveInfo(source, variables, isAdmin());
  }

  boolean isAdmin();

  default List<T> filterSensitiveInfo(List<T> source, Map<String, Object> variables, boolean isAdmin) {

    List<T> result = new ArrayList<>();
    for(T val : source) {
      result.add(filterSensitiveInfo(val, variables, isAdmin));
    }
    return result;
  }

  default List<T> filterSensitiveInfo(List<T> source, VariablesResolver<T> resolver) {

    if (Objects.isNull(resolver)) {
      throw new IllegalArgumentException("variable resolver is required");
    }
    List<T> result = new ArrayList<>();
    boolean isAdmin = isAdmin();
    for(T val : source) {
      result.add(filterSensitiveInfo(val, resolver.resolveVariables(val), isAdmin));
    }
    return result;
  }
}
