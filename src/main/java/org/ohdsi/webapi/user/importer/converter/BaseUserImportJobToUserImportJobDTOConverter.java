package org.ohdsi.webapi.user.importer.converter;

import com.odysseusinc.scheduler.api.v1.converter.BaseArachneJobToArachneJobDTOConverter;
import org.ohdsi.webapi.user.importer.dto.UserImportJobDTO;
import org.ohdsi.webapi.user.importer.model.UserImportJob;

public abstract class BaseUserImportJobToUserImportJobDTOConverter<T extends UserImportJobDTO> extends BaseArachneJobToArachneJobDTOConverter<UserImportJob, T> {

  @Override
  protected void convertJob(UserImportJob source, T target) {

    target.setProviderType(source.getProviderType());
    target.setPreserveRoles(source.getPreserveRoles());
    target.setUserRoles(source.getUserRoles());
    if (source.getRoleGroupMapping() != null) {
      target.setRoleGroupMapping(RoleGroupMappingConverter.convertRoleGroupMapping(source.getProviderType().getValue(),
              source.getRoleGroupMapping()));
    }
  }
}
