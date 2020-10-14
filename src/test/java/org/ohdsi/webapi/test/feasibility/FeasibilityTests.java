/*
 * Copyright 2015 Observational Health Data Sciences and Informatics [OHDSI.org].
 *
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
package org.ohdsi.webapi.test.feasibility;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.junit.Test;
import org.ohdsi.webapi.AbstractDatabaseTest;
import org.ohdsi.webapi.WebApi;
import org.ohdsi.webapi.feasibility.InclusionRule;
import org.ohdsi.webapi.feasibility.FeasibilityStudy;
import org.ohdsi.webapi.feasibility.FeasibilityStudyRepository;
import org.ohdsi.webapi.cohortdefinition.CohortDefinition;
import org.ohdsi.webapi.cohortdefinition.CohortDefinitionDetails;
import org.ohdsi.webapi.cohortdefinition.CohortDefinitionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author Chris Knoll <cknoll@ohdsi.org>
 */
@SpringBootTest(classes = WebApi.class)
@Rollback
public class FeasibilityTests extends AbstractDatabaseTest {
  
  @Autowired
  private CohortDefinitionRepository cohortDefinitionRepository;
  
  @Autowired
  private FeasibilityStudyRepository studyRepository;
  
  @PersistenceContext
  protected EntityManager entityManager;
  
  public int doCreate()
  {
    // create a new study, using CohortDefinition:1 as the index rule, and associate 3 inclusion rules.
    FeasibilityStudy newStudy = new FeasibilityStudy();
    
    CohortDefinition def = new CohortDefinition();

    CohortDefinitionDetails defDetails = new CohortDefinitionDetails();
    
    def.setName("Index Rule for Test Study");
    this.cohortDefinitionRepository.save(def);
    
    // assign details
    defDetails.setCohortDefinition(def);
    def.setDetails(defDetails);
    defDetails.setExpression("{\"DefinitionExpression\": \"{ some epression }\"}");
    
    this.cohortDefinitionRepository.save(def);
    
    newStudy.setName("Test Study")
      .setIndexRule(def);
    
    ArrayList<InclusionRule> inclusionRules = new ArrayList<>();
    inclusionRules.add(new InclusionRule()
      .setName("Inclusion Rule 1")
      .setDescription("Description for Inclusion Rule 1")
      .setExpression("{\"CriteriaExpression\": \"{Rule 1}\"}"));

    inclusionRules.add(new InclusionRule()
      .setName("Inclusion Rule 2")
      .setDescription("Description for Inclusion Rule 2")
      .setExpression("{\"CriteriaExpression\": \"{Rule 2}\"}"));

    inclusionRules.add(new InclusionRule()
      .setName("Inclusion Rule 3")
      .setDescription("Description for Inclusion Rule 3")
      .setExpression("{\"CriteriaExpression\": \"{Rule 3}\"}"));

    newStudy.setInclusionRules(inclusionRules);
    
    studyRepository.save(newStudy);
    return newStudy.getId();
  }
  
  public void doUpdate(int id)
  {
    FeasibilityStudy updatedStudy = studyRepository.findOneWithDetail(id);
    CohortDefinition updatedDef = updatedStudy.getIndexRule();
    updatedDef.setName("Updated By StudyTest");
    
    // try removing the first inclusion rule
    List<InclusionRule> updatedRules = updatedStudy.getInclusionRules();
    updatedRules.add(new InclusionRule()
      .setName("Inclusion Rule 4")
      .setDescription("Description for Inclusion Rule 4")
      .setExpression("{\"CriteriaExpression\": \"{Rule 4}\"}"));
    
    updatedStudy.setInclusionRules(updatedRules);
    this.studyRepository.save(updatedStudy);
  }
  
  @Test
  @Transactional(transactionManager="transactionManager")
  public void testStudyCRUD() {
    int newStudyId = doCreate();
    doUpdate(newStudyId);
    
  }
  
}
