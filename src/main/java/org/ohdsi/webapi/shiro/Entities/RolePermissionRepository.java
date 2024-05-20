package org.ohdsi.webapi.shiro.Entities;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

/**
 *
 * @author gennadiy.anisimov
 */
public interface RolePermissionRepository extends CrudRepository<RolePermissionEntity, Long> {
  
  /* RolePermissionEntity findById(Long id);   MDACA Spring Boot 3 migration compilation issue */
  Optional<RolePermissionEntity> findById(Long id);
  
  RolePermissionEntity findByRoleAndPermission(RoleEntity role, PermissionEntity permission);

  RolePermissionEntity findByRoleIdAndPermissionId(Long roleId, Long permissionId);

  List<RolePermissionEntity> findByStatusIgnoreCase(String status);
}
