package org.ohdsi.webapi.mcp.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;

import java.util.concurrent.Callable;

/** Runs a tool body and converts any exception into an {@link McpResult} envelope. */
public final class McpCall {

    private static final Logger log = LoggerFactory.getLogger(McpCall.class);

    private McpCall() {
    }

    public static McpResult guard(Callable<?> body) {
        try {
            return McpResult.ok(body.call());
        } catch (AccessDeniedException e) {
            return McpResult.error("permission_denied", e.getMessage());
        } catch (IllegalArgumentException e) {
            return McpResult.error("invalid_input", e.getMessage());
        } catch (Exception e) {
            log.error("MCP tool call failed", e);
            return McpResult.error("upstream_error", "The WebAPI call failed. See server logs.");
        }
    }
}
