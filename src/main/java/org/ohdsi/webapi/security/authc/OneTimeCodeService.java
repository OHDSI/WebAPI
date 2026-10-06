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
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for generating and validating one-time codes (OTC).
 * OTC wraps pre-minted JWT tokens to support load-balanced WebAPI instances.
 * Each code is single-use and expires after a configurable TTL (default 10 minutes).
 */
@Service
@Transactional
public class OneTimeCodeService {

  private final OneTimeCodeRepository repo;
  private final OneTimeCodeProperties props;

  private static final Logger log = LoggerFactory.getLogger(OneTimeCodeService.class);

  public OneTimeCodeService(
      OneTimeCodeRepository repo,
      OneTimeCodeProperties props) {
    this.repo = repo;
    this.props = props;
  }

  /**
   * Generate a one-time code that wraps a pre-minted JWT token.
   * 
   * @param login the user login identifier
   * @param origin the authentication origin (GOOGLE, FACEBOOK, OIDC, etc.)
   * @param jwtToken the pre-minted JWT token to embed
   * @return UUID of the generated OTC
   */
  public UUID generateCode(String login, UserOrigin origin, String jwtToken) {
    UUID code = UUID.randomUUID();
    Instant now = Instant.now();
    Instant expiresAt = now.plus(props.getTtl());

    OneTimeCodeEntity otc = new OneTimeCodeEntity();
    otc.setCode(code);
    otc.setLogin(login);
    otc.setOrigin(origin.name());
    otc.setJwtToken(jwtToken);
    otc.setCreatedAt(now);
    otc.setExpiresAt(expiresAt);
    otc.setRevoked(false);

    repo.save(otc);
    log.debug("OTC: {} generated for user: {} from provider: {}", code, login, origin);

    return code;
  }

  /**
   * Validate and consume a one-time code.
   * Checks expiration and revocation status. On success, marks the code as revoked (single-use).
   * 
   * @param code the OTC UUID to validate
   * @return Optional containing the OTC entity if valid, empty if invalid/expired/already revoked
   */
  public Optional<OneTimeCodeEntity> validateAndConsume(UUID code) {
    Optional<OneTimeCodeEntity> otcOpt = repo.findById(code);

    if (otcOpt.isEmpty()) {
      log.debug("OTC: {} not found", code);
      return Optional.empty();
    }

    OneTimeCodeEntity otc = otcOpt.get();

    // Check expiration
    if (otc.getExpiresAt().isBefore(Instant.now())) {
      log.debug("OTC: {} expired", code);
      return Optional.empty();
    }

    // Check revocation (already used)
    if (otc.isRevoked()) {
      log.debug("OTC: {} already revoked (single-use)", code);
      return Optional.empty();
    }

    // Mark as revoked (consumed) - single use
    repo.revokeByCode(code);
    log.debug("OTC: {} consumed for user: {}", code, otc.getLogin());

    return Optional.of(otc);
  }

  /**
   * Clean up expired and revoked one-time codes.
   * Called periodically to remove old OTC records.
   */
  @Scheduled(fixedDelayString = "#{@oneTimeCodeProperties.ttl.toMillis()}")
  public void cleanupExpiredCodes() {
    Instant now = Instant.now();
    repo.deleteByExpiresAtBefore(now);
    repo.deleteByRevoked();
    log.debug("Cleanup completed for expired and revoked one-time codes");
  }

  /**
   * Revoke all OTC codes for a specific user (e.g., when user is deleted or disabled).
   * 
   * @param login the user login identifier
   */
  public void deleteCodesByLogin(String login) {
    repo.deleteByLogin(login);
    log.debug("Deleted all OTC codes for user: {}", login);
  }
}
