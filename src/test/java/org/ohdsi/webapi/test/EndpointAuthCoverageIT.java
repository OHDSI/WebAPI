package org.ohdsi.webapi.test;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 * Codebase-wide authorization guards driven by the live handler set, so future
 * endpoints are covered automatically:
 *  - every org.ohdsi.webapi endpoint must carry method security (@PreAuthorize on
 *    the handler or its controller), unless it is explicitly allow-listed as an
 *    intentionally-open endpoint (login/bootstrap, open listing/registry reads, or
 *    an endpoint whose authorization is enforced in-body);
 *  - every source-scoped handler (@PathVariable("sourceKey")) must carry @PreAuthorize.
 *
 * <p>These are static annotation-presence checks rather than HTTP probes: the
 * filter chain serves token-less requests as the anonymous principal, so
 * per-endpoint @PreAuthorize (or a deliberate allow-list entry) is what governs
 * access. Runtime behaviour for the anonymous principal is exercised by
 * {@link AnonymousAccessIT} and {@link SourceAccessIT}.
 */
@Ignore("Temporarily disabled while we determine a more robust way to determine which routes should be guarded")
public class EndpointAuthCoverageIT extends WebApiIT {

    // Reachable before login by design (login/bootstrap). Prefix match. Adding to
    // this list is a deliberate, reviewable change — keep it minimal.
    static final List<String> ANONYMOUS_ALLOW_LIST = List.of(
        "/info", "/auth/providers", "/user/login", "/user/refresh", "/user/logout", "/user/oauth/callback");

    // Intentionally open under the anonymous-principal model (EXACT match): listing /
    // registry / self-scoped reads. Their contents are filtered per-entity, or they are
    // public registries (users/roles/permissions) needed to grant access to others.
    // Opening an endpoint here is a deliberate, reviewable change — keep this tight.
    static final List<String> OPEN_READ_EXACT = List.of(
        "/cohortdefinition", "/conceptset",
        "/cohort-characterization", "/cohort-characterization/design",
        "/pathway-analysis", "/feature-analysis", "/ir", "/reusable",
        "/cohortdefinition/byTags", "/conceptset/byTags",
        "/cohort-characterization/byTags", "/pathway-analysis/byTags",
        "/source/sources",
        "/user/me",
        "/notifications", "/notifications/viewed");

    // Authorization enforced in-body (per-generation entity + source access via
    // checkGeneration*Access), so these carry no @PreAuthorize. Prefix match.
    static final List<String> IN_BODY_GUARDED_PREFIX = List.of(
        "/cohort-characterization/generation", "/pathway-analysis/generation");

    @Autowired
    @Qualifier("requestMappingHandlerMapping")
    private RequestMappingHandlerMapping handlerMapping;

    @Test
    public void everyEndpointRequiresAuthorizationOrIsAllowListed() {
        List<String> unguarded = new ArrayList<>();
        for (Map.Entry<RequestMappingInfo, HandlerMethod> e : handlerMapping.getHandlerMethods().entrySet()) {
            HandlerMethod hm = e.getValue();
            if (!hm.getBeanType().getName().startsWith("org.ohdsi.webapi")) {
                continue;
            }
            if (isGuarded(hm)) {
                continue;
            }
            for (String pattern : patternsOf(e.getKey())) {
                if (!isAllowListed(pattern)) {
                    unguarded.add(pickMethod(e.getKey()) + " " + pattern
                        + " (" + hm.getBeanType().getSimpleName() + "#" + hm.getMethod().getName() + ")");
                }
            }
        }
        assertTrue(
            "Endpoints without @PreAuthorize and not in the anonymous allow-list "
                + "(each must be method-secured or explicitly allow-listed):\n  "
                + String.join("\n  ", unguarded),
            unguarded.isEmpty());
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
            if (!isGuarded(hm)) {
                unguarded.add(hm.getBeanType().getSimpleName() + "#" + hm.getMethod().getName());
            }
        }
        assertTrue(
            "Source-scoped handlers (@PathVariable(\"sourceKey\")) missing @PreAuthorize:\n  "
                + String.join("\n  ", unguarded),
            unguarded.isEmpty());
    }

    private static boolean isGuarded(HandlerMethod hm) {
        return hm.getMethod().isAnnotationPresent(PreAuthorize.class)
            || hm.getBeanType().isAnnotationPresent(PreAuthorize.class);
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
        for (String p : OPEN_READ_EXACT) {
            if (pattern.equals(p)) {
                return true;
            }
        }
        for (String p : IN_BODY_GUARDED_PREFIX) {
            if (pattern.equals(p) || pattern.startsWith(p + "/")) {
                return true;
            }
        }
        return false;
    }

    static HttpMethod pickMethod(RequestMappingInfo info) {
        Set<RequestMethod> methods = info.getMethodsCondition().getMethods();
        if (methods.isEmpty()) {
            return HttpMethod.GET;
        }
        return HttpMethod.valueOf(methods.iterator().next().name());
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
