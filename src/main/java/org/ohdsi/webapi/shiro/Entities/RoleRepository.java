package org.ohdsi.webapi.shiro.Entities;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

/**
 * Created by GMalikov on 24.08.2015.
 */
public interface RoleRepository extends CrudRepository<RoleEntity, Long> {

  @Query("SELECT r FROM RoleEntity r LEFT JOIN FETCH r.rolePermissions LEFT JOIN FETCH r.userRoles WHERE r.id = ?1")
  RoleEntity findById(Long id);

  @Query("SELECT r FROM RoleEntity r LEFT JOIN FETCH r.rolePermissions LEFT JOIN FETCH r.userRoles WHERE r.name = ?1")
  RoleEntity findByName(String name);

  RoleEntity findByNameAndSystemRole(String name, Boolean isSystem);

  Iterable<RoleEntity> findAllBySystemRoleTrue();

  @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM RoleEntity r WHERE r.name = ?1")
  boolean existsByName(String roleName);
}
