package org.ohdsi.webapi.shiro.management.datasource;

import org.ohdsi.webapi.source.Source;
import org.springframework.stereotype.Component;

@Component
public class SourceAccessor extends BaseDataSourceAccessor<Source> {

  @Override
  protected Source extractSource(Source source) {

    return source;
  }
}
