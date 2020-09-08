package org.ohdsi.webapi.service.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.ohdsi.webapi.user.dto.UserDTO;

import java.util.Date;
import org.ohdsi.webapi.CommonDTO;

public abstract class CommonEntityDTO implements CommonDTO {

  private UserDTO createdBy;
  private UserDTO modifiedBy;
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
  private Date createdDate;
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
  private Date modifiedDate;
  private boolean hasWriteAccess;

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
}
