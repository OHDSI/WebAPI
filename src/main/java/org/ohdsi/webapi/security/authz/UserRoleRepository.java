package org.ohdsi.webapi.security.authz;

import java.util.List;
import java.util.Optional;

import org.ohdsi.webapi.security.authc.UserOrigin;
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

  /**
   * Find all roles for a user from a specific authentication origin.
   *
   * @param userId the user ID
   * @param origin the authentication origin (LDAP, OIDC, WINDOWS, etc.)
   * @return list of roles assigned to the user from this origin
   */
  @Query("""
      select ur
      from UserRole ur
      where ur.user.id = :userId and ur.origin = :origin
  """)
  List<UserRoleEntity> findByUserIdAndOrigin(@Param("userId") Long userId, @Param("origin") UserOrigin origin);
}
