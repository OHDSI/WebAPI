package org.ohdsi.webapi.security.authc.assertion;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.time.Instant;
import java.util.Date;
import java.util.List;

import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.gen.ECKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

public class AssertionVerifierTest {

  private AssertionFixture fixture;
  private AssertionVerifier verifier;

  @Before
  public void setUp() throws Exception {
    fixture = new AssertionFixture();
    verifier = fixture.verifier();
  }

  @After
  public void tearDown() {
    fixture.close();
  }

  private AssertionVerifier.Outcome verify(String token) {
    MockHttpServletRequest request = new MockHttpServletRequest();
    if (token != null) {
      request.addHeader("sso-assertion", token);
    }
    return verifier.verify(request);
  }

  private static void assertInvalid(AssertionVerifier.Outcome outcome) {
    assertTrue("expected Invalid, got " + outcome, outcome instanceof AssertionVerifier.Invalid);
  }

  @Test
  public void validAssertionIsVerified() throws Exception {
    AssertionVerifier.Outcome outcome = verify(fixture.sign(AssertionFixture.claims("Ada").build()));

    assertTrue(outcome instanceof AssertionVerifier.Verified);
    AssertionVerifier.Verified v = (AssertionVerifier.Verified) outcome;
    assertEquals("login is lower-cased as WebAPI stores it", "ada", v.login());
    assertEquals("Ada Lovelace", v.name());
  }

  @Test
  public void nameFallsBackToLogin() throws Exception {
    JWTClaimsSet claims = AssertionFixture.claims("ada").claim("given_name", null).claim("family_name", null).build();
    AssertionVerifier.Verified v = (AssertionVerifier.Verified) verify(fixture.sign(claims));
    assertEquals("ada", v.name());
  }

  @Test
  public void missingHeaderIsMissing() {
    assertTrue(verify(null) instanceof AssertionVerifier.Missing);
    assertTrue(verify("  ") instanceof AssertionVerifier.Missing);
  }

  @Test
  public void expiredAssertionIsRefused() throws Exception {
    Instant past = Instant.now().minusSeconds(600);
    JWTClaimsSet claims = AssertionFixture.claims("ada")
        .issueTime(Date.from(past)).expirationTime(Date.from(past.plusSeconds(120))).build();
    assertInvalid(verify(fixture.sign(claims)));
  }

  @Test
  public void assertionWithoutExpiryIsRefused() throws Exception {
    assertInvalid(verify(fixture.sign(AssertionFixture.claims("ada").expirationTime(null).build())));
  }

  @Test
  public void wrongAudienceIsRefused() throws Exception {
    assertInvalid(verify(fixture.sign(AssertionFixture.claims("ada").audience("other.example.org").build())));
  }

  @Test
  public void missingAudienceIsRefused() throws Exception {
    assertInvalid(verify(fixture.sign(AssertionFixture.claims("ada").audience((List<String>) null).build())));
  }

  @Test
  public void wrongIssuerIsRefused() throws Exception {
    assertInvalid(verify(fixture.sign(AssertionFixture.claims("ada").issuer("https://evil.example.org/auth/").build())));
  }

  @Test
  public void missingSubjectIsRefused() throws Exception {
    assertInvalid(verify(fixture.sign(AssertionFixture.claims("ada").subject(null).build())));
  }

  @Test
  public void signatureFromAnotherKeyIsRefused() throws Exception {
    ECKey impostor = new ECKeyGenerator(Curve.P_256).keyID(AssertionFixture.KID).generate();
    assertInvalid(verify(AssertionFixture.sign(impostor, AssertionFixture.claims("ada").build())));
  }

  @Test
  public void unknownKeyIdIsRefused() throws Exception {
    ECKey other = new ECKeyGenerator(Curve.P_256).keyID("unknown").generate();
    assertInvalid(verify(AssertionFixture.sign(other, AssertionFixture.claims("ada").build())));
  }

  @Test
  public void hs256AssertionIsRefused() throws Exception {
    assertInvalid(verify(AssertionFixture.signHs256(AssertionFixture.claims("ada").build())));
  }

  @Test
  public void unsignedAssertionIsRefused() {
    assertInvalid(verify(AssertionFixture.unsigned(AssertionFixture.claims("ada").build())));
  }

  @Test
  public void garbageIsRefused() {
    assertInvalid(verify("not-a-jwt"));
  }

  @Test
  public void unreachableJwksIsUnavailableNotInvalid() throws Exception {
    String token = fixture.sign(AssertionFixture.claims("ada").build());
    String uri = fixture.jwksUri();
    fixture.close();

    AssertionAuthProperties p = AssertionFixture.props(uri);
    AssertionVerifier down = new AssertionVerifier(AssertionAuthConfig.assertionJwtDecoder(p), p);
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader("sso-assertion", token);

    AssertionVerifier.Outcome outcome = down.verify(request);
    assertTrue("expected Unavailable, got " + outcome, outcome instanceof AssertionVerifier.Unavailable);
  }

  @Test
  public void configuredHeaderNameIsUsed() throws Exception {
    AssertionAuthProperties p = fixture.props();
    p.setHeader("x-identity");
    AssertionVerifier custom = new AssertionVerifier(AssertionAuthConfig.assertionJwtDecoder(p), p);
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader("x-identity", fixture.sign(AssertionFixture.claims("ada").build()));
    assertTrue(custom.verify(request) instanceof AssertionVerifier.Verified);
  }

  @Test
  public void missingRequiredSettingsFailAtStartup() {
    AssertionAuthProperties p = fixture.props();
    p.setAudience("");
    assertThrows(IllegalStateException.class, () -> AssertionAuthConfig.assertionJwtDecoder(p));
  }

  @Test
  public void symmetricAlgorithmCannotBeConfigured() {
    AssertionAuthProperties p = fixture.props();
    p.setAlgorithms(List.of("HS256"));
    assertThrows(IllegalStateException.class, () -> AssertionAuthConfig.assertionJwtDecoder(p));
  }
}
