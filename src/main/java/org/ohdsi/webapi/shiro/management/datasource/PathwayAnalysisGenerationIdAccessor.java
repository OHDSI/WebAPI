package org.ohdsi.webapi.shiro.management.datasource;

import org.ohdsi.webapi.cohortcharacterization.domain.CcGenerationEntity;
import org.ohdsi.webapi.pathway.repository.PathwayAnalysisGenerationRepository;
import org.springframework.stereotype.Component;

import javax.ws.rs.NotFoundException;

@Component
public class PathwayAnalysisGenerationIdAccessor extends BaseDataSourceAccessor implements DataSourceAccessor<Long> {

  private PathwayAnalysisGenerationRepository repository;

  public PathwayAnalysisGenerationIdAccessor(PathwayAnalysisGenerationRepository repository) {
    this.repository = repository;
  }

  @Override
  public void checkAccess(Long id) {
    CcGenerationEntity generationEntity = repository.findById(id).orElseThrow(NotFoundException::new);
    checkSourceAccess(generationEntity.getSource());
  }
}
