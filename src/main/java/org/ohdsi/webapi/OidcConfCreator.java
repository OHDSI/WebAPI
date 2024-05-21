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
import org.pac4j.oidc.config.OidcConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;


@Component
public class OidcConfCreator {

    @Value("${security.oid.clientId}")
    private String clientId;

    @Value("${security.oid.apiSecret}")
    private String apiSecret;

    @Value("${security.oid.url}")
    private String url;
    
    @Value("${security.oid.logoutUrl}")
    private String logoutUrl;

    @Value("${security.oid.extraScopes}")
    private String extraScopes;

    @Value("#{${security.oid.customParams:{T(java.util.Collections).emptyMap()}}}")
    private Map<String, String> customParams = new HashMap<>();
    
    @Value("${security.oauth.callback.api}")
    private String oauthApiCallback;

    public OidcConfiguration build() {
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
        if (extraScopes != null && !extraScopes.isEmpty()){
            scopes += " ";
            scopes += extraScopes;
        }
        conf.setScope(scopes);
        conf.setPreferredJwsAlgorithm(JWSAlgorithm.RS256);
        return conf;
    }

}
