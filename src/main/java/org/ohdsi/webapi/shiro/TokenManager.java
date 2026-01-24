package org.ohdsi.webapi.shiro;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import org.ohdsi.webapi.shiro.ServletBridge;
import org.ohdsi.webapi.Constants;
import org.ohdsi.webapi.util.ExpiringMultimap;

/**
 *
 * @author gennadiy.anisimov
 */
public class TokenManager {

  private static final String AUTHORIZATION_HEADER = "Authorization";

  private static final Map<String, SecretKey> userToKeyMap = new HashMap<>();
  private static final ExpiringMultimap<String, SecretKey> gracePeriodInvalidTokens = new ExpiringMultimap<>(30000);

  public static String createJsonWebToken(String subject, String sessionId, Date expiration) {
    SecretKey key = Keys.hmacShaKeyFor(Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.HS512).getEncoded());

    SecretKey oldKey;
    if ((oldKey = userToKeyMap.get(subject)) != null) {
        gracePeriodInvalidTokens.put(subject, oldKey);
    }
    userToKeyMap.put(subject, key);

    Map<String, Object> claims = new HashMap<>();
    claims.put(Constants.SESSION_ID, sessionId);
    return Jwts.builder()
            .claims(claims)
            .subject(subject)
            .expiration(expiration)
            .signWith(key)
            .compact();
  }


  public static String getSubject(String jwt) throws JwtException {
    return getBody(jwt).getSubject();
  }

  public static Claims getBody(String jwt) {
    // Extract subject without signature verification to retrieve signing key
    String untrustedSubject = getUntrustedSubject(jwt);
    if (untrustedSubject == null) {
        throw new UnsupportedJwtException("Cannot extract subject from the token");
    }

    // Retrieve signing keys: current key + grace period keys for concurrency handling
    List<SecretKey> keyOptions = gracePeriodInvalidTokens.get(untrustedSubject);
    if (userToKeyMap.containsKey(untrustedSubject)) {
      keyOptions.add(0, userToKeyMap.get(untrustedSubject));
    }

    return keyOptions.stream()
            .map(key -> {
              try {
                return Jwts.parser()
                        .verifyWith(key)
                        .build()
                        .parseSignedClaims(jwt)
                        .getPayload();
              } catch (Exception ex) {
                return null;
              }
            })
            .filter(Objects::nonNull)
            .findFirst()
            .orElseThrow(() -> new SignatureException("Signing key is not registered for the subject."));
  }

  protected static String getUntrustedSubject(String jws) {
    try {
      // Split JWT into header.payload.signature components
      String[] parts = jws.split("\\.");
      if (parts.length != 3) {
        return null;
      }

      // Base64-decode payload to extract subject claim
      String payload = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));

      // Extract "sub" field from JSON payload
      int subIndex = payload.indexOf("\"sub\"");
      if (subIndex == -1) {
        return null;
      }

      int colonIndex = payload.indexOf(":", subIndex);
      int startQuote = payload.indexOf("\"", colonIndex);
      int endQuote = payload.indexOf("\"", startQuote + 1);

      return payload.substring(startQuote + 1, endQuote);
    } catch (Exception e) {
      return null;
    }
  }

  public static Boolean invalidate(String jwt) {
    if (jwt == null)
      return false;

    String subject;
    try {
      subject = getSubject(jwt);
    }
    catch(JwtException e) {
      return false;
    }

    if (!userToKeyMap.containsKey(subject))
      return false;

    userToKeyMap.remove(subject);
    return true;
  }

  public static String extractToken(ServletRequest request) {
    HttpServletRequest httpRequest = ServletBridge.toHttp(request);

    String header =  httpRequest.getHeader(AUTHORIZATION_HEADER);
    if (header == null || header.isEmpty())
      return null;

    if (!header.toLowerCase(Locale.ENGLISH).startsWith("bearer"))
      return null;

    String[] headerParts = header.split(" ");
    if (headerParts.length != 2)
      return null;

    return headerParts[1];
  }
}
