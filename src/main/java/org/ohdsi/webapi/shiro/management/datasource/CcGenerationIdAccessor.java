package org.ohdsi.webapi.shiro.management.datasource;

import org.ohdsi.webapi.cohortcharacterization.domain.CcGenerationEntity;
import org.ohdsi.webapi.cohortcharacterization.repository.CcGenerationEntityRepository;
import org.springframework.stereotype.Component;

import javax.ws.rs.NotFoundException;

@Component
public class CcGenerationIdAccessor extends BaseDataSourceAccessor implements DataSourceAccessor<Long> {

  private CcGenerationEntityRepository ccGenerationRepository;

  public CcGenerationIdAccessor(CcGenerationEntityRepository ccGenerationRepository) {
    this.ccGenerationRepository = ccGenerationRepository;
  }

  @Override
  public void checkAccess(Long id) {
    CcGenerationEntity generationEntity = ccGenerationRepository.findById(id).orElseThrow(NotFoundException::new);
    checkSourceAccess(generationEntity.getSource());
  }
}
