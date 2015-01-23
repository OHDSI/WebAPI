/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ohdsi.webapi.cohortdefinition;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * @author cknoll1
 */
public class Window {

  public class Endpoint {
    @JsonProperty("Days")
    public Integer days;

    @JsonProperty("Coeff")
    public int coeff;
  }
  
  @JsonProperty("Start")
  public Endpoint start;  

  @JsonProperty("End")
  public Endpoint end;  
  
}
