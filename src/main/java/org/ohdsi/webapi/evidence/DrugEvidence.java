package org.ohdsi.webapi.evidence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

/**
 *
 * @author rkboyce and m_rasteger
 */
@JsonInclude(Include.NON_NULL)
public class DrugEvidence {
	
	  @JsonProperty("evidenceSource")
    public String evidenceSource;
    
    @JsonProperty("relationshipType")
    public String relationshipType;
    
    @JsonProperty("statisticType")
    public String statisticType;
		
    @JsonProperty("statisticValue")
    public BigDecimal statisticValue;

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
