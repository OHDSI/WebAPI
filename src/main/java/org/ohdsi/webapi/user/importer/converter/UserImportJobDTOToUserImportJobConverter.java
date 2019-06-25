package org.ohdsi.webapi.user.importer.converter;

import com.cronutils.model.definition.CronDefinition;
import org.ohdsi.webapi.user.importer.dto.UserImportJobDTO;
import org.springframework.stereotype.Component;

@Component
public class UserImportJobDTOToUserImportJobConverter extends BaseUserImportJobDTOToUserImportJobConverter<UserImportJobDTO> {

  public UserImportJobDTOToUserImportJobConverter(CronDefinition cronDefinition) {
    super(cronDefinition);
  }

}
