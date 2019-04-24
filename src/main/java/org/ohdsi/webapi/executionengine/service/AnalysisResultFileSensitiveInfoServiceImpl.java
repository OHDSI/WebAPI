package org.ohdsi.webapi.executionengine.service;

import org.ohdsi.webapi.common.sensitiveinfo.AbstractSensitiveInfoService;
import org.ohdsi.webapi.executionengine.entity.AnalysisResultFile;
import org.springframework.stereotype.Service;

import javax.ws.rs.core.MediaType;
import java.util.Map;

@Service
public class AnalysisResultFileSensitiveInfoServiceImpl extends AbstractSensitiveInfoService implements AnalysisResultFileSensitiveInfoService {

  @Override
  public AnalysisResultFile filterSensitiveInfo(AnalysisResultFile source, Map<String, Object> variables, boolean isAdmin) {

    if (MediaType.TEXT_PLAIN.equals(source.getMediaType())) {
      final String value = filterSensitiveInfo(new String(source.getContents()), variables, isAdmin);
      source.setContents(value.getBytes());
    }
    return source;
  }
}
