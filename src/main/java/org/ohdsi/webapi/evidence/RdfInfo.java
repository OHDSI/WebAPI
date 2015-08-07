package org.ohdsi.webapi.evidence;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RdfInfo {
	
	@JsonProperty("SOURCE")
    public String sourceDocument;
	
	@JsonProperty("TESTINFO")
    public String testinfo;
}