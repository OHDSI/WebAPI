package org.ohdsi.webapi.security.authz;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

/**
 * Created by GMalikov on 24.08.2015.
 */
public interface RoleRepository extends CrudRepository<RoleEntity, Long> {

  Optional<RoleEntity> findByNameAndSystemRole(String name, Boolean isSystem);

  List<RoleEntity> findByNameIgnoreCaseContaining(String roleSearch);

  Iterable<RoleEntity> findAllBySystemRoleTrue();

  @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM Role r WHERE r.name = ?1")
  boolean existsByName(String roleName);

}
