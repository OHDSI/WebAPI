/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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
import java.util.Map;

/**
 *
 * @author gennadiy.anisimov
 */
public class TokenManager {

  private static final Map<String, Key> userToKeyMap = new HashMap<>();
  
  public static String createJsonWebToken(String userId) {
    Key key = MacProvider.generateKey();
    if (userToKeyMap.containsKey(userId)) 
      userToKeyMap.replace(userId, key);
    else 
      userToKeyMap.put(userId, key);
    
    return Jwts.builder()
            .setSubject(userId)
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
  
  public static Boolean isValidToken(String jwt) {
    try {
      getSubject(jwt);
    } catch (JwtException e) {
      return false;
    }       
    
    return true;
  }
}
