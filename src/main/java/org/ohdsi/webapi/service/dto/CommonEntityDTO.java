package org.ohdsi.webapi.service.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

import java.util.Date;
import org.ohdsi.webapi.CommonDTO;
import org.ohdsi.webapi.security.authz.User;

@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class CommonEntityDTO implements CommonDTO, Serializable {
  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  private User createdBy;
  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  private User modifiedBy;
  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  private Date createdDate;
  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  private Date modifiedDate;

  private boolean writeAccess;
  private boolean readAccess;

  public User getCreatedBy() {
    return createdBy;
  }

  public void setCreatedBy(User createdBy) {
    this.createdBy = createdBy;
  }

  public User getModifiedBy() {
    return modifiedBy;
  }

  public void setModifiedBy(User modifiedBy) {
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

  public boolean isWriteAccess() {
    return writeAccess;
  }

  public void setWriteAccess(boolean hasWriteAccess) {
    this.writeAccess = hasWriteAccess;
  }

  public boolean isReadAccess() {
    return readAccess;
  }

  public void setReadAccess(boolean hasReadAccess) {
    this.readAccess = hasReadAccess;
  }
}
