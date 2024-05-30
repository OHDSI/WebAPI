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

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.junit.Ignore;
import org.junit.Test;
import org.ohdsi.webapi.AbstractDatabaseTest;
import org.ohdsi.webapi.feasibility.FeasibilityStudy;
import org.ohdsi.webapi.feasibility.FeasibilityStudyRepository;
import org.ohdsi.webapi.feasibility.StudyGenerationInfo;
import org.ohdsi.webapi.GenerationStatus;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author Chris Knoll <cknoll@ohdsi.org>
 */
@SpringBootTest
@Rollback
public class StudyInfoTest extends AbstractDatabaseTest {

  @Autowired
  private FeasibilityStudyRepository studyRepository;
  
  @Autowired
  private SourceRepository sourceRepository;
  
 
  @PersistenceContext
  protected EntityManager entityManager;

  @Ignore
  @Test
  @Transactional(transactionManager="transactionManager")
  public void testStudyCRUD() {
    
    Source source = new Source();
    source.setSourceId(1);
    FeasibilityStudy newStudy = new FeasibilityStudy();
    newStudy.setName("Test Info Study");
    newStudy = this.studyRepository.save(newStudy);
    StudyGenerationInfo info = new StudyGenerationInfo(newStudy, source); // for testing, assume a sourceId of 1 exists.
    info.setStatus(GenerationStatus.PENDING);
    newStudy.getStudyGenerationInfoList().add(info);
    this.studyRepository.save(newStudy);
    newStudy.getStudyGenerationInfoList().clear();
    this.studyRepository.save(newStudy);
  }
}
