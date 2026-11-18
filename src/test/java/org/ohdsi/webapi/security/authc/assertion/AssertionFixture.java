package org.ohdsi.webapi.security.authc.assertion;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.gen.ECKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.PlainJWT;
import com.nimbusds.jwt.SignedJWT;
import com.sun.net.httpserver.HttpServer;

/**
 * Test support: an in-memory ES256 key served as a JWKS from a real local HTTP
 * server, so the production decoder (including its JWKS fetch) is exercised.
 */
final class AssertionFixture implements AutoCloseable {

  static final String ISSUER = "https://sso.example.org/auth/";
  static final String AUDIENCE = "sso.example.org";
  static final String KID = "test-key";

  final ECKey key;
  private final HttpServer server;

  AssertionFixture() throws IOException, JOSEException {
    key = new ECKeyGenerator(Curve.P_256).keyID(KID).generate();
    byte[] jwks = new JWKSet(key.toPublicJWK()).toString().getBytes(StandardCharsets.UTF_8);
    server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    server.createContext("/jwks.json", exchange -> {
      exchange.getResponseHeaders().add("Content-Type", "application/json");
      exchange.sendResponseHeaders(200, jwks.length);
      try (OutputStream out = exchange.getResponseBody()) {
        out.write(jwks);
      }
    });
    server.start();
  }

  String jwksUri() {
    return "http://127.0.0.1:" + server.getAddress().getPort() + "/jwks.json";
  }

  AssertionAuthProperties props() {
    return props(jwksUri());
  }

  static AssertionAuthProperties props(String jwksUri) {
    AssertionAuthProperties p = new AssertionAuthProperties();
    p.setEnabled(true);
    p.setJwkSetUri(jwksUri);
    p.setIssuer(ISSUER);
    p.setAudience(AUDIENCE);
    return p;
  }

  AssertionVerifier verifier() {
    AssertionAuthProperties p = props();
    return new AssertionVerifier(AssertionAuthConfig.assertionJwtDecoder(p), p);
  }

  /** Claims as CHI-style SSO issues them: two minutes, sub + names. */
  static JWTClaimsSet.Builder claims(String sub) {
    Instant now = Instant.now();
    return new JWTClaimsSet.Builder()
        .issuer(ISSUER)
        .audience(AUDIENCE)
        .subject(sub)
        .claim("email", sub + "@example.org")
        .claim("given_name", "Ada")
        .claim("family_name", "Lovelace")
        .issueTime(Date.from(now))
        .expirationTime(Date.from(now.plusSeconds(120)));
  }

  String sign(JWTClaimsSet claims) throws JOSEException {
    return sign(key, claims);
  }

  static String sign(ECKey signingKey, JWTClaimsSet claims) throws JOSEException {
    SignedJWT jwt = new SignedJWT(
        new JWSHeader.Builder(JWSAlgorithm.ES256).keyID(signingKey.getKeyID()).build(), claims);
    jwt.sign(new ECDSASigner(signingKey));
    return jwt.serialize();
  }

  static String signHs256(JWTClaimsSet claims) throws JOSEException {
    SignedJWT jwt = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.HS256).keyID(KID).build(), claims);
    jwt.sign(new MACSigner("0123456789abcdef0123456789abcdef".getBytes(StandardCharsets.UTF_8)));
    return jwt.serialize();
  }

  static String unsigned(JWTClaimsSet claims) {
    return new PlainJWT(claims).serialize();
  }

  @Override
  public void close() {
    server.stop(0);
  }
}
