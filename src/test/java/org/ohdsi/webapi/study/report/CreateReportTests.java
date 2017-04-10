package org.ohdsi.webapi.study.report;

import org.ohdsi.webapi.study.*;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import static org.junit.Assert.assertNotNull;
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
public class CreateReportTests {

  @Autowired
  private StudyRepository studyRepository;

  @Autowired
  private ReportRepository reportRepository;

  @PersistenceContext
  protected EntityManager entityManager;

  @Test
  @Commit
  public void createReport() {

    Report r = new Report();
    Study studyEntity = studyRepository.findAll().iterator().next();
    
    assertNotNull("Must be at least 1 study to associate to this report", studyEntity);
    
    r.setStudy(studyEntity);
    
    r.setName("Test Report of sample data");
    r.setDescription("This report is a test case to test the sample data inserted into the database.");
    
    // Associate Cohort Pairs
    ReportCohortPair p = new ReportCohortPair();
    StudyCohort t = entityManager.getReference(StudyCohort.class,1125315);
    StudyCohort o = entityManager.getReference(StudyCohort.class,1174888);
    p.setActive(true);
    p.setTarget(t);
    p.setOutcome(o);
    r.getCohortPairs().add(p);
    
    // Associate Covariates
    ReportCovariate rcov = null;
    
    rcov = new ReportCovariate();
    rcov.setCovariateId(Long.valueOf(22274101));
    rcov.setCovariateName("Condition occurrence record for the verbatim concept observed during 365d on or prior to cohort index:  22274-Neoplasm of uncertain behavior of larynx");
    rcov.setCovariateSection(CovariateSection.CONDITIONS);
    rcov.setOrdinal(0);
    r.getCovariates().add(rcov);
    
    rcov = new ReportCovariate();
    rcov.setCovariateId(Long.valueOf(22281101));
    rcov.setCovariateName("Condition occurrence record for the verbatim concept observed during 365d on or prior to cohort index:  22281-Hb SS disease");
    rcov.setCovariateSection(CovariateSection.CONDITIONS);
    rcov.setOrdinal(1);
    r.getCovariates().add(rcov);
    
    rcov = new ReportCovariate();
    rcov.setCovariateId(Long.valueOf(22288101));
    rcov.setCovariateName("Condition occurrence record for the verbatim concept observed during 365d on or prior to cohort index:  22288-Hereditary elliptocytosis");
    rcov.setCovariateSection(CovariateSection.CONDITIONS);
    rcov.setOrdinal(2);
    r.getCovariates().add(rcov);
    
    // Create Content
    ReportContent rcontent = null;
    
    rcontent = new ReportContent();
    rcontent.setContent("This is content for the background content");
    rcontent.setSection(ContentSection.BACKGROUND);
    r.getContent().add(rcontent);
    
    rcontent = new ReportContent();
    rcontent.setContent("This is content for the methods content");
    rcontent.setSection(ContentSection.METHODS);
    r.getContent().add(rcontent);

    rcontent = new ReportContent();
    rcontent.setContent("This is content for the results content");
    rcontent.setSection(ContentSection.RESULTS);
    r.getContent().add(rcontent);
    
    rcontent = new ReportContent();
    rcontent.setContent("This is content for the conclusion content");
    rcontent.setSection(ContentSection.CONCLUSION);
    r.getContent().add(rcontent);
    
    // Associate Source
    ReportSource rs = null;
    StudySource ss = null;
    
    rs = new ReportSource();
    rs.setActive(true);
    ss = entityManager.getReference(StudySource.class,2);
    rs.setSource(ss);
    r.getSources().add(rs);
    
    rs = new ReportSource();
    rs.setActive(false);
    ss = entityManager.getReference(StudySource.class,4);
    rs.setSource(ss);
    r.getSources().add(rs);
    
    rs = new ReportSource();
    rs.setActive(true);
    ss = entityManager.getReference(StudySource.class,6);
    rs.setSource(ss);
    r.getSources().add(rs);
    
    r = reportRepository.save(r);
  }
}
