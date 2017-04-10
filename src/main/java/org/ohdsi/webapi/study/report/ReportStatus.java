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
public enum ReportStatus {
  DRAFT (1, "Draft"),
  PUBLISHED (2, "Published"),
  DELETED (3, "Deleted");
  
  private final int id;
  private final String name;

  ReportStatus(int id, String name) {
    this.id = id;
    this.name = name;
  }
}
