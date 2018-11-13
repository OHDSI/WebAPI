package org.ohdsi.webapi.user.importer.dto;

import org.ohdsi.webapi.user.importer.model.LdapProviderType;

import java.util.Date;

public class JobHistoryItemDTO {
  private Long id;
  private Date startTime;
  private Date endTime;
  private String status;
  private String exitCode;
  private String exitMessage;
  private String author;
  private LdapProviderType providerType;
  private String jobTitle;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Date getStartTime() {
    return startTime;
  }

  public void setStartTime(Date startTime) {
    this.startTime = startTime;
  }

  public Date getEndTime() {
    return endTime;
  }

  public void setEndTime(Date endTime) {
    this.endTime = endTime;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getExitCode() {
    return exitCode;
  }

  public void setExitCode(String exitCode) {
    this.exitCode = exitCode;
  }

  public String getExitMessage() {
    return exitMessage;
  }

  public void setExitMessage(String exitMessage) {
    this.exitMessage = exitMessage;
  }

  public String getAuthor() {
    return author;
  }

  public void setAuthor(String author) {
    this.author = author;
  }

  public LdapProviderType getProviderType() {
    return providerType;
  }

  public void setProviderType(LdapProviderType providerType) {
    this.providerType = providerType;
  }

  public String getJobTitle() {
    return jobTitle;
  }

  public void setJobTitle(String jobTitle) {
    this.jobTitle = jobTitle;
  }
}
