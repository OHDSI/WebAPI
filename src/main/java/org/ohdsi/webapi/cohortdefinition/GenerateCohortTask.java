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
package org.ohdsi.webapi.cohortdefinition;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.ohdsi.webapi.cohortdefinition.CohortExpressionQueryBuilder.BuildExpressionQueryOptions;

/**
 *
 * @author Chris Knoll <cknoll@ohdsi.org>
 */
public class GenerateCohortTask {

  //TODO: Define task-specific paramters
  private CohortDefinition cohortDefinition;
  private BuildExpressionQueryOptions options;
  private String sourceDialect;
  private String targetDialect;
    
  public CohortDefinition getCohortDefinition()
  {
    return this.cohortDefinition;
  }
  
  public GenerateCohortTask setCohortDefinition(CohortDefinition cohortDefinition)
  {
    this.cohortDefinition = cohortDefinition;
    return this;
  }
  
  public BuildExpressionQueryOptions getOptions()
  {
    return this.options;
  }
  
  public GenerateCohortTask setOptions(BuildExpressionQueryOptions options)
  {
    this.options = options;
    return this;
  }
  
  public String getSourceDialect() {
    return sourceDialect;
  }

  public GenerateCohortTask setSourceDialect(String sourceDialect) {
    this.sourceDialect = sourceDialect;
    return this;
    
  }

  public String getTargetDialect() {
    return targetDialect;
  }

  public GenerateCohortTask setTargetDialect(String targetDialect) {
    this.targetDialect = targetDialect;
    return this;
  }
  
  @Override
  public String toString() {

    try {
      ObjectMapper mapper = new ObjectMapper();
      return mapper.writeValueAsString(this);
    } catch (Exception e) {
      // ignored, let parent resolve...
    }

    return super.toString();

  }
}
