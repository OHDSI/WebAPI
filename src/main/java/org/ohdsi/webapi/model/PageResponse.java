package org.ohdsi.webapi.model;

import java.util.Collection;
import java.util.List;

public class PageResponse<T> {
  private int recordsTotal;
  private int recordsFiltered;
  private Collection<T> data;
  private String error;

  public int getRecordsTotal() {
    return recordsTotal;
  }

  public void setRecordsTotal(int recordsTotal) {
    this.recordsTotal = recordsTotal;
  }

  public int getRecordsFiltered() {
    return recordsFiltered;
  }

  public void setRecordsFiltered(int recordsFiltered) {
    this.recordsFiltered = recordsFiltered;
  }

  public Collection<T> getData() {
    return data;
  }

  public void setData(Collection<T> data) {
    this.data = data;
  }

  public String getError() {
    return error;
  }

  public void setError(String error) {
    this.error = error;
  }
}
