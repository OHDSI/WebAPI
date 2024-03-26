package org.ohdsi.webapi.service.dto;

public class ConceptSetDTO extends CommonEntityExtDTO {

  private Integer id;
  private String name;
  private String description;
  private String criteria;

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public void setCriteria(String criteria) {
    this.criteria = criteria;
  }
  public String getCriteria() {
    return criteria;
  }
}
