package org.ohdsi.webapi.user.importer.service;

import org.ohdsi.webapi.Constants;
import org.ohdsi.webapi.common.generation.TransactionalTasklet;
import org.ohdsi.webapi.user.importer.model.UserImportJob;
import org.slf4j.Logger;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Map;

public abstract class BaseUserImportTasklet<T> extends TransactionalTasklet<T> {

  protected final UserImportService userImportService;

  public BaseUserImportTasklet(Logger log, TransactionTemplate transactionTemplate, UserImportService userImportService) {

    super(log, transactionTemplate);
    this.userImportService = userImportService;
  }

  @Override
  protected T doTask(ChunkContext chunkContext) {

    Map<String,Object> jobParameters = chunkContext.getStepContext().getJobParameters();
    Long userImportId = Long.valueOf(jobParameters.get(Constants.Params.USER_IMPORT_ID).toString());
    UserImportJob userImportJob = userImportService.getImportUserJob(userImportId);

    return doUserImportTask(chunkContext, userImportJob);
  }

  protected abstract T doUserImportTask(ChunkContext chunkContext, UserImportJob userImportJob);
}
