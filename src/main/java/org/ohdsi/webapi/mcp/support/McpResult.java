package org.ohdsi.webapi.mcp.support;

/** Uniform envelope returned by every MCP tool. Serialized to JSON for the model. */
public record McpResult(boolean ok, String status, Object data, String message) {

    public static McpResult ok(Object data) {
        return new McpResult(true, "ok", data, null);
    }

    public static McpResult error(String status, String message) {
        return new McpResult(false, status, null, message);
    }
}
