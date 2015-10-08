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
public class DrugPrevalence {
  public DrugPrevalence() {
  }
  
  public String conceptPath;
  public long conceptId;
  public long numPersons;
  public Float percentPersons;
  public Float lengthOfEra;
}
