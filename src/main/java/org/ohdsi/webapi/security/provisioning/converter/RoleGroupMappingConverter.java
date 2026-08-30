package org.ohdsi.webapi.security.provisioning.converter;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.ohdsi.webapi.security.authz.Role;
import org.ohdsi.webapi.security.authz.RoleEntity;
import org.ohdsi.webapi.security.provisioning.model.LdapGroup;
import org.ohdsi.webapi.security.provisioning.model.LdapProviderType;
import org.ohdsi.webapi.security.provisioning.model.GroupRoleImportEntity;
import org.ohdsi.webapi.security.provisioning.model.RoleGroupMapping;
import org.ohdsi.webapi.security.provisioning.model.RoleGroupsMap;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RoleGroupMappingConverter {

  public static RoleGroupMapping convertRoleGroupMapping(String provider, List<GroupRoleImportEntity> mappingEntities) {

    RoleGroupMapping roleGroupMapping = new RoleGroupMapping();
    roleGroupMapping.setProvider(provider);
    Map<Long, List<GroupRoleImportEntity>> entityMap = mappingEntities.stream()
            .collect(Collectors.groupingBy(r -> r.getRole().getId()));
    Map<Long, RoleEntity> roleMap = entityMap.entrySet().stream().map(e -> new ImmutablePair<>(e.getKey(), e.getValue().iterator().next().getRole()))
            .collect(Collectors.toMap(ImmutablePair::getKey, ImmutablePair::getValue));

    List<RoleGroupsMap> roleGroups = entityMap
            .entrySet().stream().map(entry -> {
              RoleGroupsMap roleGroupsMap = new RoleGroupsMap();
              roleGroupsMap.setRole(Role.fromEntity(roleMap.get(entry.getKey())));
              List<LdapGroup> groups = entry
                      .getValue()
                      .stream()
                      .map(roleGroup -> new LdapGroup(roleGroup.getGroupName(), roleGroup.getGroupDn()))
                      .collect(Collectors.toList());
              roleGroupsMap.setGroups(groups);
              return roleGroupsMap;
            }).collect(Collectors.toList());
    roleGroupMapping.setRoleGroups(roleGroups);
    return roleGroupMapping;
  }

  public static List<GroupRoleImportEntity> convertRoleGroupMapping(RoleGroupMapping mapping) {

    final String providerTypeName = mapping.getProvider();
    final LdapProviderType providerTyper = LdapProviderType.fromValue(providerTypeName);
    return mapping.getRoleGroups().stream().flatMap(m -> {
      RoleEntity roleEntity = convertRole(m.getRole());
      return m.getGroups().stream().map(g -> {
        GroupRoleImportEntity entity = new GroupRoleImportEntity();
        entity.setGroupDn(g.getDistinguishedName());
        entity.setGroupName(g.getDisplayName());
        entity.setRole(roleEntity);
        entity.setProvider(providerTyper);
        return entity;
      });
    }).collect(Collectors.toList());
  }

  private static RoleEntity convertRole(Role role) {
    RoleEntity roleEntity = new RoleEntity();
    roleEntity.setName(role.name());
    roleEntity.setId(role.id());
    roleEntity.setSystemRole(role.systemRole());
    return roleEntity;
  }
}
