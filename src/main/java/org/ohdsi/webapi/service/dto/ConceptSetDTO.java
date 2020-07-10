package org.ohdsi.webapi.service.dto;

public class ConceptSetDTO extends CommonEntityDTO {

  private int id;
  private String name;

  public Integer getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}
