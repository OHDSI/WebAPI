package org.ohdsi.webapi.security.provisioning.converter;

import org.ohdsi.webapi.arachne.scheduler.api.v1.converter.BaseArachneJobToArachneJobDTOConverter;
import org.ohdsi.webapi.security.provisioning.model.UserImportJob;
import org.ohdsi.webapi.security.provisioning.model.UserImportJobDTO;


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
