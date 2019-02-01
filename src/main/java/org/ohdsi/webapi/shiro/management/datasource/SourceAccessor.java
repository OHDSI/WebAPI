package org.ohdsi.webapi.shiro.management.datasource;

import org.ohdsi.webapi.source.Source;
import org.springframework.stereotype.Component;

@Component
public class SourceAccessor extends BaseDataSourceAccessor implements DataSourceAccessor<Source> {

  @Override
  public void checkAccess(Source source) {
    checkSourceAccess(source);
  }
}
