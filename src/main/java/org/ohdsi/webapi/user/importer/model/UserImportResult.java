package org.ohdsi.webapi.user.importer.model;

public class UserImportResult {
  private int created = 0;
  private int updated = 0;

  public UserImportResult() {
  }

  public UserImportResult(int created, int updated) {
    this.created = created;
    this.updated = updated;
  }

  public int getCreated() {
    return created;
  }

  public void setCreated(int created) {
    this.created = created;
  }

  public int getUpdated() {
    return updated;
  }

  public void setUpdated(int updated) {
    this.updated = updated;
  }

  public void incCreated() {
    this.created++;
  }

  public void incUpdated() {
    this.updated++;
  }

}
