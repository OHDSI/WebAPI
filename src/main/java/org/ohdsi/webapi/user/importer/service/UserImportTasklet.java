package org.ohdsi.webapi.user.importer.service;

import org.apache.commons.logging.Log;
import org.ohdsi.webapi.common.generation.TransactionalTasklet;
import org.ohdsi.webapi.user.importer.model.UserImportJob;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.transaction.support.TransactionTemplate;

public class UserImportTasklet extends TransactionalTasklet<UserImportJob> {

  public UserImportTasklet(Log log, TransactionTemplate transactionTemplate) {
    super(log, transactionTemplate);
  }

  @Override
  protected UserImportJob doTask(ChunkContext chunkContext) {

    return null;
  }
}
