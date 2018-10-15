package org.ohdsi.webapi.user.importer.converter;

import com.odysseusinc.scheduler.api.v1.converter.BaseArachneJobToArachneJobDTOConverter;
import org.ohdsi.webapi.user.importer.dto.UserImportJobDTO;
import org.ohdsi.webapi.user.importer.model.UserImportJob;
import org.springframework.stereotype.Component;

@Component
public class UserImportJobToUserImportJobDTOConverter extends BaseArachneJobToArachneJobDTOConverter<UserImportJob, UserImportJobDTO> {

  @Override
  protected void convertJob(UserImportJob source, UserImportJobDTO target) {

    target.setProviderType(source.getProviderType());
  }

  @Override
  protected UserImportJobDTO createResultObject(UserImportJob userImportJob) {

    return new UserImportJobDTO();
  }
}
