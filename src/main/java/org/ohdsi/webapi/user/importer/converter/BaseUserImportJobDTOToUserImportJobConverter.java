package org.ohdsi.webapi.user.importer.converter;

import com.cronutils.model.definition.CronDefinition;
import com.odysseusinc.scheduler.api.v1.converter.BaseArachneJobDTOToArachneJobConverter;
import org.ohdsi.webapi.user.importer.dto.UserImportJobDTO;
import org.ohdsi.webapi.user.importer.model.UserImportJob;

public abstract class BaseUserImportJobDTOToUserImportJobConverter<T extends UserImportJobDTO> extends BaseArachneJobDTOToArachneJobConverter<T, UserImportJob> {

  protected BaseUserImportJobDTOToUserImportJobConverter(CronDefinition cronDefinition) {
    super(cronDefinition);
  }

  @Override
  protected void convertJob(T source, UserImportJob target) {

    target.setProviderType(source.getProviderType());
    target.setPreserveRoles(source.getPreserveRoles());
    target.setUserRoles(source.getUserRoles());
    if (source.getRoleGroupMapping() != null) {
      target.setRoleGroupMapping(RoleGroupMappingConverter.convertRoleGroupMapping(source.getRoleGroupMapping()));
    }
  }

  @Override
  protected UserImportJob createResultObject(T userImportJobDTO) {

    return new UserImportJob();
  }
}
