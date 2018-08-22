package org.ohdsi.webapi.user.importer;

import org.ohdsi.webapi.user.importer.model.AtlasUserRoles;
import org.ohdsi.webapi.user.importer.model.LdapGroup;
import org.ohdsi.webapi.user.importer.model.LdapProviderType;
import org.ohdsi.webapi.user.importer.model.RoleGroupMapping;
import org.ohdsi.webapi.user.importer.RoleGroupMappingEntity;
import org.springframework.ldap.core.LdapTemplate;

import java.util.List;

public interface UserImporter {

  LdapTemplate getLdapTemplate(LdapProviderType providerType);

  List<LdapGroup> findGroups(LdapProviderType providerType, String searchStr);

  List<AtlasUserRoles> findUsers(LdapProviderType providerType, RoleGroupMapping mapping);

  void importUsers(List<AtlasUserRoles> users);

  void saveRoleGroupMapping(LdapProviderType providerType, List<RoleGroupMappingEntity> mappingEntities);

  List<RoleGroupMappingEntity> getRoleGroupMapping(LdapProviderType providerType);

  void testConnection(LdapProviderType provider);
}
