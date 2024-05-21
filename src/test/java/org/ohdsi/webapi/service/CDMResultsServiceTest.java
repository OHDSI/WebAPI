package org.ohdsi.webapi.service;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import org.junit.Before;
import org.junit.Test;
import org.ohdsi.webapi.util.PreparedStatementRenderer;
import org.springframework.beans.factory.annotation.Autowired;

public class CDMResultsServiceTest extends AbstractServiceTest {

  @Autowired
  private CDMResultsService cdmResultsService;

  @Before
  public void before() {

    if (cdmResultsService == null) cdmResultsService = new CDMResultsService();
  }
}
