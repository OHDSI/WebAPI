package org.ohdsi.webapi.shiro.management.datasource;

public class AccessorParameterBinding<T> {
  private Integer parameterIndex;
  private DataSourceAccessor<T> dataSourceAccessor;

  public AccessorParameterBinding(Integer parameterIndex, DataSourceAccessor<T> dataSourceAccessor) {
    this.parameterIndex = parameterIndex;
    this.dataSourceAccessor = dataSourceAccessor;
  }

  public Integer getParameterIndex() {
    return parameterIndex;
  }

  public DataSourceAccessor<T> getDataSourceAccessor() {
    return dataSourceAccessor;
  }

}
