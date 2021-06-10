package org.ohdsi.webapi.shiro.Entities;

import java.util.List;
import org.springframework.data.repository.CrudRepository;

/**
 *
 * @author gennadiy.anisimov
 */
public interface UserRoleRepository extends CrudRepository<UserRoleEntity, Long> {

  public List<UserRoleEntity> findByUser(UserEntity user);

  public UserRoleEntity findByUserAndRole(UserEntity user, RoleEntity role);

  public List<UserRoleEntity> findByStatusIgnoreCase(String status);

  public List<UserRoleEntity> findByUserId(Long userId);

  public UserRoleEntity findByUserIdAndRoleId(Long userId, Long roleId);

}
