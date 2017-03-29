/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ohdsi.webapi.study;

/**
 *
 * @author Chris Knoll <cknoll@ohdsi.org>
 */
public enum RelationshipType {
  INDICATION (1, "Indication");
  
  private final int id;
  private final String name;

  RelationshipType(int id, String name) {
    this.id = id;
    this.name = name;
  }
}
