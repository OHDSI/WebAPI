package org.ohdsi.webapi.user.importer;

import org.ohdsi.webapi.user.importer.model.AtlasUserRoles;
import org.ohdsi.webapi.user.importer.model.LdapGroup;
import org.ohdsi.webapi.user.importer.model.LdapProviderType;
import org.ohdsi.webapi.user.importer.model.RoleGroupMapping;

import java.util.List;

public interface UserImporter {

  List<LdapGroup> findGroups(LdapProviderType providerType, String searchStr);

  List<AtlasUserRoles> findUsers(LdapProviderType providerType, RoleGroupMapping mapping);

  void importUsers(List<AtlasUserRoles> users);

  void saveRoleGroupMapping(LdapProviderType providerType, List<RoleGroupMappingEntity> mappingEntities);

  List<RoleGroupMappingEntity> getRoleGroupMapping(LdapProviderType providerType);

  void testConnection(LdapProviderType provider);
}
