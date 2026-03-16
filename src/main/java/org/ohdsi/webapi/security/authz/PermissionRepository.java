package org.ohdsi.webapi.security.authz;

import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

/**
 * Created by GMalikov on 24.08.2015.
 */
public interface PermissionRepository extends CrudRepository<PermissionEntity, Long> {

  public Optional<PermissionEntity> findByValueIgnoreCase(String permission);

  @Query("""
    SELECT DISTINCT p.value
    FROM UserRole ur
    JOIN ur.role r
    JOIN r.rolePermissions rp
    JOIN rp.permission p
    WHERE ur.user.id = :userId
""")
  public Set<String> queryUserPermissions(@Param("userId") Long userId);

}
