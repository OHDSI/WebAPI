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
import javax.persistence.*;

import com.fasterxml.jackson.core.type.TypeReference;
import org.hibernate.annotations.Type;
import org.ohdsi.analysis.Utils;
import org.ohdsi.circe.cohortdefinition.CohortExpression;

/**
 *
 * Stores the LOB/CLOB portion of the cohort definition expression.
 */
@Entity(name = "CohortDefinitionDetails")
@Table(name="cohort_definition_details")
public class CohortDefinitionDetails implements Serializable {

  private static final long serialVersionUID = 1L;

  @Id
  private Integer id;

  @MapsId
  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name="id")
  private CohortDefinition definition;

  @Lob
  @Type(type = "org.hibernate.type.TextType")
  private String expression;

  @Column(name = "hash_code")
  private Integer hashCode;

  @PrePersist
  @PreUpdate
  public void updateHashCode() {
    this.setHashCode(calculateHashCode());
  }

  public Integer calculateHashCode() {

    return getStandardizedExpression().hashCode();
  }

  public CohortExpression getExpressionObject() {

    return (getExpression() != null) ? Utils.deserialize(getExpression(), new TypeReference<CohortExpression>() {}) : null;
  }

  public String getExpression() {
    return expression;
  }

  public String getStandardizedExpression() {
    return Utils.serialize(getExpressionObject());
  }

  public CohortDefinitionDetails setExpression(String expression) {
    this.expression = expression;
    return this;
  }

  public CohortDefinition getCohortDefinition() {
    return this.definition;
  }

  public CohortDefinitionDetails setCohortDefinition(CohortDefinition definition) {
    this.definition = definition;
    return this;
  }

  public Integer getHashCode() {
    return hashCode;
  }

  private void setHashCode(final Integer hashCode) {
    this.hashCode = hashCode;
  }
}
