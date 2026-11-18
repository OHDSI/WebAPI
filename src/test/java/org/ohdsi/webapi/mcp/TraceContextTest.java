package org.ohdsi.webapi.mcp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class TraceContextTest {

    private static final String TRACE_ID = "4bf92f3577b34da6a3ce929d0e0e4736";
    private static final String PARENT = "00f067aa0ba902b7";

    @Test
    public void continuesTheCallersTraceWithANewSpan() {
        TraceContext t = TraceContext.continueFrom("00-" + TRACE_ID + "-" + PARENT + "-01");

        assertEquals(TRACE_ID, t.traceId());
        assertEquals(PARENT, t.parentSpanId());
        assertNotEquals(PARENT, t.spanId());
        assertTrue(t.spanId().matches("[0-9a-f]{16}"));
        assertFalse(t.generated());
        assertEquals("00-" + TRACE_ID + "-" + t.spanId() + "-01", t.traceparent());
    }

    @Test
    public void keepsTheSampledFlagAndAcceptsUpperCase() {
        TraceContext t = TraceContext.continueFrom(("00-" + TRACE_ID + "-" + PARENT + "-00").toUpperCase());
        assertEquals(TRACE_ID, t.traceId());
        assertTrue(t.traceparent().endsWith("-00"));
    }

    @Test
    public void newRootHasNoParent() {
        TraceContext t = TraceContext.newRoot();
        assertTrue(t.traceId().matches("[0-9a-f]{32}"));
        assertNull(t.parentSpanId());
        assertTrue(t.generated());
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsMalformedTraceparent() {
        TraceContext.continueFrom("00-" + TRACE_ID + "-" + PARENT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsAllZeroTraceId() {
        TraceContext.continueFrom("00-" + "0".repeat(32) + "-" + PARENT + "-01");
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsVersionFf() {
        TraceContext.continueFrom("ff-" + TRACE_ID + "-" + PARENT + "-01");
    }
}
