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
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.persistence.*;

import org.ohdsi.webapi.cohortanalysis.CohortAnalysisGenerationInfo;
import org.ohdsi.webapi.model.CommonEntity;
import org.ohdsi.webapi.shiro.Entities.UserEntity;

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
public class CohortDefinition extends CommonEntity implements Serializable{

  private static final long serialVersionUID = 1L;
    
  @Id
  @SequenceGenerator(name = "cohort_definition_seq",sequenceName = "cohort_definition_sequence", allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cohort_definition_seq")
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

  @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "cohortDefinition")
  private Set<CohortGenerationInfo> generationInfoList;

	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "cohortDefinition")
	private Set<CohortAnalysisGenerationInfo> cohortAnalysisGenerationInfoList = new HashSet<>();

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

	public Set<CohortAnalysisGenerationInfo> getCohortAnalysisGenerationInfoList() {
		return cohortAnalysisGenerationInfoList;
	}

	public void setCohortAnalysisGenerationInfoList(Set<CohortAnalysisGenerationInfo> cohortAnalysisGenerationInfoList) {
		this.cohortAnalysisGenerationInfoList = cohortAnalysisGenerationInfoList;
	}
	
}
