package org.ohdsi.webapi.security.provisioning.converter;

import com.cronutils.model.definition.CronDefinition;
import org.ohdsi.webapi.security.provisioning.model.UserImportJobDTO;
import org.springframework.stereotype.Component;

@Component
public class UserImportJobDTOToUserImportJobConverter extends BaseUserImportJobDTOToUserImportJobConverter<UserImportJobDTO> {

  public UserImportJobDTOToUserImportJobConverter(CronDefinition cronDefinition) {
    super(cronDefinition);
  }

}
