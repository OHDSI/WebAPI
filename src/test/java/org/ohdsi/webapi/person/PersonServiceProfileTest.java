package org.ohdsi.webapi.person;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ohdsi.webapi.AbstractDatabaseTest;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceDaimon;
import org.ohdsi.webapi.source.SourceRepository;
import org.ohdsi.webapi.util.CancelableJdbcTemplate;

/**
 * Builds patient profiles from a small CDM on the embedded Postgres instance.
 */
public class PersonServiceProfileTest extends AbstractDatabaseTest {

  private static final String SCHEMA = "person_profile_test";
  private static final String SOURCE_KEY = "profile";
  private static final long COHORT_ID = 1;

  private static final String[] RECORD_TABLES = {
    "drug_exposure (person_id BIGINT, drug_concept_id INT, drug_exposure_start_date DATE, drug_exposure_end_date DATE)",
    "drug_era (person_id BIGINT, drug_concept_id INT, drug_era_start_date DATE, drug_era_end_date DATE)",
    "condition_occurrence (person_id BIGINT, condition_concept_id INT, condition_start_date DATE, condition_end_date DATE)",
    "condition_era (person_id BIGINT, condition_concept_id INT, condition_era_start_date DATE, condition_era_end_date DATE)",
    "observation (person_id BIGINT, observation_concept_id INT, observation_date DATE)",
    "visit_occurrence (person_id BIGINT, visit_concept_id INT, visit_start_date DATE, visit_end_date DATE)",
    "death (person_id BIGINT, death_type_concept_id INT, death_date DATE)",
    "measurement (person_id BIGINT, measurement_concept_id INT, measurement_date DATE)",
    "device_exposure (person_id BIGINT, device_concept_id INT, device_exposure_start_date DATE, device_exposure_end_date DATE)",
    "procedure_occurrence (person_id BIGINT, procedure_concept_id INT, procedure_date DATE)",
    "specimen (person_id BIGINT, specimen_concept_id INT, specimen_date DATE)"
  };

  private PersonService personService;

  @Before
  public void createCdm() {
    jdbcTemplate.execute("DROP SCHEMA IF EXISTS " + SCHEMA + " CASCADE; CREATE SCHEMA " + SCHEMA);
    jdbcTemplate.execute("CREATE TABLE " + SCHEMA + ".concept (concept_id INT, concept_name VARCHAR(255))");
    jdbcTemplate.execute("CREATE TABLE " + SCHEMA + ".person (person_id BIGINT, gender_concept_id INT, year_of_birth INT)");
    jdbcTemplate.execute("CREATE TABLE " + SCHEMA + ".observation_period (observation_period_id BIGINT, person_id BIGINT, "
            + "observation_period_start_date DATE, observation_period_end_date DATE, period_type_concept_id INT)");
    jdbcTemplate.execute("CREATE TABLE " + SCHEMA + ".cohort (cohort_definition_id BIGINT, subject_id BIGINT, "
            + "cohort_start_date DATE, cohort_end_date DATE)");
    for (String table : RECORD_TABLES) {
      jdbcTemplate.execute("CREATE TABLE " + SCHEMA + "." + table);
    }
    jdbcTemplate.update("INSERT INTO " + SCHEMA + ".concept VALUES (8532, 'FEMALE'), (32813, 'EHR'), "
            + "(201826, 'Type 2 diabetes mellitus')");

    // person 1 was enrolled for two years but has no clinical records (for example, no claims)
    jdbcTemplate.update("INSERT INTO " + SCHEMA + ".person VALUES (1, 8532, 1970)");
    jdbcTemplate.update("INSERT INTO " + SCHEMA + ".observation_period VALUES "
            + "(10, 1, DATE '2019-01-01', DATE '2019-12-31', 32813), (11, 1, DATE '2021-01-01', DATE '2021-06-30', 32813)");

    // person 2 has a record and belongs to cohort 1
    jdbcTemplate.update("INSERT INTO " + SCHEMA + ".person VALUES (2, 8532, 1980)");
    jdbcTemplate.update("INSERT INTO " + SCHEMA + ".observation_period VALUES "
            + "(20, 2, DATE '2019-01-01', DATE '2020-12-31', 32813)");
    jdbcTemplate.update("INSERT INTO " + SCHEMA + ".condition_occurrence VALUES (2, 201826, DATE '2020-03-01', NULL)");
    jdbcTemplate.update("INSERT INTO " + SCHEMA + ".cohort VALUES (1, 2, DATE '2020-03-01', DATE '2020-12-31')");

    Source source = new Source();
    source.setSourceKey(SOURCE_KEY);
    source.setSourceDialect("postgresql");
    source.setDaimons(Arrays.asList(daimon(source, SourceDaimon.DaimonType.CDM),
            daimon(source, SourceDaimon.DaimonType.Vocabulary), daimon(source, SourceDaimon.DaimonType.Results)));
    SourceRepository sourceRepository = mock(SourceRepository.class);
    when(sourceRepository.findBySourceKey(SOURCE_KEY)).thenReturn(source);

    personService = spy(new PersonService());
    doReturn(sourceRepository).when(personService).getSourceRepository();
    doReturn(new CancelableJdbcTemplate(getDataSource())).when(personService).getSourceJdbcTemplate(source);
  }

  @After
  public void dropCdm() {
    jdbcTemplate.execute("DROP SCHEMA IF EXISTS " + SCHEMA + " CASCADE");
  }

  private static SourceDaimon daimon(Source source, SourceDaimon.DaimonType type) {
    SourceDaimon daimon = new SourceDaimon(source);
    daimon.setDaimonType(type);
    daimon.setTableQualifier(SCHEMA);
    return daimon;
  }

  @Test
  public void personWithoutRecordsIsIndexedAtTheFirstObservationPeriod() {
    PersonProfile profile = personService.getPersonProfile(SOURCE_KEY, "1", 0L);

    assertThat(profile.records).isEmpty();
    assertThat(profile.ageAtIndex).isEqualTo(2019 - 1970);
    assertThat(profile.observationPeriods).extracting(p -> p.id).containsExactlyInAnyOrder(10L, 11L);
    ObservationPeriod first = profile.observationPeriods.stream().filter(p -> p.id == 10L).findFirst().get();
    assertThat(first.x1).isEqualTo(0);
    assertThat(first.x2).isEqualTo(364);
    ObservationPeriod second = profile.observationPeriods.stream().filter(p -> p.id == 11L).findFirst().get();
    assertThat(second.x1).isEqualTo(731);
    assertThat(second.x2).isEqualTo(911);
  }

  @Test
  public void personWithoutRecordsOutsideTheRequestedCohortIsIndexedAtTheFirstObservationPeriod() {
    PersonProfile profile = personService.getPersonProfile(SOURCE_KEY, "1", COHORT_ID);

    assertThat(profile.cohorts).isEmpty();
    assertThat(profile.ageAtIndex).isEqualTo(2019 - 1970);
    assertThat(profile.observationPeriods).extracting(p -> p.x1).containsExactlyInAnyOrder(0, 731);
  }

  @Test
  public void cohortMemberIsIndexedAtTheCohortStart() {
    PersonProfile profile = personService.getPersonProfile(SOURCE_KEY, "2", COHORT_ID);

    assertThat(profile.ageAtIndex).isEqualTo(2020 - 1980);
    assertThat(profile.records).hasSize(1);
    assertThat(profile.records.get(0).startDay).isEqualTo(0);
    assertThat(profile.records.get(0).endDay).isEqualTo(0);
    assertThat(profile.observationPeriods).hasSize(1);
    assertThat(profile.observationPeriods.get(0).x1).isEqualTo(-425);
    assertThat(profile.observationPeriods.get(0).x2).isEqualTo(305);
  }
}
