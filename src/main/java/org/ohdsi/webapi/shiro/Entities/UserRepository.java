package org.ohdsi.webapi.shiro.Entities;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

/**
 * Created by GMalikov on 24.08.2015.
 */
public interface UserRepository extends CrudRepository<UserEntity, Long> {

    public UserEntity findByLogin(String login);

    @Query("from UserEntity where login = 'testLogin'")
    public UserEntity getTestUser();
}
