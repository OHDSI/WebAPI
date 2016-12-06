package org.ohdsi.webapi.shiro.Entities;

import java.util.List;
import org.springframework.data.repository.CrudRepository;

/**
 *
 * @author gennadiy.anisimov
 */
public interface RolePermissionRepository extends CrudRepository<RolePermissionEntity, Long> {
  
  public RolePermissionEntity findById(Long id);
  
  public RolePermissionEntity findByRoleAndPermission(RoleEntity role, PermissionEntity permission);

  public RolePermissionEntity findByRoleIdAndPermissionId(Long roleId, Long permissionId);

  public List<RolePermissionEntity> findByStatusIgnoreCase(String status);
}
