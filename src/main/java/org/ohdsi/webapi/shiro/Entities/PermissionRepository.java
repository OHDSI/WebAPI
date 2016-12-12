package org.ohdsi.webapi.shiro.Entities;

import org.springframework.data.repository.CrudRepository;

/**
 * Created by GMalikov on 24.08.2015.
 */
public interface PermissionRepository extends CrudRepository<PermissionEntity, Long> {

  public PermissionEntity findById(Long id);

  public PermissionEntity findByValueIgnoreCase(String permission);
}
