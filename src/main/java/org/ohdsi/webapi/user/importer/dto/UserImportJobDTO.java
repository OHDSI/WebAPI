package org.ohdsi.webapi.user.importer.dto;

import com.odysseusinc.scheduler.api.v1.dto.ArachneJobDTO;
import org.ohdsi.webapi.user.importer.model.LdapProviderType;

import java.util.Date;

public class UserImportJobDTO extends ArachneJobDTO {
  private LdapProviderType providerType;

  private Boolean preserveRoles;

  private Date lastExecuted;

  private Date nextExecution;

  public LdapProviderType getProviderType() {
    return providerType;
  }

  public void setProviderType(LdapProviderType providerType) {
    this.providerType = providerType;
  }

  public Boolean getPreserveRoles() {
    return preserveRoles;
  }

  public void setPreserveRoles(Boolean preserveRoles) {
    this.preserveRoles = preserveRoles;
  }

  public Date getLastExecuted() {
    return lastExecuted;
  }

  public void setLastExecuted(Date lastExecuted) {
    this.lastExecuted = lastExecuted;
  }

  public Date getNextExecution() {
    return nextExecution;
  }

  public void setNextExecution(Date nextExecution) {
    this.nextExecution = nextExecution;
  }
}
