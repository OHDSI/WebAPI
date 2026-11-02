package org.ohdsi.webapi.security.apikey;

import org.ohdsi.webapi.security.authz.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApiKeyRepository extends JpaRepository<ApiKeyEntity, Long> {

    /**
     * O(1) lookup by the public identifier portion of the key.
     * The identifier column has a UNIQUE index, so this is an indexed point lookup.
     */
    Optional<ApiKeyEntity> findByKeyIdentifier(String keyIdentifier);

    List<ApiKeyEntity> findByUser(UserEntity user);
}
