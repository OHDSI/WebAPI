package org.ohdsi.webapi.cohortcharacterization.converter;

import com.odysseusinc.arachne.commons.utils.ConverterUtils;
import org.ohdsi.webapi.cohortcharacterization.domain.CohortCharacterizationEntity;
import org.ohdsi.webapi.cohortcharacterization.dto.BaseCcDTO;
import org.ohdsi.webapi.cohortcharacterization.dto.CcParameterDTO;
import org.ohdsi.webapi.cohortcharacterization.dto.CcStrataDTO;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class BaseCcToCcDTOConverter<T extends BaseCcDTO> extends BaseCcToCcShortDTOConverter<T> {

  @Autowired
  protected ConverterUtils converterUtils;

  @Override
  public T convert(CohortCharacterizationEntity source) {

    final T cohortCharacterizationDTO = super.convert(source);
    cohortCharacterizationDTO.setParameters(converterUtils.convertSet(source.getParameters(), CcParameterDTO.class));
    cohortCharacterizationDTO.setStratas(converterUtils.convertSet(source.getStratas(), CcStrataDTO.class));
    cohortCharacterizationDTO.setStratifiedBy(source.getStratifiedBy());
    cohortCharacterizationDTO.setStrataOnly(source.getStrataOnly());
    cohortCharacterizationDTO.setStrataConceptSets(source.getConceptSets());
    return cohortCharacterizationDTO;
  }

}
