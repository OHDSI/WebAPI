/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ohdsi.webapi.report;

/**
 *
 * @author anthonygsena
 */
public class DrugEraPrevalence {
  public DrugEraPrevalence() {
  }
  
  public long conceptId;
  public String trellisName;
  public String seriesName;
  public long xCalendarYear;
  public Float yPrevalence1000Pp;    
}
