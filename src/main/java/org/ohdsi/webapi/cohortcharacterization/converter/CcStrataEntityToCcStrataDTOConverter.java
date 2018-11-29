package org.ohdsi.webapi.cohortcharacterization.converter;

import com.odysseusinc.arachne.commons.converter.BaseConvertionServiceAwareConverter;
import org.ohdsi.webapi.cohortcharacterization.domain.CcStrataEntity;
import org.ohdsi.webapi.cohortcharacterization.dto.CcStrataDTO;
import org.springframework.stereotype.Component;

@Component
public class CcStrataEntityToCcStrataDTOConverter extends BaseConvertionServiceAwareConverter<CcStrataEntity, CcStrataDTO> {

  @Override
  protected CcStrataDTO createResultObject(CcStrataEntity ccStrataEntity) {
    return new CcStrataDTO();
  }

  @Override
  protected void convert(CcStrataEntity source, CcStrataDTO dto) {
    dto.setId(source.getId());
    dto.setName(source.getName());
    dto.setCriteria(source.getCriteria());
  }
}
