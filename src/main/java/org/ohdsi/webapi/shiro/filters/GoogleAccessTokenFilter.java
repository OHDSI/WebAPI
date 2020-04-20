package org.ohdsi.webapi.shiro.filters;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.ohdsi.webapi.shiro.TokenManager;
import org.ohdsi.webapi.shiro.tokens.JwtAuthToken;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

public class GoogleAccessTokenFilter extends AtlasAuthFilter {

    private static final String VALIDATE_URL = "https://oauth2.googleapis.com/tokeninfo?access_token=%s";

    private RestTemplate restTemplate;

    private ObjectMapper mapper = new ObjectMapper();

    public GoogleAccessTokenFilter(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
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

        if (TokenManager.extractToken(servletRequest) != null) {
            return executeLogin(servletRequest, servletResponse);
        }
        return false;
    }

    private String getTokenInfo(String token) throws IOException {

        String result = null;
        ResponseEntity<String> response = restTemplate.getForEntity(String.format(VALIDATE_URL, token), String.class);
        if (response.getStatusCode() == HttpStatus.OK) {
            JsonNode root =  mapper.readTree(response.getBody());
            result = getValueAsString(root, "email");
            if (Objects.isNull(result)) {
                result = getValueAsString(root, "aud");
            }
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
