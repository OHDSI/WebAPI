package org.ohdsi.webapi.cohortcharacterization.converter;

import com.odysseusinc.arachne.commons.utils.ConverterUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.ohdsi.webapi.cohortcharacterization.domain.CohortCharacterizationEntity;
import org.ohdsi.webapi.cohortcharacterization.dto.BaseCcDTO;
import org.ohdsi.webapi.cohortcharacterization.dto.CcParameterDTO;
import org.ohdsi.webapi.cohortcharacterization.dto.CcStrataDTO;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;
import java.util.TreeSet;

public abstract class BaseCcToCcDTOConverter<T extends BaseCcDTO> extends BaseCcToCcShortDTOConverter<T> {

  @Autowired
  protected ConverterUtils converterUtils;

  @Override
  public T convert(CohortCharacterizationEntity source) {

    final T cohortCharacterizationDTO = super.convert(source);

    Set<CcParameterDTO> convertedParameters = new TreeSet<>((o1, o2) -> ObjectUtils.compare(o1.getId(), o2.getId()));
    convertedParameters.addAll(converterUtils.convertSet(source.getParameters(), CcParameterDTO.class));
    cohortCharacterizationDTO.setParameters(convertedParameters);

    Set<CcStrataDTO> convertedStratas = new TreeSet<>((o1, o2) -> ObjectUtils.compare(o1.getId(), o2.getId()));
    convertedStratas.addAll(converterUtils.convertSet(source.getStratas(), CcStrataDTO.class));
    cohortCharacterizationDTO.setStratas(convertedStratas);
    
    cohortCharacterizationDTO.setStratifiedBy(source.getStratifiedBy());
    cohortCharacterizationDTO.setStrataOnly(source.getStrataOnly());
    cohortCharacterizationDTO.setStrataConceptSets(source.getStrataConceptSets());
    return cohortCharacterizationDTO;
  }

}
