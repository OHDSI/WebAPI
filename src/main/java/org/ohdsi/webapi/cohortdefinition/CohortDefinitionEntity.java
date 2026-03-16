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

import java.util.Objects;
import java.util.Set;

import org.ohdsi.analysis.Cohort;
import org.ohdsi.circe.cohortdefinition.CohortExpression;
import org.ohdsi.webapi.model.CommonEntityExt;
import org.ohdsi.webapi.tag.domain.Tag;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.NamedAttributeNode;
import jakarta.persistence.NamedEntityGraph;
import jakarta.persistence.NamedSubgraph;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

/**
 * JPA Entity for Cohort Definitions
 * @author cknoll1
 */
@Entity(name = "CohortDefinition")
@Table(name="cohort_definition")
@NamedEntityGraph(
    name = "CohortDefinition.withDetail",
    attributeNodes = { @NamedAttributeNode(value = "details", subgraph = "detailsGraph") },
    subgraphs = {@NamedSubgraph(name = "detailsGraph", type = CohortDefinitionDetailsEntity.class, attributeNodes = { @NamedAttributeNode(value="expression")})}
)
public class CohortDefinitionEntity extends CommonEntityExt<Integer> implements Cohort{

  private static final long serialVersionUID = 1L;
    
  @Id
  @SequenceGenerator(name = "cohort_definition_generator", sequenceName = "cohort_definition_sequence", allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cohort_definition_seq")
  @Access(AccessType.PROPERTY)
  private Integer id;
  
  private String name;

  private String description;

  @Enumerated(EnumType.STRING)
  @Column(name="expression_type")
  private ExpressionType expressionType;
  
  @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = false, orphanRemoval = true, mappedBy="definition")
  @JoinColumn(name="id")
  private CohortDefinitionDetailsEntity details;

  @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "cohortDefinition")
  private Set<CohortGenerationInfo> generationInfoList;

  @ManyToMany(targetEntity = Tag.class, fetch = FetchType.LAZY)
  @JoinTable(name = "cohort_tag",
          joinColumns = @JoinColumn(name = "asset_id", referencedColumnName = "id"),
          inverseJoinColumns = @JoinColumn(name = "tag_id", referencedColumnName = "id"))
  private Set<Tag> tags;

  @Override
  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public CohortDefinitionEntity setName(String name) {
    this.name = name;
    return this;
  }

  public String getDescription() {
    return description;
  }

  public CohortDefinitionEntity setDescription(String description) {
    this.description = description;
    return this;
  }

  public ExpressionType getExpressionType() {
    return expressionType;
  }
  
  public CohortDefinitionEntity setExpressionType(ExpressionType expressionType) {
    this.expressionType = expressionType;
    return this;
  }

  public CohortDefinitionDetailsEntity getDetails() {
    return this.details;
  }

  public CohortDefinitionEntity setDetails(CohortDefinitionDetailsEntity details) {
    this.details = details;
    return this;
  }

  public Set<CohortGenerationInfo> getGenerationInfoList() {
    return this.generationInfoList;
  }
  
  public CohortDefinitionEntity setGenerationInfoList(Set<CohortGenerationInfo> list) {
    this.generationInfoList = list;
    return this;
  }

  @Override
  public boolean equals(final Object o) {

    if (this == o) return true;
    if (!(o instanceof CohortDefinitionEntity)) return false;
    final CohortDefinitionEntity that = (CohortDefinitionEntity) o;
    return Objects.equals(getId(), that.getId());
  }

  @Override
  public int hashCode() {

    return Objects.hash(getId());
  }

    @Override
    public CohortExpression getExpression() {

      return details != null ? details.getExpressionObject() : null;
    }


  @Override
  public Set<Tag> getTags() {
    return tags;
  }

  @Override
  public void setTags(Set<Tag> tags) {
    this.tags = tags;
  }
}
