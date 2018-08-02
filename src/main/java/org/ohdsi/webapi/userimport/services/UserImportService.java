package org.ohdsi.webapi.userimport.services;

import org.ohdsi.webapi.userimport.model.AtlasUserRoles;
import org.ohdsi.webapi.userimport.model.LdapGroup;
import org.ohdsi.webapi.userimport.model.LdapProviderType;
import org.ohdsi.webapi.userimport.model.RoleGroupMapping;
import org.ohdsi.webapi.userimport.entities.RoleGroupMappingEntity;
import org.springframework.ldap.core.LdapTemplate;

import java.util.List;

public interface UserImportService {

  LdapTemplate getLdapTemplate(LdapProviderType providerType);

  List<LdapGroup> findGroups(LdapProviderType providerType, String searchStr);

  List<AtlasUserRoles> findUsers(LdapProviderType providerType, RoleGroupMapping mapping);

  void importUsers(List<AtlasUserRoles> users);

  void saveRoleGroupMapping(LdapProviderType providerType, List<RoleGroupMappingEntity> mappingEntities);

  List<RoleGroupMappingEntity> getRoleGroupMapping(LdapProviderType providerType);
}
