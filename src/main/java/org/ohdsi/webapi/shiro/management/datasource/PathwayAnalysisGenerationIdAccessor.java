package org.ohdsi.webapi.shiro.management.datasource;

import org.ohdsi.webapi.pathway.repository.PathwayAnalysisGenerationRepository;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.util.EntityUtils;
import org.springframework.stereotype.Component;

import javax.ws.rs.NotFoundException;

@Component
public class PathwayAnalysisGenerationIdAccessor extends BaseDataSourceAccessor<Long> {

  private PathwayAnalysisGenerationRepository repository;

  public PathwayAnalysisGenerationIdAccessor(PathwayAnalysisGenerationRepository repository) {
    this.repository = repository;
  }

  @Override
  protected Source extractSource(Long id) {

    return repository.findById(id, EntityUtils.fromAttributePaths("source")).orElseThrow(NotFoundException::new).getSource();
  }
}
