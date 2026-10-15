package org.ohdsi.webapi.person;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceDaimon;

public class PersonServiceSqlTest {

  private final PersonService service = new PersonService();

  private static Source source(String cdm, String vocabulary) {

    Source source = new Source();
    source.setSourceDialect("postgresql");
    List<SourceDaimon> daimons = new ArrayList<>();
    daimons.add(daimon(source, SourceDaimon.DaimonType.CDM, cdm));
    if (vocabulary != null) {
      daimons.add(daimon(source, SourceDaimon.DaimonType.Vocabulary, vocabulary));
    }
    source.setDaimons(daimons);
    return source;
  }

  private static SourceDaimon daimon(Source source, SourceDaimon.DaimonType type, String qualifier) {

    SourceDaimon daimon = new SourceDaimon(source);
    daimon.setDaimonType(type);
    daimon.setTableQualifier(qualifier);
    return daimon;
  }

  private void assertConceptFromVocabulary(String sql) {

    assertTrue(sql, sql.contains("vocab_schema.concept"));
    assertFalse(sql, sql.contains("cdm_schema.concept "));
    assertFalse(sql, sql.contains("@vocabularyTableQualifier"));
  }

  @Test
  public void personQueriesReadConceptsFromTheVocabularySchema() {

    Source source = source("cdm_schema", "vocab_schema");

    String personInfo = service.preparePersonInfoSql("1", source).getSql();
    assertConceptFromVocabulary(personInfo);
    assertTrue(personInfo, personInfo.contains("cdm_schema.person"));

    String periods = service.prepareObservationPeriodsSql("1", source).getSql();
    assertConceptFromVocabulary(periods);
    assertTrue(periods, periods.contains("cdm_schema.observation_period"));

    String records = service.prepareGetPersonProfile("1", source).getSql();
    assertConceptFromVocabulary(records);
    assertTrue(records, records.contains("cdm_schema.drug_exposure"));
  }

  @Test
  public void personQueriesFallBackToTheCdmSchemaWithoutAVocabularyDaimon() {

    Source source = source("cdm_schema", null);

    String records = service.prepareGetPersonProfile("1", source).getSql();
    assertTrue(records, records.contains("cdm_schema.concept"));
    assertFalse(records, records.contains("@vocabularyTableQualifier"));
  }
}
