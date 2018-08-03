package org.ohdsi.webapi.user.importer;

import org.ohdsi.webapi.user.importer.model.LdapProviderType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoleGroupMappingRepository extends JpaRepository<RoleGroupMappingEntity, Integer> {

  List<RoleGroupMappingEntity> findByProvider(LdapProviderType provider);
}
