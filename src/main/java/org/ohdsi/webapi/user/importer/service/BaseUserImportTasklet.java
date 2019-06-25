package org.ohdsi.webapi.user.importer.service;

import org.ohdsi.analysis.Utils;
import org.ohdsi.webapi.Constants;
import org.ohdsi.webapi.common.generation.TransactionalTasklet;
import org.ohdsi.webapi.user.importer.model.LdapProviderType;
import org.ohdsi.webapi.user.importer.model.RoleGroupMapping;
import org.slf4j.Logger;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Map;
import java.util.Objects;

public abstract class BaseUserImportTasklet<T> extends TransactionalTasklet<T> {

  protected final UserImportService userImportService;

  public BaseUserImportTasklet(Logger log, TransactionTemplate transactionTemplate, UserImportService userImportService) {

    super(log, transactionTemplate);
    this.userImportService = userImportService;
  }

  @Override
  protected T doTask(ChunkContext chunkContext) {

    Map<String,Object> jobParameters = chunkContext.getStepContext().getJobParameters();
    Boolean preserveRoles = Boolean.valueOf(jobParameters.getOrDefault(Constants.Params.PRESERVE_ROLES, "TRUE").toString());
    String provider = (String)jobParameters.get(Constants.Params.LDAP_PROVIDER);
    if (Objects.isNull(provider)) {
      throw new IllegalArgumentException("provider is required for user import");
    }
    LdapProviderType providerType = LdapProviderType.fromValue(provider);
    String roleGroupMappingJson = (String) jobParameters.get(Constants.Params.ROLE_GROUP_MAPPING);
    RoleGroupMapping roleGroupMapping = Utils.deserialize(roleGroupMappingJson, RoleGroupMapping.class);
    return doUserImportTask(chunkContext, providerType, preserveRoles, roleGroupMapping);
  }

  protected abstract T doUserImportTask(ChunkContext chunkContext, LdapProviderType providerType, Boolean preserveRoles, RoleGroupMapping roleGroupMapping);
}
