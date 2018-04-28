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
public class Evidence {
	  @JsonProperty("evidenceSource")
    public String evidenceSource;
    
    @JsonProperty("relationshipType")
    public String relationshipType;
    
    @JsonProperty("statisticType")
    public String statisticType;
		
    @JsonProperty("statisticValue")
    public BigDecimal statisticValue;

    @JsonProperty("drugConceptId")
    public String drugConceptId;	
    
    @JsonProperty("drugConceptName")
    public String drugConceptName;	
    
    @JsonProperty("hoiConceptId")
    public String hoiConceptId;	
    
    @JsonProperty("hoiConceptName")
    public String hoiConceptName;	
		
    @JsonProperty("uniqueIdentifier")
    public String uniqueIdentifier;
		
    @JsonProperty("uniqueIdentifierType")
    public String uniqueIdentifierType;
		
    @JsonProperty("count")
    public Integer count;	

}
