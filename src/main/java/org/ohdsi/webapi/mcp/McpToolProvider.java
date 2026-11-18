package org.ohdsi.webapi.mcp;

import java.util.List;

/** A group of MCP tools. Every bean of this type is published on the MCP endpoint. */
public interface McpToolProvider {

    List<ToolDefinition> tools();
}
