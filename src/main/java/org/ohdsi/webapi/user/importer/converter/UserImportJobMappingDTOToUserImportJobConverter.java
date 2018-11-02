package org.ohdsi.webapi.user.importer.converter;

import com.cronutils.model.definition.CronDefinition;
import org.ohdsi.webapi.user.importer.dto.UserImportJobMappingDTO;
import org.ohdsi.webapi.user.importer.model.UserImportJob;
import org.springframework.stereotype.Component;

@Component
public class UserImportJobMappingDTOToUserImportJobConverter extends BaseUserImportJobDTOToUserImportJobConverter<UserImportJobMappingDTO> {

  public UserImportJobMappingDTOToUserImportJobConverter(CronDefinition cronDefinition) {
    super(cronDefinition);
  }

  @Override
  protected void convertJob(UserImportJobMappingDTO source, UserImportJob target) {
    super.convertJob(source, target);
    target.setRoleGroupMapping(RoleGroupMappingConverter.convertRoleGroupMapping(source.getRoleGroupMapping()));
  }
}
