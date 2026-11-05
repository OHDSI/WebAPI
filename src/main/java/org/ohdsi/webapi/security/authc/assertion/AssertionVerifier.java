package org.ohdsi.webapi.security.authc.assertion;

import java.util.Locale;

import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;

/**
 * Verifies the assertion header on a request.
 *
 * <p>Failures are split by cause, because they call for different responses: a
 * missing or bad assertion is the client's problem (401), while an unreachable
 * JWKS is ours (503). Treating an outage as "bad token" would sign everyone out.
 */
public class AssertionVerifier {

  private static final Logger log = LoggerFactory.getLogger(AssertionVerifier.class);

  /** Result of checking one request. */
  public sealed interface Outcome permits Verified, Missing, Invalid, Unavailable {}

  /** A valid assertion. {@code login} is already lower-cased, as WebAPI stores logins. */
  public record Verified(Jwt jwt, String login, String name) implements Outcome {}

  public record Missing() implements Outcome {}

  public record Invalid(String reason) implements Outcome {}

  public record Unavailable(String reason) implements Outcome {}

  private final JwtDecoder decoder;
  private final AssertionAuthProperties props;

  public AssertionVerifier(JwtDecoder decoder, AssertionAuthProperties props) {
    this.decoder = decoder;
    this.props = props;
  }

  public Outcome verify(HttpServletRequest request) {
    String token = request.getHeader(props.getHeader());
    if (token == null || token.isBlank()) {
      return new Missing();
    }
    return verify(token.trim());
  }

  public Outcome verify(String token) {
    Jwt jwt;
    try {
      jwt = decoder.decode(token);
    } catch (BadJwtException e) {
      // Malformed, wrong algorithm, bad signature, unknown key, or failed
      // issuer/audience/time validation.
      log.debug("Assertion refused: {}", e.getMessage());
      return new Invalid(e.getMessage());
    } catch (JwtException e) {
      // Anything else from the decoder, chiefly a JWKS that couldn't be fetched.
      log.warn("Assertion could not be checked: {}", e.getMessage());
      return new Unavailable(e.getMessage());
    }

    String login = jwt.getClaimAsString(props.getUsernameClaim());
    if (login == null || login.isBlank()) {
      return new Invalid("assertion has no " + props.getUsernameClaim() + " claim");
    }
    login = login.trim().toLowerCase(Locale.ROOT);
    return new Verified(jwt, login, displayName(jwt, login));
  }

  private String displayName(Jwt jwt, String login) {
    String given = jwt.getClaimAsString(props.getGivenNameClaim());
    String family = jwt.getClaimAsString(props.getFamilyNameClaim());
    String name = ((given == null ? "" : given.trim()) + " " + (family == null ? "" : family.trim())).trim();
    return name.isEmpty() ? login : name;
  }
}
