# HTTP ETag Support for WebAPI

## Overview

WebAPI supports HTTP ETag caching for selected GET endpoints. When enabled, the server generates an ETag (hash of the response body) and returns it with each response. On subsequent requests, if the client sends the ETag via `If-None-Match` header and the content hasn't changed, the server returns `304 Not Modified` instead of the full response body, saving bandwidth and improving performance.

## Quick Start

Add the `@UseEtag` annotation to any controller method:

```java
import org.ohdsi.webapi.util.UseEtag;

@GetMapping("/{id}")
@UseEtag
public CohortDefinitionDTO getCohortDefinition(@PathVariable Integer id) {
    return cohortDefinitionService.findById(id);
}
```

That's it. No changes to return types or service layer required.

## How It Works

1. **First Request**: Client requests `/cohortdefinition/123`
   - Server generates response JSON
   - Filter computes MD5 hash of response body
   - Response includes weak ETag header: `ETag: W/"0a1b2c3..."` 
   - Client receives full response (200 OK)

2. **Subsequent Requests**: Client requests same URL
   - Browser automatically sends `If-None-Match: W/"0a1b2c3..."` header
   - Filter computes ETag of current response
   - If ETags match → returns `304 Not Modified` (no body)
   - If ETags differ → returns full response with new ETag (200 OK)

## Implementation Components

### `@UseEtag` Annotation

Location: `org.ohdsi.webapi.util.UseEtag`

Method-level annotation that marks endpoints for ETag support. Only methods with this annotation are processed by the ETag filter.

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface UseEtag {
}
```

### `EtagFilter` Servlet Filter

Location: `org.ohdsi.webapi.util.EtagFilter`

Extends Spring's `ShallowEtagHeaderFilter` to add selective ETag processing based on the `@UseEtag` annotation:

1. Overrides `shouldNotFilter()` to skip requests without `@UseEtag` annotation
2. Sets Cache-Control and CORS headers before delegating to parent
3. Leverages Spring's built-in ETag generation (MD5) and 304 handling
4. Configured to generate weak ETags (`W/"..."`) per RFC 7232

**Key Design Decision**: Extends `ShallowEtagHeaderFilter` rather than implementing from scratch. This leverages Spring's tested implementation for response caching, ETag generation, async dispatch handling, and `If-None-Match` comparison.

## HTTP Headers

For `@UseEtag` endpoints, the filter sets these response headers:

| Header | Value | Purpose |
|--------|-------|---------|
| `ETag` | `W/"0<md5-hash>"` | Weak ETag - unique identifier for response content |
| `Cache-Control` | `private, max-age=0, must-revalidate` | Allows browser caching but forces revalidation |
| `Access-Control-Expose-Headers` | `ETag` | Exposes ETag to JavaScript in CORS contexts |

## Testing

### Using curl

```bash
# First request - get ETag
curl -i -H "Authorization: Bearer <token>" http://localhost:8080/WebAPI/cohortdefinition/

# Note the ETag header value, then:
curl -i -H "Authorization: Bearer <token>" \
     -H 'If-None-Match: "<etag-value>"' \
     http://localhost:8080/WebAPI/cohortdefinition/

# Should return: HTTP/1.1 304 Not Modified
```

### Using PowerShell

```powershell
$headers = @{ "Authorization" = "Bearer <token>" }
$r1 = Invoke-WebRequest -Uri "http://localhost:8080/WebAPI/cohortdefinition/" -Headers $headers
$etag = $r1.Headers["ETag"]
Write-Host "ETag: $etag"

$headers2 = @{ 
    "Authorization" = "Bearer <token>"
    "If-None-Match" = $etag 
}
$r2 = Invoke-WebRequest -Uri "http://localhost:8080/WebAPI/cohortdefinition/" -Headers $headers2
Write-Host "Status: $($r2.StatusCode)"  # Should be 304
```

### Browser Testing

1. Open Chrome DevTools → Network tab
2. Ensure "Disable cache" is **unchecked**
3. Make first request (200 OK with ETag header)
4. Navigate away and back, or refresh normally (not hard refresh)
5. Second request should show 304 Not Modified

## Troubleshooting

### Issue: Application fails to start with "required a single bean, but 2 were found"

**Error:**
```
Parameter 0 of constructor in org.ohdsi.webapi.util.EtagFilter required a single bean, but 2 were found:
- requestMappingHandlerMapping
- controllerEndpointHandlerMapping
```

**Solution:** The `EtagFilter` constructor uses `@Qualifier("requestMappingHandlerMapping")` to disambiguate between Spring MVC's handler mapping and Spring Actuator's endpoint handler mapping.

### Issue: Browser not sending `If-None-Match` header

**Symptoms:** ETag header appears in response, but subsequent requests don't include `If-None-Match`.

**Common Causes:**

1. **`Cache-Control: no-store`** - Spring Security sets this by default. The filter overrides it with `private, max-age=0, must-revalidate` for `@UseEtag` endpoints.

2. **CORS requests** - Cross-origin requests require special handling:
   - The `Access-Control-Expose-Headers: ETag` header must be set (the filter does this)
   - The `Vary: Origin` header can affect caching behavior
   - `max-age=0, must-revalidate` works better than `no-cache` for some browsers in CORS scenarios

3. **DevTools "Disable cache" enabled** - Check Chrome DevTools Network tab settings.

4. **Hard refresh** - Ctrl+Shift+R bypasses cache. Use normal refresh (F5) or navigation.

### Issue: curl command fails in PowerShell

**Error:**
```
Invoke-WebRequest: ParameterBindingException
```

**Cause:** PowerShell aliases `curl` to `Invoke-WebRequest`.

**Solution:** Use `curl.exe` explicitly, or use PowerShell's `Invoke-WebRequest` syntax.

### Issue: ETag works with curl but not in browser (CORS)

**Explanation:** When the frontend (e.g., Atlas at `localhost:80`) and backend (WebAPI at `localhost:8080`) are on different origins, browser caching behavior is more restrictive.

**What helped:**
- `Cache-Control: private, max-age=0, must-revalidate` (instead of just `no-cache`)
- `Access-Control-Expose-Headers: ETag` to expose the header to JavaScript
- Preserving Spring's default `Vary` header for CORS correctness

**Alternative:** For maximum browser caching reliability, serve Atlas and WebAPI from the same origin.

## Limitations

1. **Response body required** - ETag is computed from response content; empty responses are skipped
2. **Successful responses only** - ETag is only added for 2xx status codes
3. **Per-request computation** - ETag is computed fresh for each request; no server-side caching of ETags
4. **JSON responses** - Designed for JSON APIs; binary responses may work but are not the primary use case

## Security Considerations

- ETags use MD5 hashing (sufficient for cache validation; collision resistance is not required)
- Weak ETags (`W/"..."`) indicate semantic equivalence rather than byte-for-byte identity
- `Cache-Control: private` ensures responses are not stored in shared caches (proxies)
- The `Vary: Origin` header (set by Spring CORS) ensures CORS responses are cached per-origin

## References

- [RFC 7232 - HTTP Conditional Requests](https://tools.ietf.org/html/rfc7232)
- [MDN - ETag](https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/ETag)
- [MDN - If-None-Match](https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/If-None-Match)
- [Spring ShallowEtagHeaderFilter](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/filter/ShallowEtagHeaderFilter.html)
