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
public class HoiEvidence {
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
    
    @JsonProperty("uniqueIdentifier")
    public String uniqueIdentifier;
		
    @JsonProperty("uniqueIdentifierType")
    public String uniqueIdentifierType;
		
    @JsonProperty("count")
    public Integer count;	

}
