/*
 *
 * Copyright 2017 Observational Health Data Sciences and Informatics
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Authors: Mikhail Mironov
 *
 */
package org.ohdsi.webapi;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.oauth2.sdk.pkce.CodeChallengeMethod;
import org.pac4j.oidc.config.OidcConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;


@Component
public class OidcConfCreator {

    private static final Logger logger = LoggerFactory.getLogger(OidcConfCreator.class);

    private volatile OidcConfiguration cachedConfiguration;
    private final Object lock = new Object();

    @Value("${security.auth.openId.clientId}")
    private String clientId;

    @Value("${security.auth.openId.apiSecret}")
    private String apiSecret;

    @Value("${security.auth.openId.url}")
    private String url;

    @Value("${security.auth.openId.externalUrl:}")
    private String externalUrl;

    @Value("${security.auth.openId.logoutUrl}")
    private String logoutUrl;

    @Value("${security.auth.openId.extraScopes}")
    private String extraScopes;

    @Value("#{${security.auth.openId.customParams:{T(java.util.Collections).emptyMap()}}}")
    private Map<String, String> customParams = new HashMap<>();

    @Value("${security.auth.oauth.callback.api}")
    private String oauthApiCallback;

    /**
     * Returns the external OIDC URL for browser-facing endpoints.
     * If externalUrl is set, returns it; otherwise returns the discovery URL.
     */
    public String getExternalUrl() {
        if (externalUrl != null && !externalUrl.isEmpty()) {
            return externalUrl;
        }
        // Fall back to discovery URL, removing the .well-known path if present
        if (url != null && url.contains("/.well-known/")) {
            return url.substring(0, url.indexOf("/.well-known/"));
        }
        return url;
    }

    public OidcConfiguration build() {
        OidcConfiguration cached = cachedConfiguration;
        if (cached != null) {
            return cached;
        }

        synchronized (lock) {
            cached = cachedConfiguration;
            if (cached != null) {
                return cached;
            }

            OidcConfiguration conf = new OidcConfiguration();
            conf.setClientId(clientId);
            conf.setSecret(apiSecret);
            conf.setDiscoveryURI(url);
            conf.setLogoutUrl(logoutUrl);
            conf.setWithState(true);
            conf.setUseNonce(true);

            if (customParams != null) {
                customParams.forEach(conf::addCustomParam);
            }

            String scopes = "openid";
            if (extraScopes != null && !extraScopes.isEmpty()) {
                scopes += " ";
                scopes += extraScopes;
            }
            conf.setScope(scopes);
            conf.setPreferredJwsAlgorithm(JWSAlgorithm.RS256);
            conf.setPkceMethod(CodeChallengeMethod.S256);

            try {
                logger.info("Initializing OIDC configuration with discovery URL: {}", url);
                conf.init();

                var resolver = conf.getOpMetadataResolver();
                if (resolver != null && resolver.load() != null) {
                    cachedConfiguration = conf;
                } else {
                    logger.error("OIDC metadata resolver returned null");
                }
            } catch (Exception e) {
                logger.error("Failed to initialize OIDC configuration", e);
            }

            return conf;
        }
    }

}
