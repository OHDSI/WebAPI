package org.ohdsi.webapi.study;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ohdsi.webapi.WebApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = WebApi.class)
@Transactional
//@Ignore
public class DeleteStudyTest {

  @Autowired
  private StudyRepository studyRepository;

  @Autowired
  private StudyCohortRepository studyCohortRepository;

  @Autowired
  private StudyIRRepository studyIRRepository;

  @Autowired
  private StudyCCARepository studyCCARepository;
  
  @PersistenceContext
  protected EntityManager entityManager;
  
  @Test
  @Commit
  public void deleteAllStudies() {
    studyRepository.deleteAll();
    studyCCARepository.deleteAll();
    studyIRRepository.deleteAll();
    studyCohortRepository.deleteAll();
    
  }  
  
}
