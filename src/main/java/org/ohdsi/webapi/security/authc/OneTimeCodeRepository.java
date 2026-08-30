/*
 * Copyright 2024 Observational Health Data Sciences and Informatics [OHDSI.org].
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ohdsi.webapi.security.authc;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for One-Time Code (OTC) entities.
 * Provides methods for generating, validating, consuming, and cleaning up OTC records.
 */
@Repository
public interface OneTimeCodeRepository extends JpaRepository<OneTimeCodeEntity, UUID> {

    /**
     * Delete all expired one-time codes.
     * 
     * @param now current instant for expiration comparison
     */
    @Modifying
    @Query("""
            delete from OneTimeCode otc
            where otc.expiresAt < :now
      """)
    void deleteByExpiresAtBefore(@Param("now") Instant now);

    /**
     * Delete all revoked one-time codes.
     */
    @Modifying
    @Query("""
            delete from OneTimeCode otc
            where otc.revoked = true
      """)
    void deleteByRevoked();

    /**
     * Mark a one-time code as revoked (consumed).
     * 
     * @param code the OTC UUID to revoke
     */
    @Modifying
    @Query("""
            update OneTimeCode otc
            set otc.revoked = true
            where otc.code = :code
      """)
    void revokeByCode(@Param("code") UUID code);

    /**
     * Delete all codes for a specific login (used when user is deleted/disabled).
     * 
     * @param login the user login identifier
     */
    @Modifying
    @Query("""
            delete from OneTimeCode otc
            where otc.login = :login
      """)
    void deleteByLogin(@Param("login") String login);
}
