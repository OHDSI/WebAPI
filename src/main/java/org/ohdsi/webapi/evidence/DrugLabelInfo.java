package org.ohdsi.webapi.evidence;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class DrugLabelInfo {
    @JsonProperty("conceptId")
    public String conceptId;
    
    @JsonProperty("conceptName")
    public String conceptName;

    @JsonProperty("usaProductLabelExists")
    public Integer usaProductLabelExists;
}
