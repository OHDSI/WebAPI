package org.ohdsi.webapi.shiro.management.datasource;

import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceRepository;
import org.springframework.stereotype.Component;

@Component
public class SourceKeyAccessor extends BaseDataSourceAccessor<String> {

  private SourceRepository sourceRepository;

  public SourceKeyAccessor(SourceRepository sourceRepository) {
    this.sourceRepository = sourceRepository;
  }

  @Override
  protected Source extractSource(String sourceKey) {

    return sourceRepository.findBySourceKey(sourceKey);
  }
}
