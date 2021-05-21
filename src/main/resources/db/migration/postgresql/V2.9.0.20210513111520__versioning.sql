INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description)
VALUES (NEXTVAL('${ohdsiSchema}.sec_permission_id_seq'), 'cohortdefinition:*:version:get',
        'Get list of cohort versions'),
       (NEXTVAL('${ohdsiSchema}.sec_permission_id_seq'), 'cohortdefinition:*:version:*:get',
        'Get cohort version'),
       (NEXTVAL('${ohdsiSchema}.sec_permission_id_seq'), 'cohortdefinition:*:version:*:put',
        'Update cohort version info'),
       (NEXTVAL('${ohdsiSchema}.sec_permission_id_seq'), 'cohortdefinition:*:version:*:delete',
        'Delete cohort version info'),
       (NEXTVAL('${ohdsiSchema}.sec_permission_id_seq'), 'cohortdefinition:*:version:*:createAsset:put',
        'Copy cohort version as new cohort'),
       (NEXTVAL('${ohdsiSchema}.sec_permission_id_seq'), 'conceptset:*:version:get',
        'Get list of concept set versions'),
       (NEXTVAL('${ohdsiSchema}.sec_permission_id_seq'), 'conceptset:*:version:*:get',
        'Get concept set version'),
       (NEXTVAL('${ohdsiSchema}.sec_permission_id_seq'), 'conceptset:*:version:*:put',
        'Update concept set version info'),
       (NEXTVAL('${ohdsiSchema}.sec_permission_id_seq'), 'conceptset:*:version:*:delete',
        'Delete concept set version info'),
       (NEXTVAL('${ohdsiSchema}.sec_permission_id_seq'), 'conceptset:*:version:*:createAsset:put',
        'Copy concept set version as new concept set'),
       (NEXTVAL('${ohdsiSchema}.sec_permission_id_seq'), 'conceptset:*:version:*:expression:get',
        'Get expression for concept set items for default source'),
       (NEXTVAL('${ohdsiSchema}.sec_permission_id_seq'), 'conceptset:*:version:*:expression:*:get',
        'Get expression for concept set items for certain source');

INSERT INTO ${ohdsiSchema}.sec_role_permission(role_id, permission_id)
SELECT sr.id, sp.id
FROM ${ohdsiSchema}.sec_permission SP,
     ${ohdsiSchema}.sec_role sr
WHERE sp.value IN (
                   'cohortdefinition:*:version:get',
                   'cohortdefinition:*:version:*:get',
                   'cohortdefinition:*:version:*:put',
                   'cohortdefinition:*:version:*:delete',
                   'cohortdefinition:*:version:*:createAsset:put',
                   'conceptset:*:version:get',
                   'conceptset:*:version:*:get',
                   'conceptset:*:version:*:put',
                   'conceptset:*:version:*:delete',
                   'conceptset:*:version:*:createAsset:put',
                   'conceptset:*:version:*:expression:get',
                   'conceptset:*:version:*:expression:*:get')
  AND sr.name IN ('Atlas users');

CREATE SEQUENCE ${ohdsiSchema}.cohort_version_seq;
CREATE SEQUENCE ${ohdsiSchema}.cc_version_seq;
CREATE SEQUENCE ${ohdsiSchema}.concept_set_version_seq;
CREATE SEQUENCE ${ohdsiSchema}.ir_version_seq;
CREATE SEQUENCE ${ohdsiSchema}.pathway_version_seq;

-- Cohorts
CREATE TABLE ${ohdsiSchema}.cohort_versions
(
    id            int8                     NOT NULL DEFAULT nextval('${ohdsiSchema}.cohort_version_seq'),
    asset_id      int4                     NOT NULL,
    comment       varchar                  NULL,
    description   varchar                  NULL,
    version       int4                     NOT NULL DEFAULT 1,
    asset_json    varchar                  NOT NULL,
    archived      bool                     NOT NULL DEFAULT FALSE,
    created_by_id INTEGER,
    created_date  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT (now()),
    CONSTRAINT cohort_versions_un UNIQUE (asset_id, version),
    CONSTRAINT pk_cohort_versions_id PRIMARY KEY (id),
    CONSTRAINT fk_cohort_versions_sec_user_creator FOREIGN KEY (created_by_id) REFERENCES ${ohdsiSchema}.sec_user (id)
);

CREATE INDEX cohort_versions_asset_idx ON ${ohdsiSchema}.cohort_versions USING btree (asset_id);

-- Cohort characterizations
CREATE TABLE ${ohdsiSchema}.cohort_characterization_versions
(
    id            int8                     NOT NULL DEFAULT nextval('${ohdsiSchema}.cc_version_seq'),
    asset_id      int4                     NOT NULL,
    comment       varchar                  NULL,
    version       int4                     NOT NULL DEFAULT 1,
    asset_json    varchar                  NOT NULL,
    archived      bool                     NOT NULL DEFAULT FALSE,
    created_by_id INTEGER,
    created_date  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT (now()),
    CONSTRAINT cohort_characterization_versions_un UNIQUE (asset_id, version),
    CONSTRAINT pk_cc_versions_id PRIMARY KEY (id),
    CONSTRAINT fk_cc_versions_sec_user_creator FOREIGN KEY (created_by_id) REFERENCES ${ohdsiSchema}.sec_user (id)
);

CREATE INDEX cc_versions_asset_idx ON ${ohdsiSchema}.cohort_characterization_versions USING btree (asset_id);

-- Concept sets
CREATE TABLE ${ohdsiSchema}.concept_set_versions
(
    id            int8                     NOT NULL DEFAULT nextval('${ohdsiSchema}.concept_set_version_seq'),
    asset_id      int4                     NOT NULL,
    comment       varchar                  NULL,
    version       int4                     NOT NULL DEFAULT 1,
    asset_json    varchar                  NOT NULL,
    archived      bool                     NOT NULL DEFAULT FALSE,
    created_by_id INTEGER,
    created_date  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT (now()),
    CONSTRAINT concept_set_versions_un UNIQUE (asset_id, version),
    CONSTRAINT pk_concept_set_versions_id PRIMARY KEY (id),
    CONSTRAINT fk_concept_set_versions_sec_user_creator FOREIGN KEY (created_by_id) REFERENCES ${ohdsiSchema}.sec_user (id)
);

CREATE INDEX concept_set_versions_asset_idx ON ${ohdsiSchema}.concept_set_versions USING btree (asset_id);

-- Incidence rates
CREATE TABLE ${ohdsiSchema}.ir_versions
(
    id            int8                     NOT NULL DEFAULT nextval('${ohdsiSchema}.ir_version_seq'),
    asset_id      int4                     NOT NULL,
    comment       varchar                  NULL,
    version       int4                     NOT NULL DEFAULT 1,
    asset_json    varchar                  NOT NULL,
    archived      bool                     NOT NULL DEFAULT FALSE,
    created_by_id INTEGER,
    created_date  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT (now()),
    CONSTRAINT ir_versions_un UNIQUE (asset_id, version),
    CONSTRAINT pk_ir_versions_id PRIMARY KEY (id),
    CONSTRAINT fk_ir_versions_sec_user_creator FOREIGN KEY (created_by_id) REFERENCES ${ohdsiSchema}.sec_user (id)
);

CREATE INDEX ir_versions_asset_idx ON ${ohdsiSchema}.ir_versions USING btree (asset_id);

-- Pathways
CREATE TABLE ${ohdsiSchema}.pathway_versions
(
    id            int8                     NOT NULL DEFAULT nextval('${ohdsiSchema}.pathway_version_seq'),
    asset_id      int4                     NOT NULL,
    comment       varchar                  NULL,
    version       int4                     NOT NULL DEFAULT 1,
    asset_json    varchar                  NOT NULL,
    archived      bool                     NOT NULL DEFAULT FALSE,
    created_by_id INTEGER,
    created_date  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT (now()),
    CONSTRAINT pathway_versions_un UNIQUE (asset_id, version),
    CONSTRAINT pk_pathway_versions_id PRIMARY KEY (id),
    CONSTRAINT fk_pathway_versions_sec_user_creator FOREIGN KEY (created_by_id) REFERENCES ${ohdsiSchema}.sec_user (id)
);

CREATE INDEX pathway_versions_asset_idx ON ${ohdsiSchema}.pathway_versions USING btree (asset_id);