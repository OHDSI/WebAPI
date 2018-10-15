package org.ohdsi.webapi.user.importer.converter;

import com.cronutils.model.definition.CronDefinition;
import com.odysseusinc.scheduler.api.v1.converter.BaseArachneJobDTOToArachneJobConverter;
import org.ohdsi.webapi.user.importer.dto.UserImportJobDTO;
import org.ohdsi.webapi.user.importer.model.UserImportJob;
import org.springframework.stereotype.Component;

@Component
public class UserImportJobDTOToUserImportJobConverter extends BaseArachneJobDTOToArachneJobConverter<UserImportJobDTO, UserImportJob> {

  public UserImportJobDTOToUserImportJobConverter(CronDefinition cronDefinition) {
    super(cronDefinition);
  }

  @Override
  protected void convertJob(UserImportJobDTO source, UserImportJob target) {

    target.setProviderType(source.getProviderType());
  }

  @Override
  protected UserImportJob createResultObject(UserImportJobDTO userImportJobDTO) {

    return new UserImportJob();
  }
}
