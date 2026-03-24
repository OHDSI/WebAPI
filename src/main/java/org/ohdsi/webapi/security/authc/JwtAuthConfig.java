package org.ohdsi.webapi.security.authc;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Locale;

import jakarta.annotation.PostConstruct;

import org.ohdsi.webapi.security.authz.UserEntity;
import org.ohdsi.webapi.security.authz.UserRepository;
import org.ohdsi.webapi.security.identity.WebApiPrincipal;
import org.ohdsi.webapi.security.session.SessionService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.core.convert.converter.Converter;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jose.jws.JwsAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jose.util.DefaultResourceRetriever;
import com.nimbusds.jose.util.ResourceRetriever;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.JWSAlgorithm;
import java.net.URL;
import java.net.MalformedURLException;
import org.springframework.context.annotation.Primary;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfigurationSource;
import com.nimbusds.jose.jwk.source.ImmutableSecret;

@Configuration
@EnableScheduling
public class JwtAuthConfig {

  // Default algorithm selection; actual encoder/decoder beans are conditional
  public static final MacAlgorithm DEFAULT_HS_ALGORITHM = MacAlgorithm.HS256;
  private final SessionService sessionService;
  private final UserRepository userRepository;

  @Value("${security.provider:DisabledSecurity}")
  private String securityProvider;

  @Value("${security.jwt.algorithm:HS256}")
  private String configuredAlgorithm;

  @Value("${security.jwt.secret:}")
  private String configuredSecret;

  @Value("${security.jwt.rsa.private-key-path:}")
  private String rsaPrivateKeyPath;

  @Value("${security.jwt.rsa.public-key-path:}")
  private String rsaPublicKeyPath;

  @Value("${security.jwt.kid:}")
  private String configuredKid;

  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JwtAuthConfig.class);

  @PostConstruct
  void validateConfiguration() {
    if ("DisabledSecurity".equals(securityProvider)) {
      return; // Skip JWT validation when security is disabled
    }
    if ("HS256".equalsIgnoreCase(configuredAlgorithm) || configuredAlgorithm == null) {
      if (configuredSecret == null || configuredSecret.isBlank()) {
        throw new IllegalStateException("security.jwt.secret must be set for HS256 algorithm");
      }
      if ("super-secret-key-super-secret-key".equals(configuredSecret)) {
        log.warn("Using default JWT secret — change security.jwt.secret before deploying to production");
      }
    }
  }

  // Constructor now injects both session store and user repository
  public JwtAuthConfig(SessionService sessionService, UserRepository userRepository) {
    this.sessionService = sessionService;
    this.userRepository = userRepository;
  }

  /**
   * Secret key used for HS256 signing. Read from configuration when
   * `security.jwt.algorithm=HS256` (default).
   */
  @Bean
  @ConditionalOnProperty(prefix = "security.jwt", name = "algorithm", havingValue = "HS256", matchIfMissing = true)
  public SecretKey jwtSecretKey() {
    return new SecretKeySpec(
        configuredSecret.getBytes(StandardCharsets.UTF_8),
        DEFAULT_HS_ALGORITHM.getName() // maps to HmacSHA256
    );
  }

  /**
   * Determine the JWS algorithm instance from configured algorithm name.
   */
  @Bean
  public JwsAlgorithm jwsAlgorithm() {
    String alg = configuredAlgorithm == null ? "HS256" : configuredAlgorithm.trim().toUpperCase(Locale.ROOT);
    if ("RS256".equals(alg)) {
      return SignatureAlgorithm.RS256;
    }
    return DEFAULT_HS_ALGORITHM;
  }

  /**
   * JWT encoder for HS256 using a symmetric secret.
   */
  @Bean
  @ConditionalOnProperty(prefix = "security.jwt", name = "algorithm", havingValue = "HS256", matchIfMissing = true)
  public JwtEncoder jwtEncoder(SecretKey secretKey) {
    return new NimbusJwtEncoder(new ImmutableSecret<>(secretKey));
  }

  /**
   * JWT decoder for HS256 using a symmetric secret.
   */
  @Bean
  @ConditionalOnProperty(prefix = "security.jwt", name = "algorithm", havingValue = "HS256", matchIfMissing = true)
  public JwtDecoder jwtDecoder(SecretKey secretKey) {
    return NimbusJwtDecoder
        .withSecretKey(secretKey)
        .macAlgorithm(DEFAULT_HS_ALGORITHM)
        .build();
  }

  // --- RS256 beans ---
  /**
   * JWT encoder for RS256 using an RSA private key loaded from the
   * `security.jwt.rsa.private-key-path` PEM file. Produced JWK will include
   * the configured `kid` if present.
   */
  @Bean
  @ConditionalOnProperty(prefix = "security.jwt", name = "algorithm", havingValue = "RS256")
  public JwtEncoder jwtEncoderRs() {
    try {
      RSAPrivateKey privateKey = loadPrivateKey(rsaPrivateKeyPath);
      RSAPublicKey publicKey = loadPublicKey(rsaPublicKeyPath);
      RSAKey jwk = new RSAKey.Builder(publicKey).privateKey(privateKey).keyID(configuredKid == null || configuredKid.isBlank() ? UUID.randomUUID().toString() : configuredKid).build();
      JWKSet jwkSet = new JWKSet(jwk);
      JWKSource<SecurityContext> jwkSource = new ImmutableJWKSet<>(jwkSet);
      return new NimbusJwtEncoder(jwkSource);
    } catch (Exception e) {
      throw new IllegalStateException("Failed to initialize RS256 JwtEncoder", e);
    }
  }

  /**
   * JWT decoder for RS256 using an RSA public key loaded from the
   * `security.jwt.rsa.public-key-path` PEM file.
   */
  @Bean
  @ConditionalOnProperty(prefix = "security.jwt", name = "algorithm", havingValue = "RS256")
  public JwtDecoder jwtDecoderRs() {
    try {
      RSAPublicKey publicKey = loadPublicKey(rsaPublicKeyPath);
      return NimbusJwtDecoder.withPublicKey(publicKey).build();
    } catch (Exception e) {
      throw new IllegalStateException("Failed to initialize RS256 JwtDecoder", e);
    }
  }

  /**
   * Tunable JWKs-backed JwtDecoder (production-ready). If
   * `security.jwt.jwk-set-uri` is configured, this bean will create a
   * `RemoteJWKSet` with a `DefaultResourceRetriever` (tunable timeouts and
   * size) and a `DefaultJWTProcessor` that selects keys by `kid`.
   */
  @Bean
  @Primary
  @ConditionalOnProperty(prefix = "security.jwt", name = "algorithm", havingValue = "RS256")
  @ConditionalOnProperty(prefix = "security.jwt", name = "jwk-set-uri")
  public JwtDecoder jwtDecoderFromJwkSet(@Value("${security.jwt.jwk-set-uri}") String jwkSetUri) {
    try {
      // connect timeout ms, read timeout ms, size limit
      ResourceRetriever resourceRetriever = new DefaultResourceRetriever(2000, 2000, 1024 * 1024);
      RemoteJWKSet<SecurityContext> remoteJWKSet = new RemoteJWKSet<>(new URL(jwkSetUri), resourceRetriever);

      ConfigurableJWTProcessor<SecurityContext> jwtProcessor = new DefaultJWTProcessor<>();
      JWSKeySelector<SecurityContext> keySelector = new JWSVerificationKeySelector<>(JWSAlgorithm.RS256, remoteJWKSet);
      jwtProcessor.setJWSKeySelector(keySelector);

      return new NimbusJwtDecoder(jwtProcessor);
    } catch (MalformedURLException e) {
      throw new IllegalStateException("Invalid security.jwt.jwk-set-uri: " + jwkSetUri, e);
    }
  }

  private RSAPrivateKey loadPrivateKey(String path) throws IOException, NoSuchAlgorithmException, java.security.spec.InvalidKeySpecException {
    if (path == null || path.isBlank()) {
      throw new IllegalArgumentException("RSA private key path not configured (security.jwt.rsa.private-key-path)");
    }
    String pem = Files.readString(Path.of(path));
    String base64 = pem.replaceAll("-----BEGIN (.*)-----", "").replaceAll("-----END (.*)-----", "").replaceAll("\\s", "");
    byte[] der = Base64.getDecoder().decode(base64);
    PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(der);
    KeyFactory kf = KeyFactory.getInstance("RSA");
    return (RSAPrivateKey) kf.generatePrivate(spec);
  }

  private RSAPublicKey loadPublicKey(String path) throws IOException, NoSuchAlgorithmException, java.security.spec.InvalidKeySpecException {
    if (path == null || path.isBlank()) {
      throw new IllegalArgumentException("RSA public key path not configured (security.jwt.rsa.public-key-path)");
    }
    String pem = Files.readString(Path.of(path));
    String base64 = pem.replaceAll("-----BEGIN (.*)-----", "").replaceAll("-----END (.*)-----", "").replaceAll("\\s", "");
    byte[] der = Base64.getDecoder().decode(base64);
    X509EncodedKeySpec spec = new X509EncodedKeySpec(der);
    KeyFactory kf = KeyFactory.getInstance("RSA");
    return (RSAPublicKey) kf.generatePublic(spec);
  }

  @Bean
  @Order(100)
  public SecurityFilterChain apiChain(HttpSecurity http,
      CorsConfigurationSource corsConfigurationSource) throws Exception {

    boolean securityEnabled = !"DisabledSecurity".equals(securityProvider);

    http
        .csrf(AbstractHttpConfigurer::disable)
        .cors(cors -> cors.configurationSource(corsConfigurationSource))
        // Disable unneeded filters
        .requestCache(AbstractHttpConfigurer::disable)
        .sessionManagement(AbstractHttpConfigurer::disable)
        .logout(AbstractHttpConfigurer::disable)
        .formLogin(AbstractHttpConfigurer::disable)
        .httpBasic(AbstractHttpConfigurer::disable);

    if (securityEnabled) {
      // Public endpoints that don't require authentication
      http.authorizeHttpRequests(auth -> auth
              .requestMatchers("/info", "/auth/**", "/user/login/**", "/user/oauth/**",
                               "/.well-known/**", "/actuator/**").permitAll()
              .anyRequest().authenticated())
          // Return 401 JSON for unauthenticated requests
          .exceptionHandling(ex -> ex.authenticationEntryPoint((req, resp, authEx) -> {
              resp.setStatus(jakarta.servlet.http.HttpServletResponse.SC_UNAUTHORIZED);
              resp.setContentType("application/json");
              resp.getWriter().write("{\"message\":\"Unauthorized\"}");
          }))
          // Configure JWT authentication
          .oauth2ResourceServer(oauth -> oauth
              .authenticationEntryPoint((req, resp, authEx) -> {
                  resp.setStatus(jakarta.servlet.http.HttpServletResponse.SC_UNAUTHORIZED);
                  resp.setContentType("application/json");
                  resp.getWriter().write("{\"message\":\"Unauthorized\"}");
              })
              .jwt(jwt -> jwt.jwtAuthenticationConverter(
                  new JwtToWebApiAuthenticationConverter(sessionService, userRepository))));
    } else {
      // DisabledSecurity: permit all requests, no JWT validation
      http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
    }

    // Fallback to anonymous if JWT not present
    http.anonymous(anon -> anon
            .principal(WebApiPrincipal.ANONYMOUS)
            .authorities("ROLE_ANONYMOUS"));

    return http.build();
  }

  /**
   * Converts a decoded JWT into a WebApiAuthenticationToken while performing
   * session validation.
   */
  private static class JwtToWebApiAuthenticationConverter
      implements Converter<Jwt, AbstractAuthenticationToken> {

    private final SessionService userSessionStore;
    private final UserRepository userRepository;

    // Constructor now takes both dependencies
    public JwtToWebApiAuthenticationConverter(SessionService userSessionStore,
        UserRepository userRepository) {
      this.userSessionStore = Objects.requireNonNull(userSessionStore, "userSessionStore");
      this.userRepository = Objects.requireNonNull(userRepository, "userRepository");
    }

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
      String login = jwt.getSubject();
      String sessionId = jwt.getClaimAsString("sid");

      // Validate session (single-login / multi-login policy)
      UUID sessionUuid;
      try {
        sessionUuid = UUID.fromString(sessionId);
      } catch (IllegalArgumentException e) {
        throw new BadCredentialsException("Invalid session ID in JWT", e);
      }

      if (!userSessionStore.isSessionValid(login, sessionUuid)) {
        throw new BadCredentialsException("Session invalid or revoked");
      }

      // Lookup the user in your system
      UserEntity user = userRepository.findByLogin(login).orElseThrow(() -> new BadCredentialsException("User not found: %s".formatted(login)));

      // Build principal and authentication token
      WebApiPrincipal principal = new WebApiPrincipal(user.getId(), user.getLogin());

      Collection<GrantedAuthority> authorities = List.of(); // populate as needed

      return WebApiAuthenticationToken.authenticated(principal, sessionUuid, authorities);
    }
  }
}