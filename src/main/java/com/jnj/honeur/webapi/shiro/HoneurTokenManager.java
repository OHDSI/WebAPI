package com.jnj.honeur.webapi.shiro;

import com.jnj.honeur.security.SecurityUtils2;
import io.jsonwebtoken.*;
import io.jsonwebtoken.impl.crypto.MacProvider;
import org.apache.shiro.web.util.WebUtils;
import org.ohdsi.webapi.shiro.TokenManager;
import org.pac4j.cas.profile.CasProfile;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Calendar;

/**
 *
 * @author gennadiy.anisimov
 */
public class HoneurTokenManager {
  
  private static final String AUTHORIZATION_HEADER = "Authorization";
  
  private static final Map<String, Key> userToKeyMap = new HashMap<>();
  
  public static String createJsonWebToken(String subject, Date expiration) {

    CasProfile casProfile = new CasProfile();
    casProfile.setId(subject);

    return SecurityUtils2.generateJwtToken(casProfile, expiration);
  }
  
  public static String getSubject(String jwt) throws JwtException {
    String subject = SecurityUtils2.getSubject(jwt);
    if(subject == null){
      throw new JwtException("Token is invalid or expired.");
    }
    return SecurityUtils2.getSubject(jwt);
  }
  
  public static Boolean invalidate(String jwt) {
    return TokenManager.invalidate(jwt);
  }
  
  public static String extractToken(ServletRequest request) {
    return TokenManager.extractToken(request);
  }

  public static Date getExpirationDate(final int expirationIntervalInSeconds) {
    Calendar calendar = Calendar.getInstance();
    calendar.add(Calendar.SECOND, expirationIntervalInSeconds);
    return calendar.getTime();
  }
}
