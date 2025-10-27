package org.ohdsi.webapi.shiro.Entities;

import java.util.List;
import org.springframework.data.repository.CrudRepository;

/**
 *
 * @author gennadiy.anisimov
 */
public interface RolePermissionRepository extends CrudRepository<RolePermissionEntity, Long> {
  
  // Removed: clashes with CrudRepository.findById which returns Optional<T>
  //   RolePermissionEntity findById(Long id);
  
  RolePermissionEntity findByRoleAndPermission(RoleEntity role, PermissionEntity permission);

  RolePermissionEntity findByRoleIdAndPermissionId(Long roleId, Long permissionId);

  List<RolePermissionEntity> findByStatusIgnoreCase(String status);
}
