package org.ohdsi.webapi.user.importer.service;

import org.ohdsi.webapi.user.importer.model.*;

import java.util.List;

public interface UserImportService {

  List<LdapGroup> findGroups(LdapProviderType providerType, String searchStr);

  List<AtlasUserRoles> findUsers(LdapProviderType providerType, RoleGroupMapping mapping);

  UserImportResult importUsers(List<AtlasUserRoles> users, LdapProviderType providerType, boolean preserveRoles);

  void saveRoleGroupMapping(LdapProviderType providerType, List<RoleGroupEntity> mappingEntities);

  List<RoleGroupEntity> getRoleGroupMapping(LdapProviderType providerType);

  void testConnection(LdapProviderType provider);

  UserImportJob getImportUserJob(Long userImportId);
}