package org.ohdsi.webapi.shiro.Entities;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

/**
 * Created by GMalikov on 24.08.2015.
 */
public interface RoleRepository extends CrudRepository<RoleEntity, Long> {

  RoleEntity findById(Long id);
  
  RoleEntity findByName(String name);

  @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM RoleEntity r WHERE r.name = ?1")
  boolean existsByName(String roleName);
}
