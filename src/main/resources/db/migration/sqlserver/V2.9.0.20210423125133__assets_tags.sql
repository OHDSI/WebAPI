CREATE SEQUENCE ${ohdsiSchema}.tags_seq START WITH 1;

-- Possible types are:
-- 1 - Predefined tags
-- 2 - Custom tags
-- 3 - Prizm tags
CREATE TABLE ${ohdsiSchema}.tags
(
    id        int4 NOT NULL,
    parent_id int4 NULL,
    name      VARCHAR(255),
    type      int4 NOT NULL DEFAULT 1,
);

ALTER TABLE ${ohdsiSchema}.tags
    ADD CONSTRAINT df_tags_id DEFAULT (NEXT VALUE FOR ${ohdsiSchema}.tags_seq) FOR id;

ALTER TABLE ${ohdsiSchema}.tags
    ADD CONSTRAINT pk_tags_id PRIMARY KEY (id);