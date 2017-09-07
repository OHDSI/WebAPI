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

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import org.ohdsi.circe.cohortdefinition.ConceptSet;

/**
 *
 * @author Chris Knoll <cknoll@ohdsi.org>
 */
public class IncidenceRateAnalysisExpression {
  
  @JsonProperty("ConceptSets")
  public ConceptSet[] conceptSets = new ConceptSet[0];

  @JsonProperty("targetIds")
  public List<Integer> targetIds = new ArrayList<>();

  @JsonProperty("outcomeIds")
  public List<Integer> outcomeIds = new ArrayList<>();
    
  @JsonProperty("timeAtRisk")
  public TimeAtRisk timeAtRisk;

  @JsonProperty("studyWindow")
  public DateRange studyWindow;
  
  @JsonProperty("strata")
  public List<StratifyRule> strata = new ArrayList<>();
  
}
