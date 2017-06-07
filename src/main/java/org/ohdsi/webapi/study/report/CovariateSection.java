/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ohdsi.webapi.study.report;

/**
 *
 * @author Chris Knoll <cknoll@ohdsi.org>
 */
public enum CovariateSection {
  DEMOGRAPHICS(1, "Indication"),
  CONDITIONS(2, "Condition"),
  DRUGS(3, "Drugs"),
  PROCEDURES(4, "Procedures"),
	MEASUREMENTS(5, "Measurements"),
	OBSERVATIONS(6, "Observations"),
	DISTRIBUTIONS(7, "Distributions");
  
  private final int id;
  private final String name;

  CovariateSection(int id, String name) {
    this.id = id;
    this.name = name;
  }
}
