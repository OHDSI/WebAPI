CREATE SEQUENCE ${ohdsiSchema}.shiny_published_sequence START WITH 1;

CREATE TABLE ${ohdsiSchema}.shiny_published
(
    id BIGINT NOT NULL PRIMARY KEY DEFAULT nextval('${ohdsiSchema}.shiny_published_sequence'),
    type VARCHAR,
    analysis_id BIGINT,
    source_key VARCHAR,
    execution_id BIGINT,
    content_id UUID,
    created_by_id BIGINT,
    modified_by_id BIGINT,
    created_date TIMESTAMP,
    modified_date TIMESTAMP
);