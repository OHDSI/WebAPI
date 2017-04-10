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
public enum ContentSection {
  BACKGROUND (1, "Background"),
  METHODS (2, "Methods"),
  RESULTS (3, "Results"),
  CONCLUSION (4, "Conclusion");
  
  private final int id;
  private final String name;

  ContentSection(int id, String name) {
    this.id = id;
    this.name = name;
  }
}
