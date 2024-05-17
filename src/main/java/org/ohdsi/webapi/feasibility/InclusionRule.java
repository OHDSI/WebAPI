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

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

/**
 *
 * @author Chris Knoll <cknoll@ohdsi.org>
 */
@Embeddable
public class InclusionRule {
  
  @Column(name="name")  
  private String name;

  @Column(name="description")
  private String description;
  
  @Column(name="expression")  
  @Lob  
  private String expression;
  public String getName() {
    return name;
  }

  public InclusionRule setName(String name) {
    this.name = name;
    return this;
  }

  public String getDescription() {
    return description;
  }

  public InclusionRule setDescription(String description) {
    this.description = description;
    return this;
  }

  public String getExpression() {
    return expression;
  }

  public InclusionRule setExpression(String expression) {
    this.expression = expression;
    return this;
  }
}
