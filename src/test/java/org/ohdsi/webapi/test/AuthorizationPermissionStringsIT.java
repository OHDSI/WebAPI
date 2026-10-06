package org.ohdsi.webapi.test;

import static org.junit.Assert.assertTrue;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 * Guards that every permission literal referenced in a {@code @PreAuthorize}
 * expression (e.g. {@code isPermitted('read:incidence')},
 * {@code anyOf('read:source','write:source')}) is an actual permission seeded in
 * {@code sec_permission}. A typo such as {@code read:cohortdefinition} (vs
 * {@code read:cohort-definition}) would otherwise silently deny everyone while
 * passing every other test.
 *
 * <p>Single-quoted string literals in these SpEL expressions are always
 * permission names — entity types ({@code INCIDENCE_RATE}) and access types
 * ({@code READ}/{@code WRITE}) are unquoted identifiers, so the quote-based
 * extraction picks up permissions only.
 */
public class AuthorizationPermissionStringsIT extends WebApiIT {

    private static final Pattern QUOTED = Pattern.compile("'([^']*)'");

    @Autowired
    @Qualifier("requestMappingHandlerMapping")
    private RequestMappingHandlerMapping handlerMapping;

    @Test
    public void everyPreAuthorizePermissionIsSeeded() {
        Set<String> seeded = new TreeSet<>(
            jdbcTemplate.queryForList("SELECT value FROM " + getOhdsiSchema() + ".sec_permission", String.class));

        Set<String> unknown = new TreeSet<>();
        for (HandlerMethod hm : handlerMapping.getHandlerMethods().values()) {
            if (!hm.getBeanType().getName().startsWith("org.ohdsi.webapi")) {
                continue;
            }
            for (String expr : preAuthorizeExpressions(hm)) {
                for (String permission : permissionLiterals(expr)) {
                    if (!seeded.contains(permission)) {
                        unknown.add(permission + "  (in: " + expr + ")");
                    }
                }
            }
        }
        assertTrue(
            "@PreAuthorize references permissions not present in sec_permission "
                + "(typo or missing migration?):\n  " + String.join("\n  ", unknown),
            unknown.isEmpty());
    }

    private static Set<String> preAuthorizeExpressions(HandlerMethod hm) {
        Set<String> exprs = new LinkedHashSet<>();
        PreAuthorize method = AnnotatedElementUtils.findMergedAnnotation(hm.getMethod(), PreAuthorize.class);
        if (method != null) {
            exprs.add(method.value());
        }
        PreAuthorize type = AnnotatedElementUtils.findMergedAnnotation(hm.getBeanType(), PreAuthorize.class);
        if (type != null) {
            exprs.add(type.value());
        }
        return exprs;
    }

    private static List<String> permissionLiterals(String expression) {
        List<String> permissions = new java.util.ArrayList<>();
        Matcher m = QUOTED.matcher(expression);
        while (m.find()) {
            permissions.add(m.group(1));
        }
        return permissions;
    }
}
