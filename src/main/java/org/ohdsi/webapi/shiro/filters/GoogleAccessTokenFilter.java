package org.ohdsi.webapi.shiro.filters;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.subject.PrincipalCollection;
import org.ohdsi.webapi.shiro.PermissionManager;
import org.ohdsi.webapi.shiro.TokenManager;
import org.ohdsi.webapi.shiro.tokens.JwtAuthToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class GoogleAccessTokenFilter extends AtlasAuthFilter {

    private static final String VALIDATE_URL = "https://oauth2.googleapis.com/tokeninfo?access_token=%s";

    private static final Logger logger = LoggerFactory.getLogger(GoogleAccessTokenFilter.class);

    private RestTemplate restTemplate;

    private ObjectMapper mapper = new ObjectMapper();

    private PermissionManager authorizer;

    private Set<String> defaultRoles;

    public GoogleAccessTokenFilter(RestTemplate restTemplate,
                                   PermissionManager authorizer,
                                   Set<String> roles) {
        this.restTemplate = restTemplate;
        this.authorizer = authorizer;
        this.defaultRoles = roles;
    }

    @Override
    protected AuthenticationToken createToken(ServletRequest servletRequest, ServletResponse servletResponse) throws Exception {

        String token = TokenManager.extractToken(servletRequest);
        String userId = getTokenInfo(token);
        return Optional.ofNullable(userId).map(JwtAuthToken::new)
                .orElseThrow(AuthenticationException::new);
    }

    @Override
    protected boolean onAccessDenied(ServletRequest servletRequest, ServletResponse servletResponse) throws Exception {

        try {
            if (TokenManager.extractToken(servletRequest) != null) {
                boolean loggedIn = executeLogin(servletRequest, servletResponse);
                if (loggedIn) {
                    final PrincipalCollection principals = SecurityUtils.getSubject().getPrincipals();
                    String name = (String) principals.getPrimaryPrincipal();
                    this.authorizer.registerUser(name, name, defaultRoles);
                }
            }
        } catch (AuthenticationException ignored) {
        }
        return true;
    }

    private String getTokenInfo(String token) throws IOException {

        String result = null;
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(String.format(VALIDATE_URL, token), String.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode root = mapper.readTree(response.getBody());
                result = getValueAsString(root, "email");
                if (Objects.isNull(result)) {
                    result = getValueAsString(root, "aud");
                }
            }
        } catch (HttpClientErrorException e) {
            logger.warn("Access token is invalid {}", e.getMessage());
        }
        return result;
    }

    private String getValueAsString(JsonNode node, String property) {
        JsonNode valueNode = node.get(property);
        if (!valueNode.isNull()) {
            return valueNode.textValue();
        }
        return null;
    }
}
