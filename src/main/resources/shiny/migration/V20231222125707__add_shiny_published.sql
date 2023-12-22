CREATE SEQUENCE ${ohdsiSchema}.shiny_published_sequence START WITH 1;

CREATE TABLE ${ohdsiSchema}.shiny_published(
    id BIGINT PRIMARY KEY default nextval('${ohdsiSchema}.shiny_published_sequence'),
    type VARCHAR NOT NULL,
    analysis_id BIGINT NOT NULL,
    execution_id BIGINT,
    source_key VARCHAR,
    content_id UUID,
    created_by_id BIGINT REFERENCES ${ohdsiSchema}.sec_user(id),
    modified_by_id BIGINT REFERENCES ${ohdsiSchema}.sec_user(id),
    created_date TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT (now()),
    modified_date TIMESTAMP WITH TIME ZONE
);
