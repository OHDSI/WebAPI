package org.ohdsi.webapi.common.sensitiveinfo;

import org.ohdsi.webapi.cohortdefinition.CohortGenerationInfo;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class CohortGenerationSensitiveInfoServiceImpl extends AbstractSensitiveInfoService implements CohortGenerationSensitiveInfoService {
  @Override
  public CohortGenerationInfo filterSensitiveInfo(CohortGenerationInfo source, Map<String, Object> variables) {

    String value = filterSensitiveInfo(source.getFailMessage(), variables);
    source.setFailMessage(value);
    return source;
  }
}
