package org.ohdsi.webapi.security.authz;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

/**
 *
 * @author gennadiy.anisimov
 */
public interface UserRoleRepository extends CrudRepository<UserRoleEntity, Long> {

  public List<UserRoleEntity> findByUser(UserEntity user);

  public Optional<UserRoleEntity> findByUserAndRole(UserEntity user, RoleEntity role);

@Query("""
    select ur.user.id
    from UserRole ur
    where ur.role.id = :roleId
""")
List<Long> findUserIdsByRoleId(@Param("roleId") Long roleId);
}
