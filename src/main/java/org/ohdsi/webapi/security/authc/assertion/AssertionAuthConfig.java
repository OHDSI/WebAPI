package org.ohdsi.webapi.security.authc.assertion;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.ohdsi.webapi.security.authc.HttpSecurityShared;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtIssuerValidator;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.client.RestTemplate;

/**
 * Wires the trusted-assertion provider when {@code security.auth.assertion.enabled}
 * is true. See {@link AssertionAuthProperties}.
 */
@Configuration
@ConditionalOnProperty(prefix = "security.auth.assertion", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(AssertionAuthProperties.class)
public class AssertionAuthConfig {

  public static final String LOGIN_PATH = "/user/login/assertion";

  @Bean
  public AssertionVerifier assertionVerifier(AssertionAuthProperties props) {
    return new AssertionVerifier(assertionJwtDecoder(props), props);
  }

  /** Builds the decoder: JWKS-backed, algorithm-restricted, iss/aud/exp enforced. */
  static JwtDecoder assertionJwtDecoder(AssertionAuthProperties props) {
    require(props.getJwkSetUri(), "jwk-set-uri");
    require(props.getIssuer(), "issuer");
    require(props.getAudience(), "audience");
    if (props.getAlgorithms() == null || props.getAlgorithms().isEmpty()) {
      throw new IllegalStateException("security.auth.assertion.algorithms must not be empty");
    }

    List<SignatureAlgorithm> algorithms = new ArrayList<>();
    for (String name : props.getAlgorithms()) {
      SignatureAlgorithm alg = SignatureAlgorithm.from(name.trim());
      if (alg == null) {
        // SignatureAlgorithm has only asymmetric algorithms, so HS* lands here too.
        throw new IllegalStateException("security.auth.assertion.algorithms: unsupported " + name);
      }
      algorithms.add(alg);
    }

    // Without explicit timeouts a hung JWKS endpoint would hang every request.
    SimpleClientHttpRequestFactory http = new SimpleClientHttpRequestFactory();
    http.setConnectTimeout((int) props.getJwksTimeout().toMillis());
    http.setReadTimeout((int) props.getJwksTimeout().toMillis());

    NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(props.getJwkSetUri())
        .jwsAlgorithms(set -> {
          set.clear();
          set.addAll(algorithms);
        })
        .restOperations(new RestTemplate(http))
        .build();

    String audience = props.getAudience();
    List<OAuth2TokenValidator<Jwt>> validators = List.of(
        new JwtTimestampValidator(props.getClockSkew()),
        // JwtTimestampValidator only checks exp when present; an assertion without
        // one would never expire.
        new JwtClaimValidator<Object>(JwtClaimNames.EXP, Objects::nonNull),
        new JwtIssuerValidator(props.getIssuer()),
        new JwtClaimValidator<List<String>>(JwtClaimNames.AUD,
            aud -> aud != null && aud.contains(audience)));
    decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(validators));
    return decoder;
  }

  private static void require(String value, String name) {
    if (value == null || value.isBlank()) {
      throw new IllegalStateException("security.auth.assertion." + name + " is required when the provider is enabled");
    }
  }

  /**
   * The login endpoint must be reachable without a WebAPI token: it is how one is
   * obtained. The controller does its own verification.
   */
  @Bean
  @Order(2)
  public SecurityFilterChain assertionLoginChain(HttpSecurity http, HttpSecurityShared shared) throws Exception {
    shared.configureDefaults(http);
    http.securityMatcher(LOGIN_PATH)
        .authorizeHttpRequests(authz -> authz.anyRequest().permitAll());
    return http.build();
  }

  /**
   * Runs just after Spring Security, once the WebAPI token has been resolved, so
   * it can compare the token's user with the assertion's.
   */
  @Bean
  @ConditionalOnProperty(prefix = "security.auth.assertion", name = "enforce-on-every-request",
      havingValue = "true", matchIfMissing = true)
  public FilterRegistrationBean<AssertionBindingFilter> assertionBindingFilter(AssertionVerifier verifier) {
    FilterRegistrationBean<AssertionBindingFilter> registration =
        new FilterRegistrationBean<>(new AssertionBindingFilter(verifier));
    registration.setOrder(SecurityProperties.DEFAULT_FILTER_ORDER + 1);
    registration.addUrlPatterns("/*");
    return registration;
  }
}
