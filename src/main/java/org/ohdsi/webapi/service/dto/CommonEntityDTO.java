package org.ohdsi.webapi.service.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.ohdsi.webapi.user.dto.UserDTO;

import java.util.Date;
import org.ohdsi.webapi.CommonDTO;

@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class CommonEntityDTO implements CommonDTO {
  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  private UserDTO createdBy;
  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  private UserDTO modifiedBy;
  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  private Date createdDate;
  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  private Date modifiedDate;

  private boolean hasWriteAccess;
  private boolean hasReadAccess;

  public UserDTO getCreatedBy() {
    return createdBy;
  }

  public void setCreatedBy(UserDTO createdBy) {
    this.createdBy = createdBy;
  }

  public UserDTO getModifiedBy() {
    return modifiedBy;
  }

  public void setModifiedBy(UserDTO modifiedBy) {
    this.modifiedBy = modifiedBy;
  }

  public Date getCreatedDate() {
    return createdDate;
  }

  public void setCreatedDate(Date createdDate) {
    this.createdDate = createdDate;
  }

  public Date getModifiedDate() {
    return modifiedDate;
  }

  public void setModifiedDate(Date modifiedDate) {
    this.modifiedDate = modifiedDate;
  }

  public boolean isHasWriteAccess() {
    return hasWriteAccess;
  }

  public void setHasWriteAccess(boolean hasWriteAccess) {
    this.hasWriteAccess = hasWriteAccess;
  }

  public boolean isHasReadAccess() {
    return hasReadAccess;
  }

  public void setHasReadAccess(boolean hasReadAccess) {
    this.hasReadAccess = hasReadAccess;
  }
}
