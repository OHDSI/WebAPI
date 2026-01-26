package org.ohdsi.webapi.shiro.filters;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.ECDSAVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.shiro.authc.AuthenticationException;
import org.ohdsi.webapi.shiro.PermissionManager;
import org.ohdsi.webapi.shiro.ServletBridge;
import org.ohdsi.webapi.shiro.tokens.JwtAuthToken;
import org.pac4j.oidc.config.OidcConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.text.ParseException;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Validates OIDC JWT bearer tokens using the provider's JWKS.
 * Used for token exchange: external OIDC token -> WebAPI JWT.
 */
public class OidcJwtAuthFilter extends AtlasAuthFilter {

    private static final Logger logger = LoggerFactory.getLogger(OidcJwtAuthFilter.class);
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final long JWKS_CACHE_DURATION_MS = 300_000;

    public static final String OIDC_EXTERNAL_TOKEN = "oidc_external_token";

    private final OidcConfiguration oidcConfiguration;
    private final PermissionManager authorizer;
    private final Set<String> defaultRoles;
    private final Set<String> acceptedAudiences;
    private final Map<String, JWK> keyCache = new ConcurrentHashMap<>();
    private volatile long lastJwksFetch = 0;

    public OidcJwtAuthFilter(OidcConfiguration oidcConfiguration,
                            PermissionManager authorizer,
                            Set<String> defaultRoles) {
        this(oidcConfiguration, authorizer, defaultRoles, null);
    }

    public OidcJwtAuthFilter(OidcConfiguration oidcConfiguration,
                            PermissionManager authorizer,
                            Set<String> defaultRoles,
                            Set<String> additionalAudiences) {
        this.oidcConfiguration = oidcConfiguration;
        this.authorizer = authorizer;
        this.defaultRoles = defaultRoles;
        this.acceptedAudiences = new HashSet<>();
        if (oidcConfiguration.getClientId() != null) {
            this.acceptedAudiences.add(oidcConfiguration.getClientId());
        }
        if (additionalAudiences != null) {
            this.acceptedAudiences.addAll(additionalAudiences);
        }
        if (this.acceptedAudiences.isEmpty()) {
            throw new IllegalArgumentException("At least one accepted audience must be configured (clientId or apiResource)");
        }
        logger.info("OidcJwtAuthFilter initialized with accepted audiences: {}", this.acceptedAudiences);
    }

    @Override
    protected JwtAuthToken createToken(ServletRequest request, ServletResponse response) throws Exception {
        String bearerToken = extractBearerToken(request);
        if (bearerToken == null) {
            throw new AuthenticationException("No bearer token found");
        }
        return new JwtAuthToken(verifyAndExtractSubject(bearerToken));
    }

    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
        String bearerToken = extractBearerToken(request);
        if (bearerToken == null) {
            return true;
        }

        try {
            String subject = verifyAndExtractSubject(bearerToken);
            String name = extractName(bearerToken, subject);
            authorizer.registerUser(subject, name, defaultRoles);
            request.setAttribute(OIDC_EXTERNAL_TOKEN, true);
            return executeLogin(request, response);
        } catch (AuthenticationException e) {
            logger.warn("OIDC JWT authentication failed for request from {}: {}",
                    request.getRemoteAddr(), e.getMessage());
            return true;
        }
    }

    private String extractBearerToken(ServletRequest request) {
        HttpServletRequest httpRequest = ServletBridge.toHttp(request);
        String authHeader = httpRequest.getHeader(AUTHORIZATION_HEADER);
        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            return authHeader.substring(BEARER_PREFIX.length());
        }
        return null;
    }

    private String verifyAndExtractSubject(String jwtToken) throws AuthenticationException {
        try {
            SignedJWT signedJwt = SignedJWT.parse(jwtToken);
            JWSHeader header = signedJwt.getHeader();
            JWTClaimsSet claims = signedJwt.getJWTClaimsSet();

            Date now = new Date();
            Date expiration = claims.getExpirationTime();
            if (expiration != null && expiration.before(now)) {
                throw new AuthenticationException("Token expired");
            }

            Date notBefore = claims.getNotBeforeTime();
            if (notBefore != null && notBefore.after(now)) {
                throw new AuthenticationException("Token not yet valid");
            }

            String expectedIssuer = getExpectedIssuer();
            if (expectedIssuer != null && !expectedIssuer.equals(claims.getIssuer())) {
                throw new AuthenticationException("Invalid token issuer");
            }

            List<String> tokenAudiences = claims.getAudience();
            if (tokenAudiences != null && !tokenAudiences.isEmpty()) {
                boolean hasValidAudience = tokenAudiences.stream().anyMatch(acceptedAudiences::contains);
                if (!hasValidAudience) {
                    logger.warn("Token audience {} does not match any accepted audiences {}",
                        tokenAudiences, acceptedAudiences);
                    throw new AuthenticationException("Invalid token audience");
                }
            }

            JWK jwk = getKey(header.getKeyID());
            if (jwk == null) {
                throw new AuthenticationException("Signing key not found");
            }

            if (!signedJwt.verify(createVerifier(jwk))) {
                throw new AuthenticationException("Invalid signature");
            }

            String email = (String) claims.getClaim("email");
            return (email != null && !email.isEmpty()) ? email : claims.getSubject();

        } catch (ParseException | JOSEException e) {
            throw new AuthenticationException("JWT validation failed: " + e.getMessage(), e);
        }
    }

    private String extractName(String jwtToken, String fallback) {
        try {
            SignedJWT signedJwt = SignedJWT.parse(jwtToken);
            String name = (String) signedJwt.getJWTClaimsSet().getClaim("name");
            return (name != null && !name.isEmpty()) ? name : fallback;
        } catch (ParseException e) {
            return fallback;
        }
    }

    private String getExpectedIssuer() {
        try {
            var resolver = oidcConfiguration.getOpMetadataResolver();
            if (resolver != null) {
                var metadata = resolver.load();
                if (metadata != null) {
                    return metadata.getIssuer().getValue();
                }
            }
        } catch (Exception e) {
            logger.warn("Failed to get OIDC issuer: {}", e.getMessage());
        }
        return null;
    }

    private JWK getKey(String kid) {
        JWK jwk = keyCache.get(kid);
        if (jwk == null) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastJwksFetch > JWKS_CACHE_DURATION_MS) {
                synchronized (this) {
                    if (currentTime - lastJwksFetch > JWKS_CACHE_DURATION_MS) {
                        refreshJwks();
                    }
                }
                jwk = keyCache.get(kid);
            }
        }
        return jwk;
    }

    private void refreshJwks() {
        try {
            URI jwksUri = getJwksUri();
            if (jwksUri == null) {
                logger.error("No JWKS URI available");
                return;
            }

            JWKSet jwkSet = JWKSet.load(jwksUri.toURL());
            keyCache.clear();
            for (JWK key : jwkSet.getKeys()) {
                if (key.getKeyID() != null) {
                    keyCache.put(key.getKeyID(), key);
                }
            }
        } catch (Exception e) {
            logger.error("Failed to fetch JWKS: {}", e.getMessage());
        } finally {
            lastJwksFetch = System.currentTimeMillis();
        }
    }

    private URI getJwksUri() {
        try {
            var resolver = oidcConfiguration.getOpMetadataResolver();
            if (resolver != null) {
                var metadata = resolver.load();
                if (metadata != null) {
                    return metadata.getJWKSetURI();
                }
            }
        } catch (Exception e) {
            logger.warn("Failed to get JWKS URI: {}", e.getMessage());
        }
        return null;
    }

    private JWSVerifier createVerifier(JWK jwk) throws JOSEException {
        if (jwk instanceof ECKey) {
            return new ECDSAVerifier(((ECKey) jwk).toECPublicKey());
        } else if (jwk instanceof RSAKey) {
            return new RSASSAVerifier(((RSAKey) jwk).toRSAPublicKey());
        }
        throw new JOSEException("Unsupported key type: " + jwk.getKeyType());
    }
}
