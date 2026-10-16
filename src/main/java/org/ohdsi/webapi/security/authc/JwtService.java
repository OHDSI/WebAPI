package org.ohdsi.webapi.security.authc;

import java.time.Instant;
import java.util.Date;

import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jose.jws.JwsAlgorithm;
import org.ohdsi.webapi.Constants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtService {

    private final JwtEncoder jwtEncoder;
    private final JwsAlgorithm jwsAlgorithm;
    private final String kid;

    public JwtService(JwtEncoder jwtEncoder,
                      JwsAlgorithm jwsAlgorithm,
                      @Value("${security.jwt.kid:}") String kid) {
        this.jwtEncoder = jwtEncoder;
        this.jwsAlgorithm = jwsAlgorithm;
        this.kid = kid == null ? "" : kid;
    }

    public String generateToken(String login, String sessionId, Date expiresAt) {

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(login)
                .issuedAt(Instant.now())
                .expiresAt(expiresAt.toInstant())
                .claim(Constants.SESSION_ID, sessionId)
                .build();

        JwsHeader.Builder headerBuilder = JwsHeader.with(jwsAlgorithm);
        if (kid != null && !kid.isBlank()) {
            headerBuilder = headerBuilder.keyId(kid);
        }
        JwsHeader header = headerBuilder.build();

        return jwtEncoder
                .encode(JwtEncoderParameters.from(header, claims))
                .getTokenValue();
    }
}