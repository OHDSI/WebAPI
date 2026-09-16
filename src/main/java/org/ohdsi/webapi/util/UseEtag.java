package org.ohdsi.webapi.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a controller method to enable HTTP ETag support.
 * <p>
 * When applied, the response body will be hashed to generate an ETag header.
 * If the client sends an {@code If-None-Match} header matching the ETag,
 * a 304 Not Modified response is returned instead of the full body.
 * </p>
 *
 * <pre>
 * &#64;GetMapping("/{id}")
 * &#64;UseEtag
 * public MyDto getById(@PathVariable Long id) {
 *     return service.findById(id);
 * }
 * </pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface UseEtag {
}
