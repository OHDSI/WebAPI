/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ohdsi.webapi.vocabulary;

/**
 *
 * A Class that encapsulates the elements of a Concept Set Expression.
 */
public class ConceptSetExpression {
  public static class ConceptSetItem
  {
    public Concept concept;
    public boolean isExcluded;
    public boolean includeDescendants;
    public boolean includeMapped;
  }
  
  public static class Concept {
    public long conceptId;
    public String conceptName;
    public String conceptCode;
    public String domainId;
    public String vocabularyId;
  }

  public ConceptSetItem[] items;
  
}
