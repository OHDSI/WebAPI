package org.ohdsi.webapi.shiro.Entities;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Created by GMalikov on 24.08.2015.
 */
public interface RoleRepository extends CrudRepository<RoleEntity, Long> {

  RoleEntity findById(Long id);

  RoleEntity findByNameAndSystemRole(String name, Boolean isSystem);

  List<RoleEntity> findByNameIgnoreCaseContaining(String roleSearch);

  Iterable<RoleEntity> findAllBySystemRoleTrue();

  @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM RoleEntity r WHERE r.name = ?1")
  boolean existsByName(String roleName);

  @Query(
    "SELECT r " +
    "FROM RoleEntity r " +
    "JOIN RolePermissionEntity rp ON r.id = rp.role.id " +
    "JOIN PermissionEntity p ON rp.permission.id = p.id " +
    "WHERE p.value IN :permissions " +
    "GROUP BY r.id, r.name, r.systemRole " +
    "HAVING COUNT(p.value) = :permissionCnt"
  )
  List<RoleEntity> finaAllRolesHavingPermissions(@Param("permissions") List<String> permissions, @Param("permissionCnt") Long permissionCnt);
}
