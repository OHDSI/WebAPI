package org.ohdsi.webapi.shiro;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SigningKeyResolverAdapter;
import io.jsonwebtoken.impl.crypto.MacProvider;
import java.security.Key;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import org.apache.shiro.web.util.WebUtils;

/**
 *
 * @author gennadiy.anisimov
 */
public class TokenManager {
  
  private static final String AUTHORIZATION_HEADER = "Authorization";
  
  private static final Map<String, Key> userToKeyMap = new HashMap<>();
  
  public static String createJsonWebToken(String subject) {
    Key key = MacProvider.generateKey();
    if (userToKeyMap.containsKey(subject)) 
      userToKeyMap.replace(subject, key);
    else 
      userToKeyMap.put(subject, key);
    
    return Jwts.builder()
            .setSubject(subject)
            .signWith(SignatureAlgorithm.HS512, key)
            .compact();
  }
  
  public static String getSubject(String jwt) throws JwtException {    
    return Jwts.parser()
      .setSigningKeyResolver(new SigningKeyResolverAdapter() {
          @Override
          public Key resolveSigningKey(JwsHeader header, Claims claims) {
            String subject = claims.getSubject();
            if (subject == null || subject.isEmpty()) 
              throw new JwtException("Subject is not provided in JWT");

            if (!userToKeyMap.containsKey(subject)) 
              throw new JwtException("Signing key is not reqistred for the subject");

            return userToKeyMap.get(subject);
          }})
      .parseClaimsJws(jwt)
      .getBody()
      .getSubject();
  }
  
  public static Boolean invalidate(String jwt) {
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
  
  public static Boolean isValidToken(String jwt) {
    try {
      getSubject(jwt);
    } catch (JwtException e) {
      return false;
    }       
    
    return true;
  }
  
  public static String extractToken(ServletRequest request) {
    HttpServletRequest httpRequest = WebUtils.toHttp(request);
    
    String header =  httpRequest.getHeader(AUTHORIZATION_HEADER);
    if (header == null || header.isEmpty())
      return null;
    
    if (!header.toLowerCase(Locale.ENGLISH).startsWith("Bearer".toLowerCase(Locale.ENGLISH)))
      return null;
    
    String jwt = header.split(" ")[1];
    return jwt;
  }
}
