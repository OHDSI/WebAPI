package org.ohdsi.webapi.shiro.management.datasource;

import org.ohdsi.webapi.cohortcharacterization.repository.CcGenerationEntityRepository;
import org.ohdsi.webapi.source.Source;
import org.springframework.stereotype.Component;

import javax.ws.rs.NotFoundException;

@Component
public class CcGenerationIdAccessor extends BaseDataSourceAccessor<Long> {

  private CcGenerationEntityRepository ccGenerationRepository;

  public CcGenerationIdAccessor(CcGenerationEntityRepository ccGenerationRepository) {
    this.ccGenerationRepository = ccGenerationRepository;
  }

  @Override
  protected Source extractSource(Long id) {

    return ccGenerationRepository.findById(id).orElseThrow(NotFoundException::new).getSource();
  }
}
