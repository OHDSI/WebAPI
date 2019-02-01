package org.ohdsi.webapi.cohortcharacterization.converter;

import com.odysseusinc.arachne.commons.utils.ConverterUtils;
import org.ohdsi.webapi.cohortcharacterization.domain.CohortCharacterizationEntity;
import org.ohdsi.webapi.cohortcharacterization.dto.BaseCcDTO;
import org.ohdsi.webapi.cohortcharacterization.dto.CcParameterDTO;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public abstract class BaseCcToCcDTOConverter<T extends BaseCcDTO> extends BaseCcToCcShortDTOConverter<T> {

  @Autowired
  protected ConverterUtils converterUtils;

  @Override
  public T convert(CohortCharacterizationEntity source) {

    final T cohortCharacterizationDTO = super.convert(source);
    cohortCharacterizationDTO.setParameters(converterUtils.convertList(toList(source.getParameters()), CcParameterDTO.class));
    return cohortCharacterizationDTO;
  }

  protected List<?> toList(final Set<?> analyses) {
    return new ArrayList<>(analyses);
  }

}
