package org.ohdsi.webapi.shiro.management.datasource;

@FunctionalInterface
public interface DataSourceAccessor<T> {
  void checkAccess(T value);
}
