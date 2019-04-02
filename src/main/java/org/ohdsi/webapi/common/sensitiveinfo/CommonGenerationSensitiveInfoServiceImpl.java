package org.ohdsi.webapi.common.sensitiveinfo;

import org.ohdsi.webapi.common.generation.CommonGenerationDTO;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class CommonGenerationSensitiveInfoServiceImpl<T extends CommonGenerationDTO> extends AbstractSensitiveInfoService implements CommonGenerationSensitiveInfoService<T> {
  @Override
  public T filterSensitiveInfo(T generation, Map<String, Object> variables) {

    String value = filterSensitiveInfo(generation.getExitMessage(), variables);
    generation.setExitMessage(value);
    return generation;
  }

}
