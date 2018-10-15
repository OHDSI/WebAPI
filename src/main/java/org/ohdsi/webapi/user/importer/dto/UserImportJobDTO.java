package org.ohdsi.webapi.user.importer.dto;

import com.odysseusinc.scheduler.api.v1.dto.ArachneJobDTO;
import org.ohdsi.webapi.user.importer.model.LdapProviderType;

public class UserImportJobDTO extends ArachneJobDTO {
  private LdapProviderType providerType;

  public LdapProviderType getProviderType() {
    return providerType;
  }

  public void setProviderType(LdapProviderType providerType) {
    this.providerType = providerType;
  }
}
