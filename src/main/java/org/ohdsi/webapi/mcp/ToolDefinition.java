package org.ohdsi.webapi.mcp;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Declarative description of one MCP tool: its name, JSON input schema, the reasoning step it
 * belongs to, whether it must carry a clinical rationale, and the code that runs it.
 */
public final class ToolDefinition {

    /** Whether a call must explain itself with {@code clinical_rationale} and {@code evidence_reference}. */
    public enum Rationale {
        NONE,
        OPTIONAL,
        REQUIRED
    }

    @FunctionalInterface
    public interface Handler {
        Object handle(ToolArguments args) throws Exception;
    }

    final String name;
    final String title;
    final String description;
    final ReasoningStep step;
    final boolean readOnly;
    final boolean destructive;
    final Rationale rationale;
    final Map<String, Object> properties;
    final List<String> required;
    final Handler handler;

    private ToolDefinition(Builder b) {
        this.name = Objects.requireNonNull(b.name);
        this.title = b.title;
        this.description = Objects.requireNonNull(b.description);
        this.step = Objects.requireNonNull(b.step);
        this.readOnly = b.readOnly;
        this.destructive = b.destructive;
        this.rationale = b.rationale;
        this.properties = b.properties;
        this.required = b.required;
        this.handler = Objects.requireNonNull(b.handler);
    }

    public String name() {
        return name;
    }

    public static Builder tool(String name) {
        return new Builder(name);
    }

    // ---- JSON schema helpers -------------------------------------------------------------

    public static Map<String, Object> stringParam(String description) {
        return param("string", description);
    }

    public static Map<String, Object> integerParam(String description) {
        return param("integer", description);
    }

    public static Map<String, Object> booleanParam(String description) {
        return param("boolean", description);
    }

    public static Map<String, Object> objectParam(String description) {
        return param("object", description);
    }

    public static Map<String, Object> stringArrayParam(String description) {
        Map<String, Object> p = param("array", description);
        p.put("items", Map.of("type", "string"));
        return p;
    }

    private static Map<String, Object> param(String type, String description) {
        Map<String, Object> p = new LinkedHashMap<>();
        p.put("type", type);
        p.put("description", description);
        return p;
    }

    public static final class Builder {
        private final String name;
        private String title;
        private String description;
        private ReasoningStep step;
        private boolean readOnly;
        private boolean destructive;
        private Rationale rationale = Rationale.NONE;
        private final Map<String, Object> properties = new LinkedHashMap<>();
        private final List<String> required = new ArrayList<>();
        private Handler handler;

        private Builder(String name) {
            this.name = name;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder step(ReasoningStep step) {
            this.step = step;
            return this;
        }

        public Builder readOnly() {
            this.readOnly = true;
            return this;
        }

        public Builder destructive() {
            this.destructive = true;
            return this;
        }

        public Builder rationale(Rationale rationale) {
            this.rationale = rationale;
            return this;
        }

        public Builder param(String name, Map<String, Object> schema) {
            properties.put(name, schema);
            return this;
        }

        public Builder requiredParam(String name, Map<String, Object> schema) {
            properties.put(name, schema);
            required.add(name);
            return this;
        }

        public Builder handler(Handler handler) {
            this.handler = handler;
            return this;
        }

        public ToolDefinition build() {
            return new ToolDefinition(this);
        }
    }
}
