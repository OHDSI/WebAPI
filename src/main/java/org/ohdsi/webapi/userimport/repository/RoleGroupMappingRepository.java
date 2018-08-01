package org.ohdsi.webapi.userimport.repository;

import org.ohdsi.webapi.userimport.model.LdapProviderType;
import org.ohdsi.webapi.userimport.entities.RoleGroupMappingEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoleGroupMappingRepository extends JpaRepository<RoleGroupMappingEntity, Integer> {

  List<RoleGroupMappingEntity> findByProvider(LdapProviderType provider);
}
