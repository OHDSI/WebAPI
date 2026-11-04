-- Audit trail for tool calls made through the MCP endpoint (/mcp).
--
-- Every tool call is written here before it runs (outcome STARTED) and completed once it
-- finishes, so no tool can change a concept set or cohort without leaving a row behind.
-- Rows are append-only: the trigger below allows exactly one update per row (STARTED ->
-- final outcome, touching only the result columns) and rejects every delete.
CREATE SEQUENCE IF NOT EXISTS ${ohdsiSchema}.mcp_audit_log_sequence;

CREATE TABLE IF NOT EXISTS ${ohdsiSchema}.mcp_audit_log (
    id bigint DEFAULT nextval('${ohdsiSchema}.mcp_audit_log_sequence'::regclass) NOT NULL,
    trace_id character varying(32) NOT NULL,
    -- span id the client sent in _meta.traceparent (null when the client sent none)
    parent_span_id character varying(16),
    -- span id WebAPI allocated for this tool call; returned to the client in _meta.traceparent
    span_id character varying(16) NOT NULL,
    -- true when the client sent no traceparent and WebAPI started a new trace
    trace_generated boolean NOT NULL,
    reasoning_step character varying(64),
    tool_name character varying(128) NOT NULL,
    arguments_json text,
    clinical_rationale text,
    evidence_reference text,
    outcome character varying(16) NOT NULL,
    result_json text,
    error_message text,
    user_id bigint,
    user_login character varying(255),
    started_at timestamp with time zone NOT NULL,
    finished_at timestamp with time zone,
    CONSTRAINT pk_mcp_audit_log PRIMARY KEY (id),
    CONSTRAINT chk_mcp_audit_log_outcome
        CHECK (outcome IN ('STARTED', 'SUCCESS', 'ERROR', 'REJECTED', 'DENIED'))
);

CREATE INDEX IF NOT EXISTS mcp_audit_log_trace_idx
    ON ${ohdsiSchema}.mcp_audit_log USING btree (trace_id, started_at);

CREATE OR REPLACE FUNCTION ${ohdsiSchema}.mcp_audit_log_append_only()
RETURNS trigger AS $$
BEGIN
    IF TG_OP = 'DELETE' THEN
        RAISE EXCEPTION 'mcp_audit_log is append-only: rows cannot be deleted';
    END IF;
    IF OLD.outcome <> 'STARTED' THEN
        RAISE EXCEPTION 'mcp_audit_log is append-only: entry % is already completed', OLD.id;
    END IF;
    IF NEW.id IS DISTINCT FROM OLD.id
        OR NEW.trace_id IS DISTINCT FROM OLD.trace_id
        OR NEW.parent_span_id IS DISTINCT FROM OLD.parent_span_id
        OR NEW.span_id IS DISTINCT FROM OLD.span_id
        OR NEW.trace_generated IS DISTINCT FROM OLD.trace_generated
        OR NEW.reasoning_step IS DISTINCT FROM OLD.reasoning_step
        OR NEW.tool_name IS DISTINCT FROM OLD.tool_name
        OR NEW.arguments_json IS DISTINCT FROM OLD.arguments_json
        OR NEW.clinical_rationale IS DISTINCT FROM OLD.clinical_rationale
        OR NEW.evidence_reference IS DISTINCT FROM OLD.evidence_reference
        OR NEW.user_id IS DISTINCT FROM OLD.user_id
        OR NEW.user_login IS DISTINCT FROM OLD.user_login
        OR NEW.started_at IS DISTINCT FROM OLD.started_at THEN
        RAISE EXCEPTION 'mcp_audit_log is append-only: only the outcome of entry % may be recorded', OLD.id;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS mcp_audit_log_append_only ON ${ohdsiSchema}.mcp_audit_log;
CREATE TRIGGER mcp_audit_log_append_only
    BEFORE UPDATE OR DELETE ON ${ohdsiSchema}.mcp_audit_log
    FOR EACH ROW EXECUTE FUNCTION ${ohdsiSchema}.mcp_audit_log_append_only();
