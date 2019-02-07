package org.ohdsi.webapi.cohortcharacterization.converter;

import com.odysseusinc.arachne.commons.converter.BaseConvertionServiceAwareConverter;
import org.ohdsi.analysis.Utils;
import org.ohdsi.webapi.cohortcharacterization.domain.CcStrataEntity;
import org.ohdsi.webapi.cohortcharacterization.dto.CcStrataDTO;
import org.springframework.stereotype.Component;

@Component
public class CcStrataDTOToCcStrataEntityConverter extends BaseConvertionServiceAwareConverter<CcStrataDTO, CcStrataEntity> {

  @Override
  protected CcStrataEntity createResultObject(CcStrataDTO source) {
    return new CcStrataEntity();
  }

  @Override
  protected void convert(CcStrataDTO source, CcStrataEntity entity) {
    entity.setId(source.getId());
    entity.setName(source.getName());
    entity.setExpressionString(Utils.serialize(source.getCriteria()));
  }
}
