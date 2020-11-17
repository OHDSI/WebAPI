package org.ohdsi.webapi.user.importer.dto;

import com.odysseusinc.scheduler.api.v1.dto.ArachneJobDTO;
import org.ohdsi.webapi.user.importer.model.LdapProviderType;
import org.ohdsi.webapi.user.importer.model.RoleGroupMapping;

import java.util.Date;

public class UserImportJobDTO extends ArachneJobDTO {
  private LdapProviderType providerType;

  private Boolean preserveRoles;

  private String userRoles;

  private Date lastExecuted;

  private Date nextExecution;

  private Date startDate;

  private RoleGroupMapping roleGroupMapping;

  public RoleGroupMapping getRoleGroupMapping() {
    return roleGroupMapping;
  }

  public void setRoleGroupMapping(RoleGroupMapping roleGroupMapping) {
    this.roleGroupMapping = roleGroupMapping;
  }

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

    @Override
    public Date getStartDate() {
        return startDate;
    }

    @Override
    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public String getUserRoles() {
        return userRoles;
    }

    public void setUserRoles(String userRoles) {
        this.userRoles = userRoles;
    }
}
