/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ohdsi.webapi.cohortresults;

/**
 *
 * @author sgold1
 */
public class CohortBreakdown {
  public Long people; // count
  public String gender;
  public String age; // e.g. "30-39"
  public Long conditions; // count
  public Long drugs; // count
}
