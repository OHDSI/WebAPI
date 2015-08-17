package org.ohdsi.webapi.DIKB;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonProperty;

public class InfoDBModel {

		  @JsonProperty("predicate")
		  public String predicate;
		  
		  @JsonProperty("precipitant")
		  public ArrayList<String> precipitant;
}
