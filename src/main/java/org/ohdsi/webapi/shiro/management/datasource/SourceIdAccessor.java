package org.ohdsi.webapi.shiro.management.datasource;

import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceRepository;
import org.springframework.stereotype.Component;

@Component
public class SourceIdAccessor extends BaseDataSourceAccessor implements DataSourceAccessor<Integer> {

  private SourceRepository sourceRepository;

  public SourceIdAccessor(SourceRepository sourceRepository) {
    this.sourceRepository = sourceRepository;
  }

  @Override
  public void checkAccess(Integer sourceId) {
    Source source = sourceRepository.findBySourceId(sourceId);
    checkSourceAccess(source);
  }
}
