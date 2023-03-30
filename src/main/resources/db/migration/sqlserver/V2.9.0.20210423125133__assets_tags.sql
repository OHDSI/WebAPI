INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description) 
    VALUES 
        (NEXT VALUE FOR ${ohdsiSchema}.sec_permission_id_seq, 'tag:get', 'List tags'),
        (NEXT VALUE FOR ${ohdsiSchema}.sec_permission_id_seq, 'tag:search:get', 'Search tags by name'),
        (NEXT VALUE FOR ${ohdsiSchema}.sec_permission_id_seq, 'tag:post', 'Create tag'),
        (NEXT VALUE FOR ${ohdsiSchema}.sec_permission_id_seq, 'tag:*:put', 'Update tag'),
        (NEXT VALUE FOR ${ohdsiSchema}.sec_permission_id_seq, 'tag:*:get', 'Get tag'),
        (NEXT VALUE FOR ${ohdsiSchema}.sec_permission_id_seq, 'tag:*:delete', 'Delete tag'),
        (NEXT VALUE FOR ${ohdsiSchema}.sec_permission_id_seq, 'cohortdefinition:*:tag:post', 'Assign tag to cohort definition'),
        (NEXT VALUE FOR ${ohdsiSchema}.sec_permission_id_seq, 'cohortdefinition:*:tag:*:delete', 'Unassign tag from cohort definition'),
        (NEXT VALUE FOR ${ohdsiSchema}.sec_permission_id_seq, 'cohortdefinition:*:protectedtag:post','Assign tag to cohort definition'),
        (NEXT VALUE FOR ${ohdsiSchema}.sec_permission_id_seq, 'cohortdefinition:*:protectedtag:*:delete','Unassign tag from cohort definition'),
        (NEXT VALUE FOR ${ohdsiSchema}.sec_permission_id_seq, 'conceptset:*:tag:post','Assign tag to cohort definition'),
        (NEXT VALUE FOR ${ohdsiSchema}.sec_permission_id_seq, 'conceptset:*:tag:*:delete','Unassign tag from cohort definition'),
        (NEXT VALUE FOR ${ohdsiSchema}.sec_permission_id_seq, 'conceptset:*:protectedtag:post','Assign tag to cohort definition'),
        (NEXT VALUE FOR ${ohdsiSchema}.sec_permission_id_seq, 'conceptset:*:protectedtag:*:delete','Unassign tag from cohort definition'),
        (NEXT VALUE FOR ${ohdsiSchema}.sec_permission_id_seq, 'conceptset:check:post','Unassign tag from cohort definition'),
        (NEXT VALUE FOR ${ohdsiSchema}.sec_permission_id_seq, 'cohort-characterization:*:tag:post','Assign tag to cohort definition'),
        (NEXT VALUE FOR ${ohdsiSchema}.sec_permission_id_seq, 'cohort-characterization:*:tag:*:delete','Unassign tag from cohort definition'),
        (NEXT VALUE FOR ${ohdsiSchema}.sec_permission_id_seq, 'cohort-characterization:*:protectedtag:post','Assign tag to cohort definition'),
        (NEXT VALUE FOR ${ohdsiSchema}.sec_permission_id_seq, 'cohort-characterization:*:protectedtag:*:delete','Unassign tag from cohort definition'),
        (NEXT VALUE FOR ${ohdsiSchema}.sec_permission_id_seq, 'ir:*:tag:post','Assign tag to cohort definition'),
        (NEXT VALUE FOR ${ohdsiSchema}.sec_permission_id_seq, 'ir:*:tag:*:delete','Unassign tag from cohort definition'),
        (NEXT VALUE FOR ${ohdsiSchema}.sec_permission_id_seq, 'ir:*:protectedtag:post','Assign tag to cohort definition'),
        (NEXT VALUE FOR ${ohdsiSchema}.sec_permission_id_seq, 'ir:*:protectedtag:*:delete','Unassign tag from cohort definition'),
        (NEXT VALUE FOR ${ohdsiSchema}.sec_permission_id_seq, 'pathway-analysis:*:tag:post','Assign tag to cohort definition'),
        (NEXT VALUE FOR ${ohdsiSchema}.sec_permission_id_seq, 'pathway-analysis:*:tag:*:delete','Unassign tag from cohort definition'),
        (NEXT VALUE FOR ${ohdsiSchema}.sec_permission_id_seq, 'pathway-analysis:*:protectedtag:post','Assign tag to cohort definition'),
        (NEXT VALUE FOR ${ohdsiSchema}.sec_permission_id_seq, 'pathway-analysis:*:protectedtag:*:delete','Unassign tag from cohort definition')
;


INSERT INTO ${ohdsiSchema}.sec_role_permission(role_id, permission_id)
SELECT sr.id, sp.id
FROM ${ohdsiSchema}.sec_permission sp,
     ${ohdsiSchema}.sec_role sr
WHERE sp.value IN (
                   'tag:get',
                   'tag:search:get',
                   'tag:post',
                   'tag:*:put',
                   'tag:*:get',
                   'tag:*:delete',
                   'cohortdefinition:*:tag:post',
                   'cohortdefinition:*:tag:*:delete',
                   'conceptset:*:tag:post',
                   'conceptset:*:tag:*:delete',
                   'conceptset:check:post',
                   'cohort-characterization:*:tag:post',
                   'cohort-characterization:*:tag:*:delete',
                   'ir:*:tag:post',
                   'ir:*:tag:*:delete',
                   'pathway-analysis:*:tag:post',
                   'pathway-analysis:*:tag:*:delete')
  AND sr.name IN ('Atlas users');

INSERT INTO ${ohdsiSchema}.sec_role_permission(role_id, permission_id)
SELECT sr.id, sp.id
FROM ${ohdsiSchema}.sec_permission sp,
     ${ohdsiSchema}.sec_role sr
WHERE sp.value IN (
                   'cohortdefinition:*:protectedtag:post',
                   'cohortdefinition:*:protectedtag:*:delete',
                   'conceptset:*:protectedtag:post',
                   'conceptset:*:protectedtag:*:delete',
                   'cohort-characterization:*:protectedtag:post',
                   'cohort-characterization:*:protectedtag:*:delete',
                   'ir:*:protectedtag:post',
                   'ir:*:protectedtag:*:delete',
                   'pathway-analysis:*:protectedtag:post',
                   'pathway-analysis:*:protectedtag:*:delete')
  AND sr.name IN ('admin');


CREATE SEQUENCE ${ohdsiSchema}.tags_seq;

-- Possible types are:
-- 0 - System (predefined) tags
-- 1 - Custom tags
-- 2 - Prizm tags
CREATE TABLE ${ohdsiSchema}.tags
(
    id                      INTEGER DEFAULT NEXT VALUE FOR ${ohdsiSchema}.tags_seq,
    name                 VARCHAR(50)                  NOT NULL,
    type                 INT                     NOT NULL DEFAULT 0,
    count                INT                     NOT NULL DEFAULT 0,
    show_group           BIT                     NOT NULL DEFAULT 0,
    icon                 VARCHAR(100)                  NULL,
    color                VARCHAR(7)                  NULL,
    multi_selection      BIT                     NOT NULL DEFAULT 0,
    permission_protected BIT                     NOT NULL DEFAULT 0,
    mandatory            BIT                     NOT NULL DEFAULT 0,
    allow_custom         BIT                     NOT NULL DEFAULT 0,
    description          VARCHAR(MAX)                  NULL,
    created_by_id        INTEGER,
    created_date         DATETIME NOT NULL DEFAULT SYSDATETIMEOFFSET(),
    modified_by_id       INTEGER,
    modified_date        DATETIME,
    CONSTRAINT pk_tags_id PRIMARY KEY (id),
    CONSTRAINT fk_tags_sec_user_creator FOREIGN KEY (created_by_id) REFERENCES ${ohdsiSchema}.sec_user (id),
    CONSTRAINT fk_tags_sec_user_updater FOREIGN KEY (modified_by_id) REFERENCES ${ohdsiSchema}.sec_user (id)
);

CREATE UNIQUE INDEX tags_name_idx ON ${ohdsiSchema}.tags (name);

CREATE TABLE ${ohdsiSchema}.tag_groups
(
    tag_id   INT NOT NULL,
    group_id INT NOT NULL,
    CONSTRAINT tag_groups_group_fk FOREIGN KEY (group_id) REFERENCES ${ohdsiSchema}.tags (id) ON DELETE CASCADE,
    CONSTRAINT tag_groups_tag_fk FOREIGN KEY (tag_id) REFERENCES ${ohdsiSchema}.tags (id) ON DELETE CASCADE
);

CREATE TABLE ${ohdsiSchema}.concept_set_tags
(
    asset_id INT NOT NULL,
    tag_id   INT NOT NULL,
    CONSTRAINT pk_concept_set_tags_id PRIMARY KEY (asset_id, tag_id),
    CONSTRAINT concept_set_tags_fk_sets FOREIGN KEY (asset_id) REFERENCES ${ohdsiSchema}.concept_set (concept_set_id) ON DELETE NO ACTION, --CHANGE ON DELETE ACTION BECAUSE OF ERROR
    CONSTRAINT concept_set_tags_fk_tags FOREIGN KEY (tag_id) REFERENCES ${ohdsiSchema}.tags (id) ON DELETE CASCADE
);

CREATE INDEX concept_set_tags_concept_id_idx ON ${ohdsiSchema}.concept_set_tags (asset_id);
CREATE INDEX concept_set_tags_tag_id_idx ON ${ohdsiSchema}.concept_set_tags (tag_id);

CREATE TABLE ${ohdsiSchema}.cohort_tags
(
    asset_id INT NOT NULL,
    tag_id   INT NOT NULL,
    CONSTRAINT pk_cohort_tags_id PRIMARY KEY (asset_id, tag_id),
    CONSTRAINT cohort_tags_fk_definitions FOREIGN KEY (asset_id) REFERENCES ${ohdsiSchema}.cohort_definition (id) ON DELETE CASCADE,
    CONSTRAINT cohort_tags_fk_tags FOREIGN KEY (tag_id) REFERENCES ${ohdsiSchema}.tags (id) ON DELETE CASCADE
);

CREATE INDEX cohort_tags_cohort_id_idx ON ${ohdsiSchema}.cohort_tags (asset_id);
CREATE INDEX cohort_tags_tag_id_idx ON ${ohdsiSchema}.cohort_tags (tag_id);

CREATE TABLE ${ohdsiSchema}.cohort_characterization_tags
(
    asset_id bigint NOT NULL,
    tag_id   INT NOT NULL,
    CONSTRAINT pk_cc_tags_id PRIMARY KEY (asset_id, tag_id),
    CONSTRAINT cc_tags_fk_ccs FOREIGN KEY (asset_id) REFERENCES ${ohdsiSchema}.cohort_characterization (id) ON DELETE CASCADE,
    CONSTRAINT cc_tags_fk_tags FOREIGN KEY (tag_id) REFERENCES ${ohdsiSchema}.tags (id) ON DELETE CASCADE
);

CREATE INDEX cc_tags_cc_id_idx ON ${ohdsiSchema}.cohort_characterization_tags (asset_id);
CREATE INDEX cc_tags_tag_id_idx ON ${ohdsiSchema}.cohort_characterization_tags (tag_id);

CREATE TABLE ${ohdsiSchema}.ir_tags
(
    asset_id INT NOT NULL,
    tag_id   INT NOT NULL,
    CONSTRAINT pk_ir_tags_id PRIMARY KEY (asset_id, tag_id),
    CONSTRAINT ir_tags_fk_irs FOREIGN KEY (asset_id) REFERENCES ${ohdsiSchema}.ir_analysis (id) ON DELETE CASCADE,
    CONSTRAINT ir_tags_fk_tags FOREIGN KEY (tag_id) REFERENCES ${ohdsiSchema}.tags (id) ON DELETE CASCADE
);

CREATE INDEX ir_tags_ir_id_idx ON ${ohdsiSchema}.ir_tags (asset_id);
CREATE INDEX ir_tags_tag_id_idx ON ${ohdsiSchema}.ir_tags (tag_id);

CREATE TABLE ${ohdsiSchema}.pathway_tags
(
    asset_id INT NOT NULL,
    tag_id   INT NOT NULL,
    CONSTRAINT pk_pathway_tags_id PRIMARY KEY (asset_id, tag_id),
    CONSTRAINT pathway_tags_fk_irs FOREIGN KEY (asset_id) REFERENCES ${ohdsiSchema}.pathway_analysis (id) ON DELETE CASCADE,
    CONSTRAINT pathway_tags_fk_tags FOREIGN KEY (tag_id) REFERENCES ${ohdsiSchema}.tags (id) ON DELETE CASCADE
);

CREATE INDEX pathway_tags_pathway_id_idx ON ${ohdsiSchema}.pathway_tags (asset_id);
CREATE INDEX pathway_tags_tag_id_idx ON ${ohdsiSchema}.pathway_tags (tag_id);