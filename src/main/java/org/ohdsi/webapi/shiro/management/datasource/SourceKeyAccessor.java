package org.ohdsi.webapi.shiro.management.datasource;

import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceRepository;
import org.springframework.stereotype.Component;

@Component
public class SourceKeyAccessor extends BaseDataSourceAccessor implements DataSourceAccessor<String> {

  private SourceRepository sourceRepository;

  public SourceKeyAccessor(SourceRepository sourceRepository) {
    this.sourceRepository = sourceRepository;
  }

  @Override
  public void checkAccess(String sourceKey) {
    Source source = sourceRepository.findBySourceKey(sourceKey);
    checkSourceAccess(source);
  }
}
