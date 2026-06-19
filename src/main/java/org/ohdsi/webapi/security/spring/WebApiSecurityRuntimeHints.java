package org.ohdsi.webapi.security.spring;

import org.ohdsi.webapi.security.authz.access.AccessType;
import org.ohdsi.webapi.security.authz.access.EntityType;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;

/**
 * GraalVM native-image reflection hints for method-security SpEL evaluation.
 *
 * <p>{@code @PreAuthorize} expressions reference public fields on
 * {@link WebApiSecurityExpressionRoot} (e.g. {@code READ}, {@code WRITE},
 * {@code SOURCE}, {@code CONCEPT_SET}) and the {@link AccessType}/{@link EntityType}
 * enums. SpEL resolves these reflectively at evaluation time, so the types — including
 * their fields — must be reflection-reachable in a native image, otherwise expression
 * evaluation fails at runtime.
 *
 * <p>A no-op on a regular JVM; consumed only by GraalVM AOT processing.
 */
public class WebApiSecurityRuntimeHints implements RuntimeHintsRegistrar {

  @Override
  public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
    hints.reflection()
        .registerType(WebApiSecurityExpressionRoot.class, MemberCategory.values())
        .registerType(AccessType.class, MemberCategory.values())
        .registerType(EntityType.class, MemberCategory.values());
  }
}
