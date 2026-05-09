package org.ohdsi.webapi.security.provisioning.model;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoleGroupRepository extends JpaRepository<RoleGroupEntity, Integer> {

  List<RoleGroupEntity> findByProviderAndUserImportJobNull(LdapProviderType provider);

  void deleteByRoleId(Long roleId);
}
