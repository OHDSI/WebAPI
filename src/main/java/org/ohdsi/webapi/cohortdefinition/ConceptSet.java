/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ohdsi.webapi.cohortdefinition;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.ohdsi.webapi.vocabulary.ConceptSetExpression;

/**
 *
 * @author cknoll1
 */
public class ConceptSet {
  
  public int id;
  public String name;
  public ConceptSetExpression expression;
}
