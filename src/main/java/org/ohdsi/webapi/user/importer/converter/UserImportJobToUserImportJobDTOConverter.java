package org.ohdsi.webapi.user.importer.converter;

import org.ohdsi.webapi.user.importer.dto.UserImportJobDTO;
import org.ohdsi.webapi.user.importer.model.UserImportJob;
import org.springframework.stereotype.Component;

@Component
public class UserImportJobToUserImportJobDTOConverter extends BaseUserImportJobToUserImportJobDTOConverter<UserImportJobDTO> {

  @Override
  protected UserImportJobDTO createResultObject(UserImportJob userImportJob) {

    return new UserImportJobDTO();
  }
}
