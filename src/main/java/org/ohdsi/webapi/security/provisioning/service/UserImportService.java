package org.ohdsi.webapi.security.provisioning.service;

import java.util.List;

import org.ohdsi.webapi.security.provisioning.model.AtlasUserRoles;
import org.ohdsi.webapi.security.provisioning.model.LdapGroup;
import org.ohdsi.webapi.security.provisioning.model.LdapProviderType;
import org.ohdsi.webapi.security.provisioning.model.RoleGroupEntity;
import org.ohdsi.webapi.security.provisioning.model.RoleGroupMapping;
import org.ohdsi.webapi.security.provisioning.model.UserImportJob;
import org.ohdsi.webapi.security.provisioning.model.UserImportResult;

public interface UserImportService {

  List<LdapGroup> findGroups(LdapProviderType providerType, String searchStr);

  List<AtlasUserRoles> findUsers(LdapProviderType providerType, RoleGroupMapping mapping);

  UserImportResult importUsers(List<AtlasUserRoles> users, LdapProviderType providerType, boolean preserveRoles);

  void saveRoleGroupMapping(LdapProviderType providerType, List<RoleGroupEntity> mappingEntities);

  List<RoleGroupEntity> getRoleGroupMapping(LdapProviderType providerType);

  void testConnection(LdapProviderType provider);

  UserImportJob getImportUserJob(Long userImportId);
}