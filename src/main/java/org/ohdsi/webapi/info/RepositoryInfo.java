package org.ohdsi.webapi.info;

public class RepositoryInfo {
  private Integer milestoneId;
  private String releaseTag;

  public RepositoryInfo(Integer milestoneId, String releaseTag) {
    this.milestoneId = milestoneId;
    this.releaseTag = releaseTag;
  }

  public Integer getMilestoneId() {
    return milestoneId;
  }

  public void setMilestoneId(Integer milestoneId) {
    this.milestoneId = milestoneId;
  }

  public String getReleaseTag() {
    return releaseTag;
  }

  public void setReleaseTag(String releaseTag) {
    this.releaseTag = releaseTag;
  }
}
