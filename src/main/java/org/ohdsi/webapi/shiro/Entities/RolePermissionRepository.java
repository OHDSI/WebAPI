package org.ohdsi.webapi.shiro.Entities;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 *
 * @author gennadiy.anisimov
 */
public interface RolePermissionRepository extends CrudRepository<RolePermissionEntity, Long> {
  
  RolePermissionEntity findById(Long id);
  
  RolePermissionEntity findByRoleAndPermission(RoleEntity role, PermissionEntity permission);

  RolePermissionEntity findByRoleIdAndPermissionId(Long roleId, Long permissionId);

  List<RolePermissionEntity> findByStatusIgnoreCase(String status);
}
