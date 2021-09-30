/*
 * Copyright 2015 fdefalco.
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
package org.ohdsi.webapi.evidence;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

/**
 *
 * @author fdefalco
 */
public class EvidenceUniverse {
  @JsonProperty("evidence_id")
  public int evidence_id;
    
  @JsonProperty("condition_concept_id")
  public int condition_concept_id;

  @JsonProperty("condition_concept_name")
  public String condition_concept_name;

  @JsonProperty("ingredient_concept_id")
  public int  ingredient_concept_id;

  @JsonProperty("ingredient_concept_name")
  public String ingredient_concept_name;

  @JsonProperty("evidence_type")
  public String evidence_type;

  @JsonProperty("supports")
  public Character supports;
  
  @JsonProperty("statistic_value")
  public BigDecimal statistic_value;
  
  @JsonProperty("evidence_linkouts")
  public String evidence_linkouts;
  
  @JsonProperty("totalNumber")
  public int  totalNumber;
  
  @JsonProperty("hasEvidence")
  public String hasEvidence;
}
