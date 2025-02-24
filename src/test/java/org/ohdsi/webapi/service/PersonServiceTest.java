package org.ohdsi.webapi.service;

import java.io.IOException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ohdsi.webapi.util.PreparedStatementRenderer;
import org.springframework.beans.factory.annotation.Autowired;

public class PersonServiceTest extends AbstractServiceTest {

  @Autowired
  private PersonService personService;

  @BeforeEach
  public void before() {

    if (personService == null) {
      personService = new PersonService();
    }
  }

  @Test
  public void prepareGetPersonProfile() throws IOException {

    String personId = "5555";
    PreparedStatementRenderer psr = personService.prepareGetPersonProfile(personId, getSource());
    String actualSql = psr.getSql();
    Assertions.assertNotNull(actualSql);
    Assertions.assertNotNull(psr.getSetter());

    assertSqlEquals("/person/sql/getRecords-expected.sql", psr);

    //// count the number of question marks in the sql, make sure it matches the orderedParamsList size
    int qmarkCount = 0;
    int startIndex = 0;
    while ((startIndex = actualSql.indexOf("?", startIndex)) != -1) {
      qmarkCount++;
      startIndex++;
    }
    Assertions.assertEquals(qmarkCount, psr.getOrderedParamsList().size());
    /// make sure that all of the parameters are equivalent to personId value
    for (Object param : psr.getOrderedParamsList()) {
      Assertions.assertEquals(param, 5555L);
    }
  }
}
