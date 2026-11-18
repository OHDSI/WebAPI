package org.ohdsi.webapi.mcp;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Typed access to the {@code arguments} of an MCP tool call. Any missing or mistyped argument
 * fails with a {@link ToolContractException} naming the argument.
 */
public final class ToolArguments {

    private final Map<String, Object> args;

    public ToolArguments(Map<String, Object> args) {
        this.args = args == null ? Collections.emptyMap() : args;
    }

    public Map<String, Object> asMap() {
        return args;
    }

    public boolean has(String name) {
        return args.get(name) != null;
    }

    public Object raw(String name) {
        return args.get(name);
    }

    public String string(String name) {
        String value = optString(name);
        if (value == null || value.isBlank()) {
            throw new ToolContractException("'" + name + "' is required");
        }
        return value;
    }

    public String optString(String name) {
        Object value = args.get(name);
        if (value == null) {
            return null;
        }
        if (!(value instanceof String s)) {
            throw new ToolContractException("'" + name + "' must be a string");
        }
        return s;
    }

    public long longValue(String name) {
        Long value = optLong(name);
        if (value == null) {
            throw new ToolContractException("'" + name + "' is required");
        }
        return value;
    }

    public int intValue(String name) {
        long value = longValue(name);
        if (value < Integer.MIN_VALUE || value > Integer.MAX_VALUE) {
            throw new ToolContractException("'" + name + "' is out of range");
        }
        return (int) value;
    }

    public Long optLong(String name) {
        Object value = args.get(name);
        if (value == null) {
            return null;
        }
        if (value instanceof Number n) {
            double d = n.doubleValue();
            if (d != Math.rint(d)) {
                throw new ToolContractException("'" + name + "' must be an integer");
            }
            return n.longValue();
        }
        if (value instanceof String s) {
            try {
                return Long.parseLong(s.trim());
            } catch (NumberFormatException e) {
                // fall through
            }
        }
        throw new ToolContractException("'" + name + "' must be an integer");
    }

    public int optInt(String name, int defaultValue) {
        return has(name) ? intValue(name) : defaultValue;
    }

    public boolean bool(String name) {
        Boolean value = optBool(name);
        if (value == null) {
            throw new ToolContractException("'" + name + "' is required (true or false)");
        }
        return value;
    }

    public Boolean optBool(String name) {
        Object value = args.get(name);
        if (value == null) {
            return null;
        }
        if (value instanceof Boolean b) {
            return b;
        }
        throw new ToolContractException("'" + name + "' must be true or false");
    }

    public boolean optBool(String name, boolean defaultValue) {
        Boolean value = optBool(name);
        return value == null ? defaultValue : value;
    }

    public String[] optStringArray(String name) {
        Object value = args.get(name);
        if (value == null) {
            return null;
        }
        if (value instanceof String s) {
            return new String[] { s };
        }
        if (value instanceof List<?> list) {
            String[] out = new String[list.size()];
            for (int i = 0; i < list.size(); i++) {
                if (!(list.get(i) instanceof String s)) {
                    throw new ToolContractException("'" + name + "' must be an array of strings");
                }
                out[i] = s;
            }
            return out;
        }
        throw new ToolContractException("'" + name + "' must be an array of strings");
    }
}
