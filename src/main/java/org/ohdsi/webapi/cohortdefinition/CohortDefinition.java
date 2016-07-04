/*
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ohdsi.webapi.cohortdefinition;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import javax.persistence.NamedSubgraph;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderColumn;
import javax.persistence.Table;

/**
 * JPA Entity for Cohort Definitions
 * @author cknoll1
 */
@Entity(name = "CohortDefinition")
@Table(name="cohort_definition")
@NamedEntityGraph(
    name = "CohortDefinition.withDetail",
    attributeNodes = { @NamedAttributeNode(value = "details", subgraph = "detailsGraph") },
    subgraphs = {@NamedSubgraph(name = "detailsGraph", type = CohortDefinitionDetails.class, attributeNodes = { @NamedAttributeNode(value="expression")})}
)
public class CohortDefinition implements Serializable{

  private static final long serialVersionUID = 1L;
    
  @Id
  @GeneratedValue
  @Access(AccessType.PROPERTY) 
  private Integer id;
  
  private String name;

  private String description;

  @Enumerated(EnumType.STRING)
  @Column(name="expression_type")  
  private ExpressionType expressionType;
  
  @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional=true, orphanRemoval = true)
  @JoinColumn(name="id")
  private CohortDefinitionDetails details;

  @OneToMany(fetch= FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "cohortDefinition")
  private Set<CohortGenerationInfo> generationInfoList;
  
  @Column(name="created_by")
  private String createdBy;
  
  @Column(name="created_date")
  private Date createdDate;

  @Column(name="modified_by")
  private String modifiedBy;
    
  @Column(name="modified_date")
  private Date modifiedDate;

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public CohortDefinition setName(String name) {
    this.name = name;
    return this;
  }

  public String getDescription() {
    return description;
  }

  public CohortDefinition setDescription(String description) {
    this.description = description;
    return this;
  }

  public ExpressionType getExpressionType() {
    return expressionType;
  }
  
  public CohortDefinition setExpressionType(ExpressionType expressionType) {
    this.expressionType = expressionType;
    return this;
  }
  
  public String getCreatedBy() {
    return createdBy;
  }

  public CohortDefinition setCreatedBy(String createdBy) {
    this.createdBy = createdBy;
    return this;
  }

  public Date getCreatedDate() {
    return createdDate;
  }

  public CohortDefinition setCreatedDate(Date createdDate) {
    this.createdDate = createdDate;
    return this;
  }

  public String getModifiedBy() {
    return modifiedBy;
  }

  public CohortDefinition setModifiedBy(String modifiedBy) {
    this.modifiedBy = modifiedBy;
    return this;
  }

  public Date getModifiedDate() {
    return modifiedDate;
  }

  public CohortDefinition setModifiedDate(Date modifiedDate) {
    this.modifiedDate = modifiedDate;
    return this;
  }
  
  public CohortDefinitionDetails getDetails() {
    return this.details;
  }

  public CohortDefinition setDetails(CohortDefinitionDetails details) {
    this.details = details;
    return this;
  }

  public Set<CohortGenerationInfo> getGenerationInfoList() {
    return this.generationInfoList;
  }
  
  public CohortDefinition setGenerationInfoList(Set<CohortGenerationInfo> list) {
    this.generationInfoList = list;
    return this;
  }
}
