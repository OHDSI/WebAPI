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

import java.io.IOException;
import java.io.Serializable;
import java.io.UncheckedIOException;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.MapsId;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
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
  @OneToOne
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

    try {
      return (getExpression() != null) ?
              new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).readValue(getExpression(), CohortExpression.class) : null;
    } catch (IOException e) {
      return null;
    }
  }

  public String getExpression() {
    return expression;
  }

  public String getStandardizedExpression() {

    try {
      return Utils.serialize(getExpressionObject());
    } catch (JsonProcessingException ex) {
      throw new UncheckedIOException(ex);
    }
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
