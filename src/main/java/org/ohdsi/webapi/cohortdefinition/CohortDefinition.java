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
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import javax.persistence.NamedSubgraph;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.ohdsi.analysis.Cohort;
import org.ohdsi.circe.cohortdefinition.CohortExpression;
import org.ohdsi.webapi.cohortanalysis.CohortAnalysisGenerationInfo;
import org.ohdsi.webapi.cohortcharacterization.domain.CohortCharacterizationEntity;
import org.ohdsi.webapi.model.CommonEntity;
import org.ohdsi.webapi.model.CommonEntityExt;
import org.ohdsi.webapi.tag.domain.Tag;

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
public class CohortDefinition extends CommonEntityExt<Integer> implements Serializable, Cohort{

  private static final long serialVersionUID = 1L;
    
  @Id
  @GenericGenerator(
    name = "cohort_definition_generator",
    strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
    parameters = {
      @Parameter(name = "sequence_name", value = "cohort_definition_sequence"),
      @Parameter(name = "increment_size", value = "1")
    }
  )
  @GeneratedValue(generator = "cohort_definition_generator")
  @Access(AccessType.PROPERTY)
  private Integer id;
  
  private String name;

  private String description;

  @Enumerated(EnumType.STRING)
  @Column(name="expression_type")
  private ExpressionType expressionType;
  
  @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = false, orphanRemoval = true, mappedBy="definition")
  @JoinColumn(name="id")
  private CohortDefinitionDetails details;

  @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "cohortDefinition")
  private Set<CohortGenerationInfo> generationInfoList;

  @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "cohortDefinition")
  private Set<CohortAnalysisGenerationInfo> cohortAnalysisGenerationInfoList = new HashSet<>();

  @ManyToMany(targetEntity = CohortCharacterizationEntity.class, fetch = FetchType.LAZY)
  @JoinTable(name = "cc_cohort",
          joinColumns = @JoinColumn(name = "cohort_id", referencedColumnName = "id"),
          inverseJoinColumns = @JoinColumn(name = "cohort_characterization_id", referencedColumnName = "id"))
  private List<CohortCharacterizationEntity> cohortCharacterizations = new ArrayList<>();

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

  @Override
  public boolean equals(final Object o) {

    if (this == o) return true;
    if (!(o instanceof CohortDefinition)) return false;
    final CohortDefinition that = (CohortDefinition) o;
    return Objects.equals(getId(), that.getId());
  }

  @Override
  public int hashCode() {

    return Objects.hash(getId());
  }

  public Set<CohortAnalysisGenerationInfo> getCohortAnalysisGenerationInfoList() {
		return cohortAnalysisGenerationInfoList;
	}

	public void setCohortAnalysisGenerationInfoList(Set<CohortAnalysisGenerationInfo> cohortAnalysisGenerationInfoList) {
		this.cohortAnalysisGenerationInfoList = cohortAnalysisGenerationInfoList;
	}

    @Override
    public CohortExpression getExpression() {

      return details != null ? details.getExpressionObject() : null;
    }

  public List<CohortCharacterizationEntity> getCohortCharacterizations() {

    return cohortCharacterizations;
  }

  public void setCohortCharacterizations(final List<CohortCharacterizationEntity> cohortCharacterizations) {

    this.cohortCharacterizations = cohortCharacterizations;
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
