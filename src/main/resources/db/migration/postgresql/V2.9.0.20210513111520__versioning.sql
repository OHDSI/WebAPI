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
        'Get expression for concept set items for certain source'),
       (NEXTVAL('${ohdsiSchema}.sec_permission_id_seq'), 'cohort-characterization:*:version:get',
        'Get list of characterization versions'),
       (NEXTVAL('${ohdsiSchema}.sec_permission_id_seq'), 'cohort-characterization:*:version:*:get',
        'Get characterization version'),
       (NEXTVAL('${ohdsiSchema}.sec_permission_id_seq'), 'cohort-characterization:*:version:*:put',
        'Update characterization version info'),
       (NEXTVAL('${ohdsiSchema}.sec_permission_id_seq'), 'cohort-characterization:*:version:*:delete',
        'Delete characterization version info'),
       (NEXTVAL('${ohdsiSchema}.sec_permission_id_seq'), 'cohort-characterization:*:version:*:createAsset:put',
        'Copy characterization version as new cohort');

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
                   'conceptset:*:version:*:expression:*:get',
                   'cohort-characterization:*:version:get',
                   'cohort-characterization:*:version:*:get',
                   'cohort-characterization:*:version:*:put',
                   'cohort-characterization:*:version:*:delete',
                   'cohort-characterization:*:version:*:createAsset:put')
  AND sr.name IN ('Atlas users');

-- Cohorts
CREATE TABLE ${ohdsiSchema}.cohort_versions
(
    asset_id      int8                     NOT NULL,
    comment       varchar                  NULL,
    description   varchar                  NULL,
    version       int4                     NOT NULL DEFAULT 1,
    asset_json    varchar                  NOT NULL,
    archived      bool                     NOT NULL DEFAULT FALSE,
    created_by_id INTEGER,
    created_date  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT (now()),
    CONSTRAINT pk_cohort_versions_id PRIMARY KEY (asset_id, version),
    CONSTRAINT fk_cohort_versions_sec_user_creator FOREIGN KEY (created_by_id) REFERENCES ${ohdsiSchema}.sec_user (id)
);

CREATE INDEX cohort_versions_asset_idx ON ${ohdsiSchema}.cohort_versions USING btree (asset_id);

-- Cohort characterizations
CREATE TABLE ${ohdsiSchema}.cohort_characterization_versions
(
    asset_id      int8                     NOT NULL,
    comment       varchar                  NULL,
    version       int4                     NOT NULL DEFAULT 1,
    asset_json    varchar                  NOT NULL,
    archived      bool                     NOT NULL DEFAULT FALSE,
    created_by_id INTEGER,
    created_date  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT (now()),
    CONSTRAINT pk_cc_versions_id PRIMARY KEY (asset_id, version),
    CONSTRAINT fk_cc_versions_sec_user_creator FOREIGN KEY (created_by_id) REFERENCES ${ohdsiSchema}.sec_user (id)
);

CREATE INDEX cc_versions_asset_idx ON ${ohdsiSchema}.cohort_characterization_versions USING btree (asset_id);

-- Concept sets
CREATE TABLE ${ohdsiSchema}.concept_set_versions
(
    asset_id      int8                     NOT NULL,
    comment       varchar                  NULL,
    version       int4                     NOT NULL DEFAULT 1,
    asset_json    varchar                  NOT NULL,
    archived      bool                     NOT NULL DEFAULT FALSE,
    created_by_id INTEGER,
    created_date  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT (now()),
    CONSTRAINT pk_concept_set_versions_id PRIMARY KEY (asset_id, version),
    CONSTRAINT fk_concept_set_versions_sec_user_creator FOREIGN KEY (created_by_id) REFERENCES ${ohdsiSchema}.sec_user (id)
);

CREATE INDEX concept_set_versions_asset_idx ON ${ohdsiSchema}.concept_set_versions USING btree (asset_id);

-- Incidence rates
CREATE TABLE ${ohdsiSchema}.ir_versions
(
    asset_id      int8                     NOT NULL,
    comment       varchar                  NULL,
    version       int4                     NOT NULL DEFAULT 1,
    asset_json    varchar                  NOT NULL,
    archived      bool                     NOT NULL DEFAULT FALSE,
    created_by_id INTEGER,
    created_date  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT (now()),
    CONSTRAINT pk_ir_versions_id PRIMARY KEY (asset_id, version),
    CONSTRAINT fk_ir_versions_sec_user_creator FOREIGN KEY (created_by_id) REFERENCES ${ohdsiSchema}.sec_user (id)
);

CREATE INDEX ir_versions_asset_idx ON ${ohdsiSchema}.ir_versions USING btree (asset_id);

-- Pathways
CREATE TABLE ${ohdsiSchema}.pathway_versions
(
    asset_id      int8                     NOT NULL,
    comment       varchar                  NULL,
    version       int4                     NOT NULL DEFAULT 1,
    asset_json    varchar                  NOT NULL,
    archived      bool                     NOT NULL DEFAULT FALSE,
    created_by_id INTEGER,
    created_date  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT (now()),
    CONSTRAINT pk_pathway_versions_id PRIMARY KEY (asset_id, version),
    CONSTRAINT fk_pathway_versions_sec_user_creator FOREIGN KEY (created_by_id) REFERENCES ${ohdsiSchema}.sec_user (id)
);

CREATE INDEX pathway_versions_asset_idx ON ${ohdsiSchema}.pathway_versions USING btree (asset_id);