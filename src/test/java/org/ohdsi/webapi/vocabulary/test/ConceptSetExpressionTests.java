/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ohdsi.webapi.vocabulary.test;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Assert;
import org.ohdsi.webapi.vocabulary.ConceptSetExpression;
import org.ohdsi.webapi.vocabulary.ConceptSetExpressionQueryBuilder;

/**
 *
 * @author cknoll1
 */
public class ConceptSetExpressionTests {

  private static ConceptSetExpression getTestExpression() {
    ConceptSetExpression exp = new ConceptSetExpression();
    exp.items = new ConceptSetExpression.ConceptSetItem[8];

    exp.items[0] = new ConceptSetExpression.ConceptSetItem();
    exp.items[0].concept = new ConceptSetExpression.Concept();
    exp.items[0].concept.conceptId = 1;
    exp.items[0].concept.conceptName = "First Concept";
    exp.items[0].isExcluded = false;
    exp.items[0].includeDescendants = false;
    exp.items[0].includeMapped = false;

    exp.items[1] = new ConceptSetExpression.ConceptSetItem();
    exp.items[1].concept = new ConceptSetExpression.Concept();
    exp.items[1].concept.conceptId = 2;
    exp.items[1].concept.conceptName = "Second Concept";
    exp.items[1].isExcluded = false;
    exp.items[1].includeDescendants = true;
    exp.items[1].includeMapped = false;
    
    exp.items[2] = new ConceptSetExpression.ConceptSetItem();
    exp.items[2].concept = new ConceptSetExpression.Concept();
    exp.items[2].concept.conceptId = 3;
    exp.items[2].concept.conceptName = "Third Concept";
    exp.items[2].isExcluded = false;
    exp.items[2].includeDescendants = true;
    exp.items[2].includeMapped = true;

    exp.items[3] = new ConceptSetExpression.ConceptSetItem();
    exp.items[3].concept = new ConceptSetExpression.Concept();
    exp.items[3].concept.conceptId = 4;
    exp.items[3].concept.conceptName = "Forth Concept (Excluded)";
    exp.items[3].isExcluded = true;
    exp.items[3].includeDescendants = false;
    exp.items[3].includeMapped = false;

    exp.items[4] = new ConceptSetExpression.ConceptSetItem();
    exp.items[4].concept = new ConceptSetExpression.Concept();
    exp.items[4].concept.conceptId = 5;
    exp.items[4].concept.conceptName = "Fith Concept (Excluded)";
    exp.items[4].isExcluded = true;
    exp.items[4].includeDescendants = true;
    exp.items[4].includeMapped = false;

    exp.items[5] = new ConceptSetExpression.ConceptSetItem();
    exp.items[5].concept = new ConceptSetExpression.Concept();
    exp.items[5].concept.conceptId = 6;
    exp.items[5].concept.conceptName = "Sixth Concept (Excluded)";
    exp.items[5].isExcluded = true;
    exp.items[5].includeDescendants = false;
    exp.items[5].includeMapped = true;

    exp.items[6] = new ConceptSetExpression.ConceptSetItem();
    exp.items[6].concept = new ConceptSetExpression.Concept();
    exp.items[6].concept.conceptId = 7;
    exp.items[6].concept.conceptName = "Seventh Concept (Excluded)";
    exp.items[6].isExcluded = true;
    exp.items[6].includeDescendants = true;
    exp.items[6].includeMapped = true;
    
    exp.items[7] = new ConceptSetExpression.ConceptSetItem();
    exp.items[7].concept = new ConceptSetExpression.Concept();
    exp.items[7].concept.conceptId = 8;
    exp.items[7].concept.conceptName = "Eigth Concept";
    exp.items[7].isExcluded = false;
    exp.items[7].includeDescendants = false;
    exp.items[7].includeMapped = true;
    
    return exp;
  }

  @BeforeClass
  public static void setUpClass() throws Exception {
  }

  @AfterClass
  public static void tearDownClass() throws Exception {
  }

  @Test
  public void SimpleConceptSetExpressionBuild() {
    ConceptSetExpressionQueryBuilder builder = new ConceptSetExpressionQueryBuilder();
    ConceptSetExpression testExpression = getTestExpression();

    String conceptSetExpressionSql = builder.buildExpressionQuery(testExpression);
    
    // included concepts should have (1,2,3,8)
    Assert.assertTrue(conceptSetExpressionSql.indexOf("(1,2,3,8)") > 0);
    
    // included descendants should have (2,3)
    Assert.assertTrue(conceptSetExpressionSql.indexOf("(2,3)") > 0);
    
    // excluded concepts should have (4,5,6,7)
    Assert.assertTrue(conceptSetExpressionSql.indexOf("(4,5,6,7)") > 0);
    
    // excluded descendants should have (5,7)
    Assert.assertTrue(conceptSetExpressionSql.indexOf("(5,7)") > 0);
    
    // mapped concepts should have (3,8)
    Assert.assertTrue(conceptSetExpressionSql.indexOf("(3,8)") > 0);
    
    // mapped descendants should have 3
    Assert.assertTrue(conceptSetExpressionSql.indexOf("(3)") > 0);
    
    // mapped excludes should have (6,7)
    Assert.assertTrue(conceptSetExpressionSql.indexOf("(6,7)") > 0);
    
    // mapped exclude descendants should have (7)
    Assert.assertTrue(conceptSetExpressionSql.indexOf("(7)") > 0);
    
  }

}
