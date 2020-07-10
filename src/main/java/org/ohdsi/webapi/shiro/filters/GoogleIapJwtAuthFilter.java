package org.ohdsi.webapi.shiro.filters;


import com.google.common.base.Preconditions;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.ECDSAVerifier;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import io.buji.pac4j.subject.Pac4jPrincipal;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.web.util.WebUtils;
import org.ohdsi.webapi.Constants;
import org.ohdsi.webapi.shiro.PermissionManager;
import org.ohdsi.webapi.shiro.tokens.JwtAuthToken;
import org.pac4j.core.profile.CommonProfile;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URL;
import java.security.interfaces.ECPublicKey;
import java.text.ParseException;
import java.time.Clock;
import java.time.Instant;
import java.util.*;

public class GoogleIapJwtAuthFilter extends AtlasAuthFilter {

    private static final String PUBLIC_KEY_VERIFICATION_URL =
            "https://www.gstatic.com/iap/verify/public_key-jwk";
    private static final String IAP_ISSUER_URL = "https://cloud.google.com/iap";
    private static final String JWT_HEADER = "x-goog-iap-jwt-assertion";

    // using a simple cache with no eviction
    private final Map<String, JWK> keyCache = new HashMap<>();

    private static Clock clock = Clock.systemUTC();

    private PermissionManager authorizer;
    private Set<String> defaultRoles;
    private final Long cloudProjectId;
    private final Long backendServiceId;

    public GoogleIapJwtAuthFilter(PermissionManager authorizer,
                                  Set<String> defaultRoles,
                                  Long cloudProjectId,
                                  Long backendServiceId) {

        this.authorizer = authorizer;
        this.defaultRoles = defaultRoles;
        this.cloudProjectId = cloudProjectId;
        this.backendServiceId = backendServiceId;
    }

    @Override
    protected JwtAuthToken createToken(ServletRequest request, ServletResponse response) throws Exception {

        String jwtToken = getJwtToken(request);
        String login = verifyJwt(
                jwtToken,
                String.format(
                        "/projects/%s/global/backendServices/%s",
                        Long.toUnsignedString(cloudProjectId), Long.toUnsignedString(backendServiceId)
                )
        );
        return new JwtAuthToken(login);
    }

    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {

        HttpServletResponse httpResponse = WebUtils.toHttp(response);
        httpResponse.setHeader(Constants.Headers.AUTH_PROVIDER, Constants.SecurityProviders.GOOGLE);

        boolean loggedIn = executeLogin(request, response);

        if (loggedIn) {
            String name, login;
            final PrincipalCollection principals = SecurityUtils.getSubject().getPrincipals();
            final Pac4jPrincipal pac4jPrincipal = principals.oneByType(Pac4jPrincipal.class);
            if (Objects.nonNull(pac4jPrincipal)) {
                CommonProfile profile = pac4jPrincipal.getProfile();
                login = profile.getEmail();
                name = Optional.ofNullable(profile.getDisplayName()).orElse(login);
            } else {
                name = (String) principals.getPrimaryPrincipal();
                login = name;
            }
            // For now we supposed name to be equal to login for google iap
            this.authorizer.registerUser(login, name, defaultRoles);
        } else {
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }

        return loggedIn;
    }

    private ECPublicKey getKey(String kid, String alg) throws Exception {

        JWK jwk = keyCache.get(kid);
        if (jwk == null) {
            // update cache loading jwk public key data from url
            JWKSet jwkSet = JWKSet.load(new URL(PUBLIC_KEY_VERIFICATION_URL));
            for (JWK key : jwkSet.getKeys()) {
                keyCache.put(key.getKeyID(), key);
            }
            jwk = keyCache.get(kid);
        }
        // confirm that algorithm matches
        if (jwk != null && jwk.getAlgorithm().getName().equals(alg)) {
            return ECKey.parse(jwk.toJSONString()).toECPublicKey();
        }
        return null;
    }

    private String getJwtToken(ServletRequest request) {

        HttpServletRequest httpRequest = WebUtils.toHttp(request);
        return httpRequest.getHeader(JWT_HEADER);
    }

    private String verifyJwt(String jwtToken, String expectedAudience) throws Exception {

        try {
            // parse signed token into header / claims
            SignedJWT signedJwt = SignedJWT.parse(jwtToken);
            JWSHeader jwsHeader = signedJwt.getHeader();

            // header must have algorithm("alg") and "kid"
            Preconditions.checkNotNull(jwsHeader.getAlgorithm());
            Preconditions.checkNotNull(jwsHeader.getKeyID());

            JWTClaimsSet claims = signedJwt.getJWTClaimsSet();

            // claims must have audience, issuer
            Preconditions.checkArgument(claims.getAudience().contains(expectedAudience));
            Preconditions.checkArgument(claims.getIssuer().equals(IAP_ISSUER_URL));

            // claim must have issued at time in the past
            Date currentTime = Date.from(Instant.now(clock));
            Preconditions.checkArgument(claims.getIssueTime().before(currentTime));
            // claim must have expiration time in the future
            Preconditions.checkArgument(claims.getExpirationTime().after(currentTime));

            // must have subject, email
            String email = claims.getClaim("email").toString();
            Preconditions.checkNotNull(claims.getSubject());
            Preconditions.checkNotNull(email);

            // verify using public key : lookup with key id, algorithm name provided
            ECPublicKey publicKey = getKey(jwsHeader.getKeyID(), jwsHeader.getAlgorithm().getName());

            Preconditions.checkNotNull(publicKey);
            JWSVerifier jwsVerifier = new ECDSAVerifier(publicKey);

            if (signedJwt.verify(jwsVerifier)) {
                return email;
            } else {
                throw new AuthenticationException();
            }
        } catch (IllegalArgumentException | ParseException | JOSEException ex) {

            throw new AuthenticationException(ex);
        }
    }
}
