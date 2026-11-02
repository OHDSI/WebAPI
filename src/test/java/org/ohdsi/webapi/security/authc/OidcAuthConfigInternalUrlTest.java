package org.ohdsi.webapi.security.authc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.sun.net.httpserver.HttpServer;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * A provider whose issuer is a public address the server itself cannot resolve is
 * the normal case behind a gateway. These cover that shape: metadata is read over
 * the internal base, the endpoints the server calls follow it there, and both the
 * issuer and the endpoint a browser is sent to stay public.
 */
public class OidcAuthConfigInternalUrlTest {

  private static final String PUBLIC_ISSUER = "https://gateway.invalid:41100/trex/oidc";

  private HttpServer server;
  private String internalBase;

  @Before
  public void startProvider() throws Exception {
    server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    internalBase = "http://127.0.0.1:" + server.getAddress().getPort() + "/trex/oidc";
    server.createContext("/trex/oidc/.well-known/openid-configuration", exchange -> {
      byte[] body = ("{"
          + "\"issuer\":\"" + PUBLIC_ISSUER + "\","
          + "\"authorization_endpoint\":\"" + PUBLIC_ISSUER + "/authorize\","
          + "\"token_endpoint\":\"" + PUBLIC_ISSUER + "/token\","
          + "\"userinfo_endpoint\":\"" + PUBLIC_ISSUER + "/userinfo\","
          + "\"jwks_uri\":\"" + PUBLIC_ISSUER + "/.well-known/jwks.json\","
          + "\"end_session_endpoint\":\"" + PUBLIC_ISSUER + "/session/end\","
          + "\"response_types_supported\":[\"code\"],"
          + "\"grant_types_supported\":[\"authorization_code\"],"
          + "\"subject_types_supported\":[\"public\"],"
          + "\"id_token_signing_alg_values_supported\":[\"RS256\"]"
          + "}").getBytes(StandardCharsets.UTF_8);
      exchange.getResponseHeaders().add("Content-Type", "application/json");
      exchange.sendResponseHeaders(200, body.length);
      try (OutputStream out = exchange.getResponseBody()) {
        out.write(body);
      }
    });
    server.start();
  }

  @After
  public void stopProvider() {
    server.stop(0);
  }

  private ClientRegistration buildRegistration(String externalUrl) {
    OidcAuthConfig config = new OidcAuthConfig(null, null, null, null);
    ReflectionTestUtils.setField(config, "oidcRuntimeEnabled", true);
    ReflectionTestUtils.setField(config, "clientId", "d2e-webapi");
    ReflectionTestUtils.setField(config, "clientSecret", "secret");
    ReflectionTestUtils.setField(config, "callbackUi", "https://gateway.invalid:41100/atlas");
    ReflectionTestUtils.setField(config, "extraScopes", "");
    ReflectionTestUtils.setField(config, "discoveryOrIssuerUrl", PUBLIC_ISSUER);
    ReflectionTestUtils.setField(config, "internalUrl", internalBase);
    ReflectionTestUtils.setField(config, "externalUrl", externalUrl);

    ClientRegistrationRepository repository = config.oidcClientRegistrationRepository();
    ClientRegistration registration = repository.findByRegistrationId("openid");
    assertNotNull("registration was not built", registration);
    return registration;
  }

  @Test
  public void readsMetadataOverTheInternalBaseWithoutTouchingTheIssuerHost() {
    ClientRegistration registration = buildRegistration("");

    // The issuer is what tokens carry, so it has to survive verbatim even though
    // nothing was ever fetched from it.
    assertEquals(PUBLIC_ISSUER, registration.getProviderDetails().getIssuerUri());
  }

  @Test
  public void movesServerSideEndpointsOntoTheInternalBase() {
    ClientRegistration.ProviderDetails details = buildRegistration("").getProviderDetails();

    assertEquals(internalBase + "/token", details.getTokenUri());
    assertEquals(internalBase + "/.well-known/jwks.json", details.getJwkSetUri());
    assertEquals(internalBase + "/userinfo", details.getUserInfoEndpoint().getUri());
  }

  @Test
  public void keepsTheBrowserFacingEndpointPublic() {
    ClientRegistration.ProviderDetails details = buildRegistration("").getProviderDetails();

    assertTrue(
        "a browser must not be sent to an internal address, but got " + details.getAuthorizationUri(),
        details.getAuthorizationUri().startsWith(PUBLIC_ISSUER));
  }

  @Test
  public void leavesTheEndSessionEndpointPublic() {
    ClientRegistration.ProviderDetails details = buildRegistration("").getProviderDetails();

    // Logout is a browser redirect like authorization, so it must not be moved
    // onto an address only the server can reach.
    assertEquals(
        PUBLIC_ISSUER + "/session/end",
        details.getConfigurationMetadata().get("end_session_endpoint"));
  }

  @Test
  public void stillRewritesTheBrowserFacingEndpointOntoTheExternalUrl() {
    String externalUrl = "https://public.example/trex/oidc";

    ClientRegistration.ProviderDetails details = buildRegistration(externalUrl).getProviderDetails();

    assertEquals(externalUrl + "/authorize", details.getAuthorizationUri());
  }
}
