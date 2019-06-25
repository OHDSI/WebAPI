package org.ohdsi.webapi.shiro.Entities;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

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
