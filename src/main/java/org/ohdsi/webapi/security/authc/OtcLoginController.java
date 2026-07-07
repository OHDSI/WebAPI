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

import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for redeeming one-time codes (OTC) for JWT tokens.
 * 
 * OTC redemption flow:
 * 1. Client receives OTC from OAuth2 provider callback (e.g., ?otc=<uuid>)
 * 2. Client calls this endpoint with OTC: GET /user/login/otc?code=<uuid>
 * 3. Service validates OTC (not expired, not revoked, exists)
 * 4. Returns pre-minted JWT embedded in OTC
 * 5. Client uses JWT to establish WebAPI session
 * 
 * This endpoint is publicly accessible (no auth required) to support the initial login flow.
 */
@RestController
@RequestMapping("/user/login")
public class OtcLoginController {

  private static final Logger log = LoggerFactory.getLogger(OtcLoginController.class);

  private final OneTimeCodeService oneTimeCodeService;

  public OtcLoginController(OneTimeCodeService oneTimeCodeService) {
    this.oneTimeCodeService = oneTimeCodeService;
  }

  /**
   * Redeem a one-time code for a JWT token.
   * 
   * @param code the OTC UUID to redeem
   * @return JWT token wrapped in LoginService.Result, or 401 if OTC is invalid/expired/revoked
   */
  @GetMapping(value = "/otc", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<LoginService.Result> redeemOtc(@RequestParam UUID code) {

    Optional<OneTimeCodeEntity> otcEntity = oneTimeCodeService.validateAndConsume(code);

    if (otcEntity.isEmpty()) {
      log.debug("OTC redemption failed: code {} invalid or expired", code);
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(new LoginService.Result(null, null, null, "Invalid or expired code"));
    }

    OneTimeCodeEntity otc = otcEntity.get();
    String jwt = otc.getJwtToken();

    log.debug("OTC: {} redeemed successfully for user: {}", code, otc.getLogin());

    // Return the pre-minted JWT directly
    // Client uses this token to establish session with WebAPI
    return ResponseEntity.ok(new LoginService.Result(null, jwt, null, null));
  }
}
