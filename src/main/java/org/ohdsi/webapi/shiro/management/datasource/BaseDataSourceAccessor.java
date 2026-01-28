package org.ohdsi.webapi.shiro.management.datasource;

import org.apache.shiro.SecurityUtils;
import org.ohdsi.webapi.shiro.management.DisabledSecurity;
import org.ohdsi.webapi.shiro.management.Security;
import org.ohdsi.webapi.source.Source;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

public abstract class BaseDataSourceAccessor<T> implements DataSourceAccessor<T> {

  @Autowired(required = false)
  private DisabledSecurity disabledSecurity;

  public void checkAccess(T s) {
    if (!hasAccess(s)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }
  }

  public boolean hasAccess(T s) {
    if (disabledSecurity != null) {
      return true;
    }

    Source source = extractSource(s);
    if (source == null) {
      return false;
    }
    return SecurityUtils.getSubject().isPermitted(String.format(Security.SOURCE_ACCESS_PERMISSION, source.getSourceKey()));
  }

  protected abstract Source extractSource(T source);

}
