package org.ohdsi.webapi.security.authz;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.ohdsi.webapi.security.authc.UserOrigin;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

/**
 * Created by GMalikov on 24.08.2015.
 */
public interface UserRepository extends CrudRepository<UserEntity, Long> {

    Optional<UserEntity> findByLogin(String login);

    @Query("SELECT u.login FROM User u")
    public Set<String> getUserLogins();

    List<UserEntity> findByOrigin(UserOrigin origin);
}
