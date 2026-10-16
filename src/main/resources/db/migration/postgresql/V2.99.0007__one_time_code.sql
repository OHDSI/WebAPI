-- One-Time Code (OTC) table for OAuth2/OIDC authentication delivery
-- OTC wraps pre-minted JWT tokens to support load-balanced WebAPI instances
-- Single-use codes with 10-minute expiration

CREATE TABLE ${ohdsiSchema}.sec_one_time_code (
    code           UUID NOT NULL,
    login          VARCHAR(255) NOT NULL,
    origin         VARCHAR(50) NOT NULL,
    jwt_token      TEXT NOT NULL,
    created_at     TIMESTAMP NOT NULL,
    expires_at     TIMESTAMP NOT NULL,
    revoked        BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT pk_sec_one_time_code PRIMARY KEY (code)
);

-- Index for cleanup queries (find expired codes)
CREATE INDEX idx_sec_one_time_code_expires_at
    ON ${ohdsiSchema}.sec_one_time_code(expires_at);

-- Index for login-based queries
CREATE INDEX idx_sec_one_time_code_login
    ON ${ohdsiSchema}.sec_one_time_code(login, expires_at);

-- Composite index for common query pattern: lookup by code + expiry check
CREATE INDEX idx_sec_one_time_code_lookup
    ON ${ohdsiSchema}.sec_one_time_code(code, expires_at, revoked);
