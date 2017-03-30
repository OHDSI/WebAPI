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
public class StudyRepositoryTests {

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
  public void createStudy() {
    // Create Cohorts

    StudyCohort i_cohort = new StudyCohort();
    i_cohort.setName("Study1: Cohort 4 (Indication)");
    i_cohort.setExpression("{\"someField\": \"someValue\"}");
    i_cohort = studyCohortRepository.save(i_cohort);

    StudyCohort t_cohort = new StudyCohort();
    t_cohort.setName("Study1: Cohort 1 (Target)");
    t_cohort.setExpression("{\"someField\": \"someValue\"}");
    t_cohort = studyCohortRepository.save(t_cohort);

    // Set up relationships
    CohortRelationship rel = new CohortRelationship();
    rel.setTarget(i_cohort);
    rel.setRelationshipType(RelationshipType.INDICATION);
    t_cohort.getCohortRelationships().add(rel);
    
    t_cohort = studyCohortRepository.save(t_cohort);
    
    StudyCohort o_cohort = new StudyCohort();
    o_cohort.setName("Study1: Cohort 2 (Outcome)");
    o_cohort.setExpression("{\"someField\": \"someValue\"}");
    o_cohort = studyCohortRepository.save(o_cohort);

    StudyCohort c_cohort = new StudyCohort();
    c_cohort.setName("Study1: Cohort 3 (Comparator)");
    c_cohort.setExpression("{\"someField\": \"someValue\"}");
    c_cohort = studyCohortRepository.save(c_cohort);

    // Create IR Analysis
    
    StudyIR ira = new StudyIR();
    ira.setParams("{\"param1\": \"someValue\"}");
    List<StudyCohort> targets = new ArrayList<>();
    targets.add(t_cohort);
    ira.setTargets(targets);
    
    List<StudyCohort> outcomes = new ArrayList<>();
    outcomes.add(o_cohort);
    ira.setOutcomes(outcomes);
    
    ira = studyIRRepository.save(ira);
    
    // Add CCA
    StudyCCA cca = new StudyCCA();
    StudyCCAPair ccaPair = new StudyCCAPair();
    ccaPair.setTarget(t_cohort);
    ccaPair.setComparator(c_cohort);
    ccaPair.setOutcome(o_cohort);
    ccaPair.setCca(cca); // add ref pair -> cca
    cca.getPairList().add(ccaPair); // add ref cca -> pair
    
    cca = studyCCARepository.save(cca);


    Study s = new Study();
    s.setName("Test Study");
    s.setDescription("Some Desc");

    // add cohorts
    s.getCohortList().add(i_cohort);
    s.getCohortList().add(t_cohort);
    s.getCohortList().add(c_cohort);
    s.getCohortList().add(o_cohort);
    
    // add IR
    s.getIrAnalysisList().add(ira);
    
    // add CCA
    s.getCcaList().add(cca); 

    s = studyRepository.save(s); 
  }

  @Test
  @Commit
  public void deleteAllStudies() {
    studyRepository.deleteAll();
    studyCCARepository.deleteAll();
    studyIRRepository.deleteAll();
    studyCohortRepository.deleteAll();
    
  }  
  
}
