package org.ohdsi.webapi.shiro.management.datasource;

import org.apache.shiro.SecurityUtils;
import org.ohdsi.webapi.shiro.management.Security;
import org.ohdsi.webapi.source.Source;

import javax.ws.rs.ForbiddenException;

public abstract class BaseDataSourceAccessor {

  protected void checkSourceAccess(Source source) {
    if (!SecurityUtils.getSubject().isPermitted(String.format(Security.SOURCE_ACCESS_PERMISSION, source.getSourceKey()))){
      throw new ForbiddenException();
    }
  }

}
