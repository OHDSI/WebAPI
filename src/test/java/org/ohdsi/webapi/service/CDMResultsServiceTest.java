package org.ohdsi.webapi.service;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.ohdsi.webapi.util.PreparedStatementRenderer;
import org.springframework.beans.factory.annotation.Autowired;

public class CDMResultsServiceTest extends AbstractServiceTest {

  @Autowired
  private CDMResultsService cdmResultsService;

  @BeforeEach
  public void before() {

    if (cdmResultsService == null) cdmResultsService = new CDMResultsService();
  }
}
