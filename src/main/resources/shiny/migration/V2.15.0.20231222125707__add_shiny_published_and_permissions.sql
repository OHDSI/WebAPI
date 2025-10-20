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

INSERT INTO ${ohdsiSchema}.sec_permission (id, value, description)
SELECT nextval('${ohdsiSchema}.sec_permission_id_seq'),
       'shiny:download:*:*:*:get',
       'Download Shiny Application presenting analysis results';

INSERT INTO ${ohdsiSchema}.sec_permission (id, value, description)
SELECT nextval('${ohdsiSchema}.sec_permission_id_seq'),
       'shiny:publish:*:*:*:get',
       'Publish Shiny Application presenting analysis results to external resource';

INSERT INTO ${ohdsiSchema}.sec_role_permission (role_id, permission_id)
SELECT sr.id, sp.id
FROM ${ohdsiSchema}.sec_permission sp,
     ${ohdsiSchema}.sec_role sr
WHERE sp."value" in
      (
          'shiny:download:*:*:*:get',
          'shiny:publish:*:*:*:get'
      )
  AND sr.name IN ('Atlas users');
