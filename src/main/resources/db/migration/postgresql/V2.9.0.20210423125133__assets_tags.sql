CREATE SEQUENCE ${ohdsiSchema}.tags_seq;

-- Possible types are:
-- 0 - System (predefined) tags
-- 1 - Custom tags
-- 2 - Prizm tags
CREATE TABLE ${ohdsiSchema}.tags
(
    id             int4                     NOT NULL DEFAULT nextval('${ohdsiSchema}.tags_seq'),
    name           VARCHAR                  NOT NULL,
    type           int4                     NOT NULL DEFAULT 0,
    created_by_id  INTEGER,
    created_date   TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT (now()),
    modified_by_id INTEGER,
    modified_date  TIMESTAMP WITH TIME ZONE,
    CONSTRAINT pk_tags_id PRIMARY KEY (id),
    CONSTRAINT fk_tags_sec_user_creator FOREIGN KEY (created_by_id) REFERENCES ${ohdsiSchema}.sec_user(id),
    CONSTRAINT fk_tags_sec_user_updater FOREIGN KEY (modified_by_id) REFERENCES ${ohdsiSchema}.sec_user(id)
);

CREATE UNIQUE INDEX tags_name_idx ON ${ohdsiSchema}.tags USING btree (name);

CREATE TABLE ${ohdsiSchema}.tag_groups
(
    tag_id    int4 NOT NULL,
    group_id int4 NOT NULL,
    CONSTRAINT tag_groups_group_fk FOREIGN KEY (group_id) REFERENCES ${ohdsiSchema}.tags(id) ON DELETE CASCADE,
    CONSTRAINT tag_groups_tag_fk FOREIGN KEY (tag_id) REFERENCES ${ohdsiSchema}.tags(id) ON DELETE CASCADE
);

CREATE TABLE ${ohdsiSchema}.concept_set_tags
(
    concept_set_id int4 NOT NULL,
    tag_id     int4 NOT NULL,
    CONSTRAINT concept_set_tags_fk_sets FOREIGN KEY (concept_set_id) REFERENCES ${ohdsiSchema}.concept_set(concept_set_id) ON DELETE CASCADE,
    CONSTRAINT concept_set_tags_fk_tags FOREIGN KEY (tag_id) REFERENCES ${ohdsiSchema}.tags(id) ON DELETE CASCADE
);

CREATE UNIQUE INDEX concept_set_tags_idx ON ${ohdsiSchema}.concept_set_tags USING btree (concept_set_id, tag_id);

CREATE TABLE ${ohdsiSchema}.cohort_tags
(
    cohort_id int4 NOT NULL,
    tag_id    int4 NOT NULL,
    CONSTRAINT cohort_tags_fk_definitions FOREIGN KEY (cohort_id) REFERENCES ${ohdsiSchema}.cohort_definition(id) ON DELETE CASCADE,
    CONSTRAINT cohort_tags_fk_tags FOREIGN KEY (tag_id) REFERENCES ${ohdsiSchema}.tags(id) ON DELETE CASCADE
);

CREATE UNIQUE INDEX cohort_tags_idx ON ${ohdsiSchema}.cohort_tags USING btree (cohort_id, tag_id);

CREATE TABLE ${ohdsiSchema}.cohort_characterization_tags
(
    cohort_characterization_id int4 NOT NULL,
    tag_id                     int4 NOT NULL,
    CONSTRAINT cc_tags_fk_ccs FOREIGN KEY (cohort_characterization_id) REFERENCES ${ohdsiSchema}.cohort_characterization(id) ON DELETE CASCADE,
    CONSTRAINT cc_tags_fk_tags FOREIGN KEY (tag_id) REFERENCES ${ohdsiSchema}.tags(id) ON DELETE CASCADE
);

CREATE UNIQUE INDEX cohort_characterization_tags_tags_idx ON ${ohdsiSchema}.cohort_characterization_tags USING btree (cohort_characterization_id, tag_id);

CREATE TABLE ${ohdsiSchema}.ir_tags
(
    analysis_id int4 NOT NULL,
    tag_id      int4 NOT NULL,
    CONSTRAINT ir_tags_fk_irs FOREIGN KEY (analysis_id) REFERENCES ${ohdsiSchema}.ir_analysis(id) ON DELETE CASCADE,
    CONSTRAINT ir_tags_fk_tags FOREIGN KEY (tag_id) REFERENCES ${ohdsiSchema}.tags(id) ON DELETE CASCADE
);

CREATE UNIQUE INDEX ir_tags_idx ON ${ohdsiSchema}.ir_tags USING btree (analysis_id, tag_id);

CREATE TABLE ${ohdsiSchema}.pathway_tags
(
    pathway_analysis_id int4 NOT NULL,
    tag_id              int4 NOT NULL,
    CONSTRAINT ir_tags_fk_irs FOREIGN KEY (pathway_analysis_id) REFERENCES ${ohdsiSchema}.pathway_analysis(id) ON DELETE CASCADE,
    CONSTRAINT ir_tags_fk_tags FOREIGN KEY (tag_id) REFERENCES ${ohdsiSchema}.tags(id) ON DELETE CASCADE
);

CREATE UNIQUE INDEX pathway_tags_idx ON ${ohdsiSchema}.pathway_tags USING btree (pathway_analysis_id, tag_id);

-- Default tags
INSERT INTO ${ohdsiSchema}.tags
(name, type)
VALUES ('Tag group 1', 1);

INSERT INTO ${ohdsiSchema}.tags
(name, type)
VALUES ('Tag group 2', 1);

INSERT INTO ${ohdsiSchema}.tags
(name, type)
VALUES ('Tag group 3', 1);

INSERT INTO ${ohdsiSchema}.tags
(name, type)
VALUES ('Tag 11', 1);

INSERT INTO ${ohdsiSchema}.tag_groups
(tag_id, parent_id)
SELECT t1.id, t2.id
FROM ${ohdsiSchema}.tags t1,
     ${ohdsiSchema}.tags t2
WHERE t1.name = 'Tag 11'
  and t2.name = 'Tag group 1';

INSERT INTO ${ohdsiSchema}.tags
(name, type)
VALUES ('Tag 12', 1);

INSERT INTO ${ohdsiSchema}.tag_groups
(tag_id, parent_id)
SELECT t1.id, t2.id
FROM ${ohdsiSchema}.tags t1,
     ${ohdsiSchema}.tags t2
WHERE t1.name = 'Tag 12'
  and t2.name = 'Tag group 1';

INSERT INTO ${ohdsiSchema}.tags
(name, type)
VALUES ('Tag 21', 1);

INSERT INTO ${ohdsiSchema}.tag_groups
(tag_id, parent_id)
SELECT t1.id, t2.id
FROM ${ohdsiSchema}.tags t1,
     ${ohdsiSchema}.tags t2
WHERE t1.name = 'Tag 21'
  and t2.name = 'Tag group 1';

INSERT INTO ${ohdsiSchema}.tag_groups
(tag_id, parent_id)
SELECT t1.id, t2.id
FROM ${ohdsiSchema}.tags t1,
     ${ohdsiSchema}.tags t2
WHERE t1.name = 'Tag 21'
  and t2.name = 'Tag group 2';

INSERT INTO ${ohdsiSchema}.tags
(name, type)
VALUES ('Tag 22', 1);

INSERT INTO ${ohdsiSchema}.tag_groups
(tag_id, parent_id)
SELECT t1.id, t2.id
FROM ${ohdsiSchema}.tags t1,
     ${ohdsiSchema}.tags t2
WHERE t1.name = 'Tag 22'
  and t2.name = 'Tag group 2';

INSERT INTO ${ohdsiSchema}.tags
(name, type)
VALUES ('Tag 23', 1);

INSERT INTO ${ohdsiSchema}.tag_groups
(tag_id, parent_id)
SELECT t1.id, t2.id
FROM ${ohdsiSchema}.tags t1,
     ${ohdsiSchema}.tags t2
WHERE t1.name = 'Tag 23'
  and t2.name = 'Tag group 2';