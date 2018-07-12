package org.ohdsi.webapi.model;

public class FacetValue {
  private String value;
  private int count;

  public FacetValue() {
  }

  public FacetValue(String value, int count) {
    this.value = value;
    this.count = count;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public int getCount() {
    return count;
  }

  public void setCount(int count) {
    this.count = count;
  }
}
