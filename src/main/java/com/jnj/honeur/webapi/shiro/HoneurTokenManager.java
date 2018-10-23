package com.jnj.honeur.webapi.shiro;

import com.jnj.honeur.security.SecurityUtils2;
import io.jsonwebtoken.JwtException;
import org.ohdsi.webapi.shiro.TokenManager;
import org.pac4j.cas.profile.CasProfile;

import javax.servlet.ServletRequest;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 *
 */
public class HoneurTokenManager {

    private static final List<String> userList = new ArrayList<>();

    public static String createJsonWebToken(String subject, Date expiration) {

        CasProfile casProfile = new CasProfile();
        casProfile.setId(subject);

        userList.add(subject);
        return SecurityUtils2.generateJwtToken(casProfile, expiration);
    }

    public static String getSubject(String jwt) throws JwtException {
        String subject = SecurityUtils2.getSubject(jwt);
        if (subject == null) {
            throw new JwtException("Token is invalid or expired.");
        }
        return SecurityUtils2.getSubject(jwt);
    }

    public static Boolean invalidate(String jwt) {
        if (jwt == null)
            return false;

        String subject;
        try {
            subject = SecurityUtils2.getSubject(jwt);
        } catch (JwtException e) {
            return false;
        }

        if (!userList.contains(subject))
            return false;

        userList.remove(subject);
        return true;
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
