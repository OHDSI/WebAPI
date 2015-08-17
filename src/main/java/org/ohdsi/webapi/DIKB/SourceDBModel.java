package org.ohdsi.webapi.DIKB;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SourceDBModel {

		  @JsonProperty("name")
		  public String sourceType;
		  
		  @JsonProperty("value")
		  public int sourceNum;
		  
}
