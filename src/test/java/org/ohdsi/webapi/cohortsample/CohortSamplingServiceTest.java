package org.ohdsi.webapi.cohortsample;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ohdsi.webapi.AbstractDatabaseTest;
import org.ohdsi.webapi.cohortsample.dto.SampleParametersDTO;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceDaimon;
import org.ohdsi.webapi.util.PreparedStatementRenderer;
import org.springframework.beans.factory.annotation.Autowired;

public class CohortSamplingServiceTest extends AbstractDatabaseTest {

  private static final String SCHEMA = "cohort_sample_test";
  private static final int COHORT_ID = 1;
  private static final int OTHER_COHORT_ID = 2;

  @Autowired
  private CohortSamplingService cohortSamplingService;

  @Before
  public void createCdmAndResults() {
    jdbcTemplate.execute("CREATE SCHEMA IF NOT EXISTS " + SCHEMA);
    jdbcTemplate.execute("CREATE TABLE " + SCHEMA + ".person (person_id BIGINT, gender_concept_id INT, year_of_birth INT)");
    jdbcTemplate.execute("CREATE TABLE " + SCHEMA + ".cohort (cohort_definition_id INT, subject_id BIGINT, cohort_start_date DATE, cohort_end_date DATE)");
    // person_id, gender, year of birth, cohort: every cohort entry starts in 2020, so age = 2020 - year_of_birth
    Object[][] people = {
      {1, 8507, 2010, COHORT_ID},       // 10, male
      {2, 8532, 1970, COHORT_ID},       // 50, female
      {3, 8532, 1940, COHORT_ID},       // 80, female
      {4, 8507, 1935, COHORT_ID},       // 85, male
      {5, 8532, 2015, OTHER_COHORT_ID}, // 5, female, other cohort
      {6, 8532, 1930, OTHER_COHORT_ID}  // 90, female, other cohort
    };
    for (Object[] p : people) {
      jdbcTemplate.update("INSERT INTO " + SCHEMA + ".person VALUES (?, ?, ?)", p[0], p[1], p[2]);
      jdbcTemplate.update("INSERT INTO " + SCHEMA + ".cohort VALUES (?, ?, DATE '2020-06-01', DATE '2020-12-31')", p[3], p[0]);
    }
  }

  @After
  public void dropCdmAndResults() {
    jdbcTemplate.execute("DROP SCHEMA IF EXISTS " + SCHEMA + " CASCADE");
  }

  private static Source source() {
    Source source = new Source();
    source.setSourceDialect("postgresql");
    SourceDaimon cdm = new SourceDaimon(source);
    cdm.setDaimonType(SourceDaimon.DaimonType.CDM);
    cdm.setTableQualifier(SCHEMA);
    SourceDaimon results = new SourceDaimon(source);
    results.setDaimonType(SourceDaimon.DaimonType.Results);
    results.setTableQualifier(SCHEMA);
    source.setDaimons(Arrays.asList(cdm, results));
    return source;
  }

  private static CohortSample notBetween(int min, int max) {
    CohortSample sample = new CohortSample();
    sample.setCohortDefinitionId(COHORT_ID);
    sample.setSize(100);
    sample.setAgeMode(SampleParametersDTO.AgeMode.NOT_BETWEEN.getSerialName());
    sample.setAgeMin(min);
    sample.setAgeMax(max);
    return sample;
  }

  private List<Long> sampledPeople(SampleParametersDTO parameters, CohortSample sample) {
    PreparedStatementRenderer renderer = cohortSamplingService.prepareSampleElementsSql(parameters, sample, source());
    // the sample is drawn in random order, so compare the sampled people sorted
    return jdbcTemplate.query(renderer.getSql(), (rs, rowNum) -> rs.getLong("person_id"), renderer.getOrderedParams())
            .stream().sorted().collect(Collectors.toList());
  }

  @Test
  public void ageNotBetweenOnlySamplesTheRequestedCohort() {
    List<Long> people = sampledPeople(new SampleParametersDTO(), notBetween(20, 60));

    assertThat(people).containsExactly(1L, 3L, 4L);
  }

  @Test
  public void ageNotBetweenKeepsTheGenderFilter() {
    SampleParametersDTO parameters = new SampleParametersDTO();
    SampleParametersDTO.GenderDTO gender = new SampleParametersDTO.GenderDTO();
    gender.setConceptIds(Arrays.asList(SampleParametersDTO.GenderDTO.GENDER_FEMALE_CONCEPT_ID));
    parameters.setGender(gender);

    List<Long> people = sampledPeople(parameters, notBetween(20, 60));

    assertThat(people).containsExactly(3L);
  }
}
