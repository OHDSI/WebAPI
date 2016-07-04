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
	
	@JsonProperty("EVIDENCE")
    public String evidence;
    
    @JsonProperty("SUPPORTS")
    public Character supports;
    
    @JsonProperty("LINKOUT")
    public String linkout;

    @JsonProperty("STATISTIC_TYPE")
    public String statisticType;

    @JsonProperty("HOI")
    public String hoi;	
    
    @JsonProperty("HOI-NAME")
    public String hoiName;	
    
    @JsonProperty("COUNT")
    public Integer count;	

    @JsonProperty("VALUE")
    public BigDecimal value;
}
