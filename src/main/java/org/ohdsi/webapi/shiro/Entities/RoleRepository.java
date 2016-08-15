package org.ohdsi.webapi.shiro.Entities;

import org.springframework.data.repository.CrudRepository;

/**
 * Created by GMalikov on 24.08.2015.
 */
public interface RoleRepository extends CrudRepository<RoleEntity, Long> {

  public RoleEntity findById(Long id);
  
  public RoleEntity findByName(String name);
}
