package org.ohdsi.webapi.shiro;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MissingClaimException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.SigningKeyResolverAdapter;
import io.jsonwebtoken.impl.crypto.MacProvider;
import java.security.Key;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;
import javax.servlet.ServletRequest;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.web.util.WebUtils;

/**
 *
 * @author gennadiy.anisimov
 */
public class TokenManager {
  
  private static final String AUTHORIZATION_HEADER = "Authorization";
  private static final String TOKEN_NAME = "bearerToken";
  
  private static final Map<String, Key> userToKeyMap = new HashMap<>();
  
  public static String createJsonWebToken(String subject, Date expiration) {
    Key key = MacProvider.generateKey();
    userToKeyMap.put(subject, key);

    Map<String, Object> claims = new HashMap<>();
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
  
  private static Claims getBody(String jwt) {
    return Jwts.parser()
      .setSigningKeyResolver(new SigningKeyResolverAdapter() {
          @Override
          public Key resolveSigningKey(JwsHeader header, Claims claims) {
            String subject = claims.getSubject();
            if (subject == null || subject.isEmpty()) 
              throw new MissingClaimException(header, claims, "Subject is not provided in JWT.");

            if (!userToKeyMap.containsKey(subject)) 
              throw new SignatureException("Signing key is not reqistred for the subject.");
            
            return userToKeyMap.get(subject);
          }})
      .parseClaimsJws(jwt)
      .getBody();   
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

  public static String extractToken(ServletRequest request, Collection<String> allowedUrls) {
    HttpServletRequest httpRequest = WebUtils.toHttp(request);

    String jwt = null;
    
    String header = httpRequest.getHeader(AUTHORIZATION_HEADER);
    if (header == null || header.isEmpty() || !StringUtils.startsWith(StringUtils.lowerCase(header, Locale.ENGLISH), "bearer")) {
      HttpServletRequest httpServletRequest = (HttpServletRequest) request;
      if (allowedUrls != null && allowedUrls.contains(httpServletRequest.getPathInfo())) {
        jwt = getTokenFromCookies(httpServletRequest);
      }
    } else {
      String[] headerParts = header.split(" ");
      if (headerParts.length == 2) {
        jwt = headerParts[1];
      }
    }
    
    return jwt;
  }

  private static String getTokenFromCookies(final HttpServletRequest request) {

    return Stream.of(request.getCookies())
            .filter(cookie -> TOKEN_NAME.equals(cookie.getName()))
            .findFirst()
            .map(Cookie::getValue)
            .orElse(null);
  }
}
