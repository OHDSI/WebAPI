/*
 * Copyright 2024 Observational Health Data Sciences and Informatics [OHDSI.org].
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ohdsi.webapi.auth;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.List;

/**
 * Service that exposes available authentication providers for Atlas frontend.
 */
@Path("/auth")
@Controller
public class AuthProviderService {

    @Value("${security.auth.jdbc.enabled}")
    private boolean jdbcAuthEnabled;

    @Value("${security.auth.windows.enabled}")
    private boolean windowsAuthEnabled;

    @Value("${security.auth.kerberos.enabled}")
    private boolean kerberosAuthEnabled;

    @Value("${security.auth.ldap.enabled}")
    private boolean ldapAuthEnabled;

    @Value("${security.auth.ad.enabled}")
    private boolean adAuthEnabled;

    @Value("${security.auth.cas.enabled}")
    private boolean casAuthEnabled;

    @Value("${security.auth.openid.enabled}")
    private boolean openidAuthEnabled;

    @Value("${security.auth.facebook.enabled}")
    private boolean facebookAuthEnabled;

    @Value("${security.auth.github.enabled}")
    private boolean githubAuthEnabled;

    @Value("${security.auth.google.enabled}")
    private boolean googleAuthEnabled;

    @Value("${security.auth.saml.enabled:false}")
    private boolean samlAuthEnabled;

    @Value("${security.oid.logoutUrl:}")
    private String oidcLogoutUrl;

    /**
     * Get the list of enabled authentication providers.
     * This endpoint is publicly accessible (no auth required).
     */
    @GET
    @Path("/providers")
    @Produces(MediaType.APPLICATION_JSON)
    public List<AuthProviderInfo> getProviders() {
        List<AuthProviderInfo> providers = new ArrayList<>();

        // OpenID Connect (e.g., Logto, Keycloak, Okta)
        if (openidAuthEnabled) {
            providers.add(new AuthProviderInfo(
                "OpenID",
                "user/login/openid",
                false,
                "mdi-shield-account",
                false,
                oidcLogoutUrl != null && !oidcLogoutUrl.isEmpty() ? oidcLogoutUrl : null
            ));
        }

        // Database authentication (JDBC)
        if (jdbcAuthEnabled) {
            providers.add(new AuthProviderInfo(
                "DB",
                "user/login/db",
                true,
                "mdi-database",
                true
            ));
        }

        // Windows authentication
        if (windowsAuthEnabled) {
            providers.add(new AuthProviderInfo(
                "Windows",
                "user/login/windows",
                true,
                "mdi-microsoft-windows",
                false
            ));
        }

        // Kerberos authentication
        if (kerberosAuthEnabled) {
            providers.add(new AuthProviderInfo(
                "Kerberos",
                "user/login/kerberos",
                true,
                "mdi-shield-key",
                true
            ));
        }

        // LDAP authentication
        if (ldapAuthEnabled) {
            providers.add(new AuthProviderInfo(
                "LDAP",
                "user/login/ldap",
                true,
                "mdi-folder-network",
                true
            ));
        }

        // Active Directory authentication
        if (adAuthEnabled) {
            providers.add(new AuthProviderInfo(
                "Active Directory",
                "user/login/ad",
                true,
                "mdi-microsoft",
                true
            ));
        }

        // CAS authentication
        if (casAuthEnabled) {
            providers.add(new AuthProviderInfo(
                "CAS",
                "user/login/cas",
                false,
                "mdi-account-key",
                false
            ));
        }

        // SAML authentication
        if (samlAuthEnabled) {
            providers.add(new AuthProviderInfo(
                "SAML",
                "user/login/saml",
                false,
                "mdi-security",
                false
            ));
        }

        // Google OAuth
        if (googleAuthEnabled) {
            providers.add(new AuthProviderInfo(
                "Google",
                "user/login/google",
                false,
                "mdi-google",
                false
            ));
        }

        // Facebook OAuth
        if (facebookAuthEnabled) {
            providers.add(new AuthProviderInfo(
                "Facebook",
                "user/login/facebook",
                false,
                "mdi-facebook",
                false
            ));
        }

        // GitHub OAuth
        if (githubAuthEnabled) {
            providers.add(new AuthProviderInfo(
                "GitHub",
                "user/login/github",
                false,
                "mdi-github",
                false
            ));
        }

        return providers;
    }
}
