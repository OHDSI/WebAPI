package org.ohdsi.webapi.security.provisioning.converter;


import org.ohdsi.webapi.security.provisioning.model.UserImportJob;
import org.ohdsi.webapi.security.provisioning.model.UserImportJobDTO;
import org.springframework.stereotype.Component;

@Component
public class UserImportJobToUserImportJobDTOConverter extends BaseUserImportJobToUserImportJobDTOConverter<UserImportJobDTO> {

  @Override
  protected UserImportJobDTO createResultObject(UserImportJob userImportJob) {

    return new UserImportJobDTO();
  }
}
