package org.ohdsi.webapi.model.users.entity;

import org.ohdsi.webapi.model.users.LdapProviderType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoleGroupMappingRepository extends JpaRepository<RoleGroupMappingEntity, Integer> {

  List<RoleGroupMappingEntity> findByProvider(LdapProviderType provider);
}
