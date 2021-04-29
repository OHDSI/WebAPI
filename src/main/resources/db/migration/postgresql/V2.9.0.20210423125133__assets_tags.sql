INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description)
VALUES
(NEXTVAL('${ohdsiSchema}.sec_permission_id_seq'), 'tag:get', 'List tags'),
(NEXTVAL('${ohdsiSchema}.sec_permission_id_seq'), 'tag:post', 'Create tag'),
(NEXTVAL('${ohdsiSchema}.sec_permission_id_seq'), 'tag:*:put', 'Update tag'),
(NEXTVAL('${ohdsiSchema}.sec_permission_id_seq'), 'tag:*:get', 'Get tag'),
(NEXTVAL('${ohdsiSchema}.sec_permission_id_seq'), 'tag:*:delete', 'Delete tag'),
(NEXTVAL('${ohdsiSchema}.sec_permission_id_seq'), 'cohortdefinition:*:tag:post', 'Assign tag to cohort definition'),
(NEXTVAL('${ohdsiSchema}.sec_permission_id_seq'), 'cohortdefinition:*:tag:*:delete', 'Unassign tag from cohort definition');

INSERT INTO ${ohdsiSchema}.sec_role_permission(role_id, permission_id)
SELECT sr.id, sp.id
FROM ${ohdsiSchema}.sec_permission SP, ${ohdsiSchema}.sec_role sr
WHERE sp.value IN (
                   'tag:get',
                   'tag:post',
                   'tag:*:put',
                   'tag:*:get',
                   'tag:*:delete',
                   'cohortdefinition:*:tag:post',
                   'cohortdefinition:*:tag:*:delete')
  AND sr.name IN ('Atlas users');

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
    count          int4                     NOT NULL DEFAULT 0,
    created_by_id  INTEGER,
    created_date   TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT (now()),
    modified_by_id INTEGER,
    modified_date  TIMESTAMP WITH TIME ZONE,
    CONSTRAINT pk_tags_id PRIMARY KEY (id),
    CONSTRAINT fk_tags_sec_user_creator FOREIGN KEY (created_by_id) REFERENCES ${ohdsiSchema}.sec_user (id),
    CONSTRAINT fk_tags_sec_user_updater FOREIGN KEY (modified_by_id) REFERENCES ${ohdsiSchema}.sec_user (id)
);

CREATE UNIQUE INDEX tags_name_idx ON ${ohdsiSchema}.tags USING btree (LOWER(name));

CREATE TABLE ${ohdsiSchema}.tag_groups
(
    tag_id   int4 NOT NULL,
    group_id int4 NOT NULL,
    CONSTRAINT tag_groups_group_fk FOREIGN KEY (group_id) REFERENCES ${ohdsiSchema}.tags (id) ON DELETE CASCADE,
    CONSTRAINT tag_groups_tag_fk FOREIGN KEY (tag_id) REFERENCES ${ohdsiSchema}.tags (id) ON DELETE CASCADE
);

CREATE TABLE ${ohdsiSchema}.concept_set_tags
(
    asset_id int4 NOT NULL,
    tag_id   int4 NOT NULL,
    CONSTRAINT pk_concept_set_tags_id PRIMARY KEY (asset_id, tag_id),
    CONSTRAINT concept_set_tags_fk_sets FOREIGN KEY (asset_id) REFERENCES ${ohdsiSchema}.concept_set (concept_set_id) ON DELETE CASCADE,
    CONSTRAINT concept_set_tags_fk_tags FOREIGN KEY (tag_id) REFERENCES ${ohdsiSchema}.tags (id) ON DELETE CASCADE
);

CREATE INDEX concept_set_tags_concept_id_idx ON ${ohdsiSchema}.concept_set_tags USING btree (asset_id);
CREATE INDEX concept_set_tags_tag_id_idx ON ${ohdsiSchema}.concept_set_tags USING btree (tag_id);

CREATE TABLE ${ohdsiSchema}.cohort_tags
(
    asset_id int4 NOT NULL,
    tag_id   int4 NOT NULL,
    CONSTRAINT pk_cohort_tags_id PRIMARY KEY (asset_id, tag_id),
    CONSTRAINT cohort_tags_fk_definitions FOREIGN KEY (asset_id) REFERENCES ${ohdsiSchema}.cohort_definition (id) ON DELETE CASCADE,
    CONSTRAINT cohort_tags_fk_tags FOREIGN KEY (tag_id) REFERENCES ${ohdsiSchema}.tags (id) ON DELETE CASCADE
);

CREATE INDEX cohort_tags_cohort_id_idx ON ${ohdsiSchema}.cohort_tags USING btree (asset_id);
CREATE INDEX cohort_tags_tag_id_idx ON ${ohdsiSchema}.cohort_tags USING btree (tag_id);

CREATE TABLE ${ohdsiSchema}.cohort_characterization_tags
(
    asset_id int4 NOT NULL,
    tag_id   int4 NOT NULL,
    CONSTRAINT pk_cc_tags_id PRIMARY KEY (asset_id, tag_id),
    CONSTRAINT cc_tags_fk_ccs FOREIGN KEY (asset_id) REFERENCES ${ohdsiSchema}.cohort_characterization (id) ON DELETE CASCADE,
    CONSTRAINT cc_tags_fk_tags FOREIGN KEY (tag_id) REFERENCES ${ohdsiSchema}.tags (id) ON DELETE CASCADE
);

CREATE INDEX cc_tags_cc_id_idx ON ${ohdsiSchema}.cohort_characterization_tags USING btree (asset_id);
CREATE INDEX cc_tags_tag_id_idx ON ${ohdsiSchema}.cohort_characterization_tags USING btree (tag_id);

CREATE TABLE ${ohdsiSchema}.ir_tags
(
    asset_id int4 NOT NULL,
    tag_id   int4 NOT NULL,
    CONSTRAINT pk_ir_tags_id PRIMARY KEY (asset_id, tag_id),
    CONSTRAINT ir_tags_fk_irs FOREIGN KEY (asset_id) REFERENCES ${ohdsiSchema}.ir_analysis (id) ON DELETE CASCADE,
    CONSTRAINT ir_tags_fk_tags FOREIGN KEY (tag_id) REFERENCES ${ohdsiSchema}.tags (id) ON DELETE CASCADE
);

CREATE INDEX ir_tags_ir_id_idx ON ${ohdsiSchema}.ir_tags USING btree (asset_id);
CREATE INDEX ir_tags_tag_id_idx ON ${ohdsiSchema}.ir_tags USING btree (tag_id);

CREATE TABLE ${ohdsiSchema}.pathway_tags
(
    asset_id int4 NOT NULL,
    tag_id   int4 NOT NULL,
    CONSTRAINT pk_pathway_tags_id PRIMARY KEY (asset_id, tag_id),
    CONSTRAINT ir_tags_fk_irs FOREIGN KEY (asset_id) REFERENCES ${ohdsiSchema}.pathway_analysis (id) ON DELETE CASCADE,
    CONSTRAINT ir_tags_fk_tags FOREIGN KEY (tag_id) REFERENCES ${ohdsiSchema}.tags (id) ON DELETE CASCADE
);

CREATE INDEX pathway_tags_pathway_id_idx ON ${ohdsiSchema}.pathway_tags USING btree (asset_id);
CREATE INDEX pathway_tags_tag_id_idx ON ${ohdsiSchema}.pathway_tags USING btree (tag_id);

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
    (tag_id, group_id)
SELECT t1.id, t2.id
FROM ${ohdsiSchema}.tags t1,
     ${ohdsiSchema}.tags t2
WHERE t1.name = 'Tag 11'
  and t2.name = 'Tag group 1';

INSERT INTO ${ohdsiSchema}.tags
    (name, type)
VALUES ('Tag 12', 1);

INSERT INTO ${ohdsiSchema}.tag_groups
    (tag_id, group_id)
SELECT t1.id, t2.id
FROM ${ohdsiSchema}.tags t1,
     ${ohdsiSchema}.tags t2
WHERE t1.name = 'Tag 12'
  and t2.name = 'Tag group 1';

INSERT INTO ${ohdsiSchema}.tags
    (name, type)
VALUES ('Tag 21', 1);

INSERT INTO ${ohdsiSchema}.tag_groups
    (tag_id, group_id)
SELECT t1.id, t2.id
FROM ${ohdsiSchema}.tags t1,
     ${ohdsiSchema}.tags t2
WHERE t1.name = 'Tag 21'
  and t2.name = 'Tag group 1';

INSERT INTO ${ohdsiSchema}.tag_groups
    (tag_id, group_id)
SELECT t1.id, t2.id
FROM ${ohdsiSchema}.tags t1,
     ${ohdsiSchema}.tags t2
WHERE t1.name = 'Tag 21'
  and t2.name = 'Tag group 2';

INSERT INTO ${ohdsiSchema}.tags
    (name, type)
VALUES ('Tag 22', 1);

INSERT INTO ${ohdsiSchema}.tag_groups
    (tag_id, group_id)
SELECT t1.id, t2.id
FROM ${ohdsiSchema}.tags t1,
     ${ohdsiSchema}.tags t2
WHERE t1.name = 'Tag 22'
  and t2.name = 'Tag group 2';

INSERT INTO ${ohdsiSchema}.tags
    (name, type)
VALUES ('Tag 23', 1);

INSERT INTO ${ohdsiSchema}.tag_groups
    (tag_id, group_id)
SELECT t1.id, t2.id
FROM ${ohdsiSchema}.tags t1,
     ${ohdsiSchema}.tags t2
WHERE t1.name = 'Tag 23'
  and t2.name = 'Tag group 2';