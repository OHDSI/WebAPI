package org.ohdsi.webapi.security.provisioning.model;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GroupRoleImportRepository extends JpaRepository<GroupRoleImportEntity, Integer> {

  List<GroupRoleImportEntity> findByProviderAndUserImportJobNull(LdapProviderType provider);

  void deleteByRoleId(Long roleId);
}
