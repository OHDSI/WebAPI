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

import java.util.Date;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ohdsi.webapi.WebApi;
import org.ohdsi.webapi.feasibility.FeasibilityStudy;
import org.ohdsi.webapi.feasibility.FeasibilityStudyRepository;
import org.ohdsi.webapi.feasibility.StudyInfo;
import org.ohdsi.webapi.cohortdefinition.CohortDefinitionRepository;
import org.ohdsi.webapi.cohortdefinition.GenerationStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

/**
 *
 * @author Chris Knoll <cknoll@ohdsi.org>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = WebApi.class)
@TransactionConfiguration(defaultRollback = false, transactionManager = "transactionManager")
public class StudyInfoTest {

  @Autowired
  private CohortDefinitionRepository cohortDefinitionRepository;

  @Autowired
  private FeasibilityStudyRepository studyRepository;

  @Autowired
  private TransactionTemplate transactionTemplate;  
  
  @PersistenceContext
  protected EntityManager entityManager;

  @Test
  @Transactional
  public void testStudyCRUD() {
    
    DefaultTransactionDefinition requiresNewTx = new DefaultTransactionDefinition();
    requiresNewTx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    
    TransactionStatus saveTx = this.transactionTemplate.getTransactionManager().getTransaction(requiresNewTx);
    FeasibilityStudy newStudy = new FeasibilityStudy();
    newStudy.setName("Test Info Study");
    newStudy = this.studyRepository.save(newStudy);
    StudyInfo info = new StudyInfo(newStudy);
    info.setStatus(GenerationStatus.PENDING);
    newStudy.setInfo(info);
    this.studyRepository.save(newStudy);
    this.transactionTemplate.getTransactionManager().commit(saveTx);
    
    TransactionStatus updateTx = this.transactionTemplate.getTransactionManager().getTransaction(requiresNewTx);
    newStudy.setInfo(null);
    this.studyRepository.save(newStudy);
    this.transactionTemplate.getTransactionManager().commit(updateTx);
    
  }
}
