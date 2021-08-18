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
import java.util.HashSet;
import java.util.Set;
import javax.persistence.*;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.ohdsi.webapi.model.CommonEntity;
import org.ohdsi.webapi.model.CommonEntityExt;
import org.ohdsi.webapi.tag.domain.Tag;

/**
 *
 * @author Chris Knoll <cknoll@ohdsi.org>
 */

@Entity(name = "IncidenceRateAnalysis")
@Table(name="ir_analysis")
@NamedEntityGraphs({
  @NamedEntityGraph(
          name = "IncidenceRateAnalysis.withExecutionInfoList",
          attributeNodes = @NamedAttributeNode("executionInfoList")
  )
})
public class IncidenceRateAnalysis extends CommonEntityExt<Integer> implements Serializable {
  private static final long serialVersionUID = 1L;
  
  @Id
  @GenericGenerator(
      name = "ir_analysis_generator",
      strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
      parameters = {
          @Parameter(name = "sequence_name", value = "ir_analysis_sequence"),
          @Parameter(name = "increment_size", value = "1")
      }
  )
  @GeneratedValue(generator = "ir_analysis_generator")
  @Column(name="id")
  @Access(AccessType.PROPERTY)
  private Integer id; 
  
  @Column(name="name")
  private String name;
  
  @Column(name="description")
  private String description;

  @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional=true, orphanRemoval = true, mappedBy="analysis")
  @JoinColumn(name="id")
  private IncidenceRateAnalysisDetails details;
  
  @OneToMany(fetch= FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "analysis", orphanRemoval=true)
  private Set<ExecutionInfo> executionInfoList = new HashSet<>();

  @ManyToMany(targetEntity = Tag.class, fetch = FetchType.LAZY)
  @JoinTable(name = "ir_tag",
          joinColumns = @JoinColumn(name = "asset_id", referencedColumnName = "id"),
          inverseJoinColumns = @JoinColumn(name = "tag_id", referencedColumnName = "id"))
  private Set<Tag> tags;

  @Override
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

  public Set<ExecutionInfo> getExecutionInfoList() {
    return executionInfoList;
  }

  public IncidenceRateAnalysis setExecutionInfoList(Set<ExecutionInfo> executionInfoList) {
    this.executionInfoList = executionInfoList;
    return this;
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
