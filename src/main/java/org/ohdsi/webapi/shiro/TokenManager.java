package org.ohdsi.webapi.shiro;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.impl.crypto.MacProvider;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import org.apache.shiro.web.util.WebUtils;
import org.ohdsi.webapi.Constants;
import org.ohdsi.webapi.util.ExpiringMultimap;

/**
 *
 * @author gennadiy.anisimov
 */
public class TokenManager {

  private static final String AUTHORIZATION_HEADER = "Authorization";

  private static final Map<String, Key> userToKeyMap = new HashMap<>();
  private static final ExpiringMultimap<String, Key> gracePeriodInvalidTokens = new ExpiringMultimap<>(30000);

  public static String createJsonWebToken(String subject, String sessionId, Date expiration) {
    Key key = MacProvider.generateKey();

    Key oldKey;
    if ((oldKey = userToKeyMap.get(subject)) != null) {
        gracePeriodInvalidTokens.put(subject, oldKey);
    }
    userToKeyMap.put(subject, key);

    Map<String, Object> claims = new HashMap<>();
    claims.put(Constants.SESSION_ID, sessionId);
    return Jwts.builder()
            .setClaims(claims)
            .setSubject(subject)
            .setExpiration(expiration)
            .signWith(SignatureAlgorithm.HS512, key)
            .compact();
  }


  public static String getSubject(String jwt) throws JwtException {
    return getBody(jwt).getSubject();
  }

  public static Claims getBody(String jwt) {

    // Get untrusted subject for secret key retrieval
    String untrustedSubject = getUntrustedSubject(jwt);
    if (untrustedSubject == null) {
        throw new UnsupportedJwtException("Cannot extract subject from the token");
    }

    // Pick all secret keys: latest one + previous keys, which were just invalidated (to overcome concurrency issue)
    List<Key> keyOptions = gracePeriodInvalidTokens.get(untrustedSubject);
    if (userToKeyMap.containsKey(untrustedSubject)) {
      keyOptions.add(0, userToKeyMap.get(untrustedSubject));
    }

    return keyOptions.stream()
            .map(key -> {
              try {
                return Jwts.parser()
                        .setSigningKey(key)
                        .parseClaimsJws(jwt)
                        .getBody();
              } catch (Exception ex) {
                return null;
              }
            })
            .filter(Objects::nonNull)
            .findFirst()
            .orElseThrow(() -> new SignatureException("Signing key is not registered for the subject."));
  }

  protected static String getUntrustedSubject(String jws) {
    int i = jws.lastIndexOf('.');
    if (i == -1) {
        return null;
    }
    String untrustedJwtString = jws.substring(0, i+1);
    return Jwts.parser().parseClaimsJwt(untrustedJwtString).getBody().getSubject();
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
    HttpServletRequest httpRequest = WebUtils.toHttp(request);

    String header =  httpRequest.getHeader(AUTHORIZATION_HEADER);
    if (header == null || header.isEmpty())
      return null;

    if (!header.toLowerCase(Locale.ENGLISH).startsWith("bearer"))
      return null;

    String[] headerParts = header.split(" ");
    if (headerParts.length != 2)
      return null;

    String jwt = headerParts[1];
    return jwt;
  }
}
