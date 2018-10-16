package org.ohdsi.webapi.user.importer;

import org.ohdsi.webapi.user.importer.model.*;

import java.util.List;

public interface UserImporter {

  List<LdapGroup> findGroups(LdapProviderType providerType, String searchStr);

  List<AtlasUserRoles> findUsers(LdapProviderType providerType, RoleGroupMapping mapping);

  void importUsers(List<AtlasUserRoles> users, List<String> defaultRoles);

  void saveRoleGroupMapping(LdapProviderType providerType, List<RoleGroupEntity> mappingEntities);

  List<RoleGroupEntity> getRoleGroupMapping(LdapProviderType providerType);

  void testConnection(LdapProviderType provider);
}
