package org.ohdsi.webapi.service;

import org.ohdsi.webapi.util.TempTableCleanupManager;
import org.springframework.batch.repeat.RepeatContext;
import org.springframework.batch.repeat.exception.ExceptionHandler;

public class GenerationTaskExceptionHandler implements ExceptionHandler {

  private TempTableCleanupManager cleanupManager;

  public GenerationTaskExceptionHandler(TempTableCleanupManager cleanupManager) {
    this.cleanupManager = cleanupManager;
  }

  @Override
  public void handleException(RepeatContext context, Throwable throwable) throws Throwable {
      cleanupManager.cleanupTempTables();
      throw throwable;
  }
}
