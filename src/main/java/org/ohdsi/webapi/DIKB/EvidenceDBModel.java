package org.ohdsi.webapi.DIKB;

import com.fasterxml.jackson.annotation.JsonProperty;


public class EvidenceDBModel {
  @JsonProperty("researchStatementLabel")
  public String researchStatementLabel;

  @JsonProperty("assertType")
  public String assertType;

  @JsonProperty("dateAnnotated")
  public String dateAnnotated;

  @JsonProperty("evidenceRole")
  public String evidenceRole;

  @JsonProperty("evidence")
  public String evidence;
  
  @JsonProperty("source")
  public String source;

}
