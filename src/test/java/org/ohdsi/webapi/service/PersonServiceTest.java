package org.ohdsi.webapi.service;

import java.io.IOException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.ohdsi.webapi.util.PreparedStatementRenderer;
import org.springframework.beans.factory.annotation.Autowired;

public class PersonServiceTest extends AbstractServiceTest {

  @Autowired
  private PersonService personService;

  @Before
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
    Assert.assertNotNull(actualSql);
    Assert.assertNotNull(psr.getSetter());

    assertSqlEquals("/person/sql/getRecords-expected.sql", psr);

    //// count the number of question marks in the sql, make sure it matches the orderedParamsList size
    int qmarkCount = 0;
    int startIndex = 0;
    while ((startIndex = actualSql.indexOf("?", startIndex)) != -1) {
      qmarkCount++;
      startIndex++;
    }
    Assert.assertEquals(qmarkCount, psr.getOrderedParamsList().size());
    /// make sure that all of the parameters are equivalent to personId value
    for (Object param : psr.getOrderedParamsList()) {
      Assert.assertEquals(param, 5555L);
    }
  }
}
