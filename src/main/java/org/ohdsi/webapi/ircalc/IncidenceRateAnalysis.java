/*
 * Copyright 2016 Observational Health Data Sciences and Informatics [OHDSI.org].
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
package org.ohdsi.webapi.ircalc;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 *
 * @author Chris Knoll <cknoll@ohdsi.org>
 */

@Entity(name = "IncidenceRateAnalysis")
@Table(name="ir_analysis")
public class IncidenceRateAnalysis implements Serializable {
  private static final long serialVersionUID = 1L;
  
  @Id
  @GeneratedValue
  @Column(name="id")
  @Access(AccessType.PROPERTY) 
  private Integer id; 
  
  @Column(name="name")
  private String name;
  
  @Column(name="description")
  private String description;
  
  @Column(name="created_by")
  private String createdBy;
  
  @Column(name="created_date")
  private Date createdDate;

  @Column(name="modified_by")
  private String modifiedBy;
    
  @Column(name="modified_date")
  private Date modifiedDate;
  
  @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional=true, orphanRemoval = true)
  @JoinColumn(name="id")
  private IncidenceRateAnalysisDetails details;
  
  @OneToMany(fetch= FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "analysis", orphanRemoval=true)
  private Set<ExecutionInfo> executionInfoList = new HashSet<>();

  public Integer getId() {
    return id;
  }

  public IncidenceRateAnalysis setId(Integer id) {
    this.id = id;
    return this;
  }

  public String getName() {
    return name;
  }

  public IncidenceRateAnalysis setName(String name) {
    this.name = name;
    return this;
  }

  public String getDescription() {
    return description;
  }

  public IncidenceRateAnalysis setDescription(String description) {
    this.description = description;
    return this;
  }

  public IncidenceRateAnalysisDetails getDetails() {
    return details;
  }
  
  public IncidenceRateAnalysis setDetails(IncidenceRateAnalysisDetails details) {
    this.details = details;
    return this;
  }

  public String getCreatedBy() {
    return createdBy;
  }

  public IncidenceRateAnalysis setCreatedBy(String createdBy) {
    this.createdBy = createdBy;
    return this;
  }

  public Date getCreatedDate() {
    return createdDate;
  }

  public IncidenceRateAnalysis setCreatedDate(Date createdDate) {
    this.createdDate = createdDate;
    return this;
  }

  public String getModifiedBy() {
    return modifiedBy;
  }

  public IncidenceRateAnalysis setModifiedBy(String modifiedBy) {
    this.modifiedBy = modifiedBy;
    return this;
  }

  public Date getModifiedDate() {
    return modifiedDate;
  }

  public IncidenceRateAnalysis setModifiedDate(Date modifiedDate) {
    this.modifiedDate = modifiedDate;
    return this;
  }

  public Set<ExecutionInfo> getExecutionInfoList() {
    return executionInfoList;
  }

  public IncidenceRateAnalysis setExecutionInfoList(Set<ExecutionInfo> executionInfoList) {
    this.executionInfoList = executionInfoList;
    return this;
  }
}
