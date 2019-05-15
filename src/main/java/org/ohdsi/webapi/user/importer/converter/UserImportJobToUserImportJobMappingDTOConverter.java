package org.ohdsi.webapi.user.importer.converter;

import org.ohdsi.webapi.user.importer.dto.UserImportJobMappingDTO;
import org.ohdsi.webapi.user.importer.model.UserImportJob;
import org.springframework.stereotype.Component;

@Component
public class UserImportJobToUserImportJobMappingDTOConverter extends BaseUserImportJobToUserImportJobDTOConverter<UserImportJobMappingDTO> {

  @Override
  protected void convertJob(UserImportJob source, UserImportJobMappingDTO target) {
     super.convertJob(source, target);
     target.setRoleGroupMapping(RoleGroupMappingConverter.convertRoleGroupMapping(source.getProviderType().getValue(),
             source.getRoleGroupMapping()));
  }

  @Override
  protected UserImportJobMappingDTO createResultObject(UserImportJob userImportJob) {

    return new UserImportJobMappingDTO();
  }
}
