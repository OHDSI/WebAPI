package org.ohdsi.webapi.shiro.Entities;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.Set;

/**
 * Created by GMalikov on 24.08.2015.
 */
public interface UserRepository extends CrudRepository<UserEntity, Long> {

    public UserEntity findByLogin(String login);

    @Query("SELECT u.login FROM UserEntity u")
    public Set<String> getUserLogins();

    @Query("SELECT u.login FROM UserEntity u where id=?1")
    public String getUserLoginById(Long id);

    @Query("from UserEntity where login = 'testLogin'")
    public UserEntity getTestUser();
}
