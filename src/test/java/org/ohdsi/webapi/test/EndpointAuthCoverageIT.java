package org.ohdsi.webapi.test;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 * Codebase-wide guards: no org.ohdsi.webapi endpoint may be reachable without
 * authentication (except a pinned allow-list), and every source-scoped handler
 * must carry @PreAuthorize. Driven by the live handler set, so future endpoints
 * are covered automatically.
 */
public class EndpointAuthCoverageIT extends WebApiIT {

    // Reachable before login by design. Adding to this list is a deliberate,
    // reviewable change — keep it minimal.
    static final List<String> ANONYMOUS_ALLOW_LIST = List.of(
        "/info", "/auth/providers", "/i18n", "/user/login", "/user/oauth/callback");

    @Autowired
    @Qualifier("requestMappingHandlerMapping")
    private RequestMappingHandlerMapping handlerMapping;

    private final TestRestTemplate anonymous = new TestRestTemplate();

    @Test
    public void everyEndpointRejectsAnonymousExceptAllowList() {
        List<String> leaks = new ArrayList<>();
        for (Map.Entry<RequestMappingInfo, HandlerMethod> e : handlerMapping.getHandlerMethods().entrySet()) {
            HandlerMethod hm = e.getValue();
            if (!hm.getBeanType().getName().startsWith("org.ohdsi.webapi")) {
                continue;
            }
            HttpMethod method = pickMethod(e.getKey());
            for (String pattern : patternsOf(e.getKey())) {
                if (isAllowListed(pattern)) {
                    continue;
                }
                String url = getBaseUri() + substitutePathVars(pattern);
                ResponseEntity<String> resp = anonymous.exchange(
                    url, method, new HttpEntity<>(jsonHeaders()), String.class);
                int sc = resp.getStatusCode().value();
                if (sc != 401 && sc != 403) {
                    leaks.add(method + " " + pattern + " -> " + sc);
                }
            }
        }
        assertTrue(
            "Endpoints reachable without authentication (expected 401/403):\n  "
                + String.join("\n  ", leaks),
            leaks.isEmpty());
    }

    static Set<String> patternsOf(RequestMappingInfo info) {
        if (info.getPathPatternsCondition() != null) {
            return info.getPathPatternsCondition().getPatternValues();
        }
        return info.getPatternsCondition().getPatterns();
    }

    static boolean isAllowListed(String pattern) {
        for (String p : ANONYMOUS_ALLOW_LIST) {
            if (pattern.equals(p) || pattern.startsWith(p + "/")) {
                return true;
            }
        }
        return false;
    }

    static String substitutePathVars(String pattern) {
        return pattern.replaceAll("\\{[^/}]+\\}", "x").replace("/**", "/x").replace("/*", "/x");
    }

    static HttpMethod pickMethod(RequestMappingInfo info) {
        Set<RequestMethod> methods = info.getMethodsCondition().getMethods();
        if (methods.isEmpty()) {
            return HttpMethod.GET;
        }
        return HttpMethod.valueOf(methods.iterator().next().name());
    }

    static HttpHeaders jsonHeaders() {
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        return h;
    }

    @Test
    public void everySourceScopedHandlerHasAuthorization() {
        List<String> unguarded = new ArrayList<>();
        for (HandlerMethod hm : handlerMapping.getHandlerMethods().values()) {
            if (!hm.getBeanType().getName().startsWith("org.ohdsi.webapi")) {
                continue;
            }
            if (!hasSourceKeyPathVariable(hm)) {
                continue;
            }
            boolean guarded = hm.getMethod().isAnnotationPresent(PreAuthorize.class)
                || hm.getBeanType().isAnnotationPresent(PreAuthorize.class);
            if (!guarded) {
                unguarded.add(hm.getBeanType().getSimpleName() + "#" + hm.getMethod().getName());
            }
        }
        assertTrue(
            "Source-scoped handlers (@PathVariable(\"sourceKey\")) missing @PreAuthorize:\n  "
                + String.join("\n  ", unguarded),
            unguarded.isEmpty());
    }

    private static boolean hasSourceKeyPathVariable(HandlerMethod hm) {
        for (MethodParameter p : hm.getMethodParameters()) {
            PathVariable pv = p.getParameterAnnotation(PathVariable.class);
            if (pv != null && ("sourceKey".equals(pv.value()) || "sourceKey".equals(pv.name()))) {
                return true;
            }
        }
        return false;
    }
}
