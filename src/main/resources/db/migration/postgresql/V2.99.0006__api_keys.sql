-- Create the sec_api_key table for personal API key authentication.
-- Keys are split into a public identifier (indexed) and a BCrypt-hashed secret,
-- allowing O(1) lookup without scanning every row.

CREATE SEQUENCE ${ohdsiSchema}.sec_api_key_sequence START WITH 1 INCREMENT BY 1;

CREATE TABLE ${ohdsiSchema}.sec_api_key (
    id          BIGINT                   NOT NULL DEFAULT nextval('${ohdsiSchema}.sec_api_key_sequence'),
    key_identifier VARCHAR(64)           NOT NULL,
    key_hash    VARCHAR(255)             NOT NULL,
    user_id     BIGINT                   NOT NULL,
    name        VARCHAR(255)             NOT NULL,
    description VARCHAR(1000),
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL,
    expires_at  TIMESTAMP WITH TIME ZONE,
    disabled    BOOLEAN                  NOT NULL DEFAULT FALSE,
    last_used_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT pk_sec_api_key          PRIMARY KEY (id),
    CONSTRAINT uq_sec_api_key_identifier UNIQUE (key_identifier),
    CONSTRAINT fk_sec_api_key_user     FOREIGN KEY (user_id)
        REFERENCES ${ohdsiSchema}.sec_user(id) ON DELETE CASCADE
);

-- Explicit index on key_identifier to guarantee O(1) lookup during authentication.
CREATE INDEX idx_sec_api_key_identifier ON ${ohdsiSchema}.sec_api_key (key_identifier);
