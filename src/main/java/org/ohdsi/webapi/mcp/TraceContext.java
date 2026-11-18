package org.ohdsi.webapi.mcp;

import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * W3C Trace Context for one MCP tool call.
 *
 * <p>The client sends its span as {@code _meta.traceparent}; WebAPI keeps the client's trace id,
 * records the client's span as the parent, and allocates a fresh span id for the tool call itself.
 * The returned {@link #traceparent()} is what the client should use as parent for anything the tool
 * result causes it to do next.
 *
 * @see <a href="https://www.w3.org/TR/trace-context/#traceparent-header">traceparent</a>
 */
public record TraceContext(String traceId, String parentSpanId, String spanId, String flags, boolean generated) {

    private static final Pattern TRACEPARENT =
            Pattern.compile("^([0-9a-f]{2})-([0-9a-f]{32})-([0-9a-f]{16})-([0-9a-f]{2})$");
    private static final String INVALID_TRACE_ID = "0".repeat(32);
    private static final String INVALID_SPAN_ID = "0".repeat(16);
    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * Continues the trace in {@code traceparent}.
     *
     * @throws IllegalArgumentException if it is not a valid version-00 traceparent
     */
    public static TraceContext continueFrom(String traceparent) {
        String value = traceparent.trim().toLowerCase(Locale.ROOT);
        var m = TRACEPARENT.matcher(value);
        if (!m.matches()) {
            throw new IllegalArgumentException(
                    "traceparent must look like 00-<32 hex trace-id>-<16 hex parent-id>-<2 hex flags>, got: " + traceparent);
        }
        if ("ff".equals(m.group(1))) {
            throw new IllegalArgumentException("traceparent version ff is invalid");
        }
        if (INVALID_TRACE_ID.equals(m.group(2)) || INVALID_SPAN_ID.equals(m.group(3))) {
            throw new IllegalArgumentException("traceparent trace-id and parent-id must not be all zeros");
        }
        return new TraceContext(m.group(2), m.group(3), randomHex(8), m.group(4), false);
    }

    /** Starts a new trace for a client that sent no traceparent. */
    public static TraceContext newRoot() {
        return new TraceContext(randomHex(16), null, randomHex(8), "01", true);
    }

    /** The traceparent for this tool call's span, to hand back to the client. */
    public String traceparent() {
        return "00-" + traceId + "-" + spanId + "-" + flags;
    }

    private static String randomHex(int bytes) {
        byte[] buf = new byte[bytes];
        String hex;
        do {
            RANDOM.nextBytes(buf);
            hex = HexFormat.of().formatHex(buf);
        } while (hex.chars().allMatch(c -> c == '0'));
        return hex;
    }
}
