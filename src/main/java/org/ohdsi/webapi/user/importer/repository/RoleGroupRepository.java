package org.ohdsi.webapi.user.importer.repository;

import org.ohdsi.webapi.user.importer.model.RoleGroupEntity;
import org.ohdsi.webapi.user.importer.model.LdapProviderType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoleGroupRepository extends JpaRepository<RoleGroupEntity, Integer> {

  List<RoleGroupEntity> findByProviderAndUserImportJobNull(LdapProviderType provider);

  void deleteByRoleId(Long roleId);
}
