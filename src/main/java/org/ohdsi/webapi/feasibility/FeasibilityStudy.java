/*
 * Copyright 2015 Observational Health Data Sciences and Informatics [OHDSI.org].
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
package org.ohdsi.webapi.feasibility;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapsId;
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import javax.persistence.NamedEntityGraphs;
import javax.persistence.NamedSubgraph;
import javax.persistence.OneToOne;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import org.ohdsi.webapi.cohortdefinition.CohortDefinition;
import org.ohdsi.webapi.cohortdefinition.CohortDefinitionDetails;

/**
 *
 * @author Chris Knoll <cknoll@ohdsi.org>
 */

@Entity(name = "FeasibilityStudy")
@Table(name="feasibility_study")
@NamedEntityGraphs({
  @NamedEntityGraph(
    name = "FeasibilityStudy.forEdit",
    attributeNodes = { 
      @NamedAttributeNode(value = "inclusionRules"),
    }
  ),
  @NamedEntityGraph(
      name = "FeasibilityStudy.forInfo",
      attributeNodes = { 
        @NamedAttributeNode(value = "info")
      }
  )
})
public class FeasibilityStudy {
  
  @Id
  @GeneratedValue
  @Column(name="id")
  private Integer id; 
  
  @Column(name="name")
  private String name;
  
  @Column(name="description")
  private String description;
  
  @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  @JoinColumn(name="index_def_id")
  private CohortDefinition indexRule;
  
  @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  @JoinColumn(name="result_def_id")
  private CohortDefinition resultRule;  
  
  @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, optional = true, orphanRemoval = true)
  @JoinColumn(name="generate_info_id")
  private StudyInfo info;  
   
  @Column(name="created_by")
  private String createdBy;
  
  @Column(name="created_date")
  private Date createdDate;

  @Column(name="modified_by")
  private String modifiedBy;
    
  @Column(name="modified_date")
  private Date modifiedDate;
  
  @ElementCollection
  @CollectionTable(name = "feasibility_inclusion", joinColumns = @JoinColumn(name = "study_id"))
  @OrderColumn(name="sequence")
  private List<InclusionRule> inclusionRules = new ArrayList<InclusionRule>(); 

  public Integer getId() {
    return id;
  }

  public FeasibilityStudy setId(Integer id) {
    this.id = id;
    return this;
  }

  public String getName() {
    return name;
  }

  public FeasibilityStudy setName(String name) {
    this.name = name;
    return this;
  }

  public String getDescription() {
    return description;
  }

  public FeasibilityStudy setDescription(String description) {
    this.description = description;
    return this;
  }
  
  public CohortDefinition getIndexRule() {
    return indexRule;
  }

  public FeasibilityStudy setIndexRule(CohortDefinition indexRule) {
    this.indexRule = indexRule;
    return this;
  }

  public CohortDefinition getResultRule() {
    return resultRule;
  }

  public FeasibilityStudy setResultRule(CohortDefinition resultRule) {
    this.resultRule = resultRule;
    return this;
  }
  
  public StudyInfo getInfo() {
    return info;
  }

  public FeasibilityStudy setInfo(StudyInfo info) {
    this.info = info;
    return this;
  }  

  public String getCreatedBy() {
    return createdBy;
  }

  public FeasibilityStudy setCreatedBy(String createdBy) {
    this.createdBy = createdBy;
    return this;
  }

  public Date getCreatedDate() {
    return createdDate;
  }

  public FeasibilityStudy setCreatedDate(Date createdDate) {
    this.createdDate = createdDate;
    return this;
  }

  public String getModifiedBy() {
    return modifiedBy;
  }

  public FeasibilityStudy setModifiedBy(String modifiedBy) {
    this.modifiedBy = modifiedBy;
    return this;
  }

  public Date getModifiedDate() {
    return modifiedDate;
  }

  public FeasibilityStudy setModifiedDate(Date modifiedDate) {
    this.modifiedDate = modifiedDate;
    return this;
  }

  public List<InclusionRule> getInclusionRules() {
    return inclusionRules;
  }

  public FeasibilityStudy setInclusionRules(List<InclusionRule> inclusionRules) {
    this.inclusionRules = inclusionRules;
    return this;
  }
}
