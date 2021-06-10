/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ohdsi.webapi.vocabulary.test;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Assert;
import org.ohdsi.circe.vocabulary.Concept;
import org.ohdsi.circe.vocabulary.ConceptSetExpression;
import org.ohdsi.circe.vocabulary.ConceptSetExpressionQueryBuilder;

/**
 *
 * @author cknoll1
 */
public class ConceptSetExpressionTests {

  private static ConceptSetExpression getTestExpression() {
    ConceptSetExpression exp = new ConceptSetExpression();
    exp.items = new ConceptSetExpression.ConceptSetItem[8];

    exp.items[0] = new ConceptSetExpression.ConceptSetItem();
    exp.items[0].concept = new Concept();
    exp.items[0].concept.conceptId = 1L;
    exp.items[0].concept.conceptName = "First Concept";
    exp.items[0].isExcluded = false;
    exp.items[0].includeDescendants = false;
    exp.items[0].includeMapped = false;

    exp.items[1] = new ConceptSetExpression.ConceptSetItem();
    exp.items[1].concept = new Concept();
    exp.items[1].concept.conceptId = 2L;
    exp.items[1].concept.conceptName = "Second Concept";
    exp.items[1].isExcluded = false;
    exp.items[1].includeDescendants = true;
    exp.items[1].includeMapped = false;
    
    exp.items[2] = new ConceptSetExpression.ConceptSetItem();
    exp.items[2].concept = new Concept();
    exp.items[2].concept.conceptId = 3L;
    exp.items[2].concept.conceptName = "Third Concept";
    exp.items[2].isExcluded = false;
    exp.items[2].includeDescendants = true;
    exp.items[2].includeMapped = true;

    exp.items[3] = new ConceptSetExpression.ConceptSetItem();
    exp.items[3].concept = new Concept();
    exp.items[3].concept.conceptId = 4L;
    exp.items[3].concept.conceptName = "Forth Concept (Excluded)";
    exp.items[3].isExcluded = true;
    exp.items[3].includeDescendants = false;
    exp.items[3].includeMapped = false;

    exp.items[4] = new ConceptSetExpression.ConceptSetItem();
    exp.items[4].concept = new Concept();
    exp.items[4].concept.conceptId = 5L;
    exp.items[4].concept.conceptName = "Fith Concept (Excluded)";
    exp.items[4].isExcluded = true;
    exp.items[4].includeDescendants = true;
    exp.items[4].includeMapped = false;

    exp.items[5] = new ConceptSetExpression.ConceptSetItem();
    exp.items[5].concept = new Concept();
    exp.items[5].concept.conceptId = 6L;
    exp.items[5].concept.conceptName = "Sixth Concept (Excluded)";
    exp.items[5].isExcluded = true;
    exp.items[5].includeDescendants = false;
    exp.items[5].includeMapped = true;

    exp.items[6] = new ConceptSetExpression.ConceptSetItem();
    exp.items[6].concept = new Concept();
    exp.items[6].concept.conceptId = 7L;
    exp.items[6].concept.conceptName = "Seventh Concept (Excluded)";
    exp.items[6].isExcluded = true;
    exp.items[6].includeDescendants = true;
    exp.items[6].includeMapped = true;
    
    exp.items[7] = new ConceptSetExpression.ConceptSetItem();
    exp.items[7].concept = new Concept();
    exp.items[7].concept.conceptId = 8L;
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
