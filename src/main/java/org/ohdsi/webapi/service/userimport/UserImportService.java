package org.ohdsi.webapi.service.userimport;

import org.ohdsi.webapi.model.users.AtlasUserRoles;
import org.ohdsi.webapi.model.users.LdapGroup;
import org.ohdsi.webapi.model.users.LdapProviderType;
import org.ohdsi.webapi.model.users.RoleGroupMapping;
import org.ohdsi.webapi.model.users.entity.RoleGroupMappingEntity;
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
