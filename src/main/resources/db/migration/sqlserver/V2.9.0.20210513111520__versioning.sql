INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description)
VALUES 
    (NEXT VALUE for ${ohdsiSchema}.sec_permission_id_seq, 'cohortdefinition:*:version:get','Get list of cohort versions'),
    (NEXT VALUE for ${ohdsiSchema}.sec_permission_id_seq, 'cohortdefinition:*:version:*:get','Get cohort version'),
    (NEXT VALUE for ${ohdsiSchema}.sec_permission_id_seq, 'cohortdefinition:*:version:*:put','Update cohort version info'),
    (NEXT VALUE for ${ohdsiSchema}.sec_permission_id_seq, 'cohortdefinition:*:version:*:delete','Delete cohort version info'),
    (NEXT VALUE for ${ohdsiSchema}.sec_permission_id_seq, 'cohortdefinition:*:version:*:createAsset:put','Copy cohort version as new cohort'),
    (NEXT VALUE for ${ohdsiSchema}.sec_permission_id_seq, 'conceptset:*:version:get','Get list of concept set versions'),
    (NEXT VALUE for ${ohdsiSchema}.sec_permission_id_seq, 'conceptset:*:version:*:get','Get concept set version'),
    (NEXT VALUE for ${ohdsiSchema}.sec_permission_id_seq, 'conceptset:*:version:*:put','Update concept set version info'),
    (NEXT VALUE for ${ohdsiSchema}.sec_permission_id_seq, 'conceptset:*:version:*:delete','Delete concept set version info'),
    (NEXT VALUE for ${ohdsiSchema}.sec_permission_id_seq, 'conceptset:*:version:*:createAsset:put','Copy concept set version as new concept set'),
    (NEXT VALUE for ${ohdsiSchema}.sec_permission_id_seq, 'conceptset:*:version:*:expression:get','Get expression for concept set items for default source'),
    (NEXT VALUE for ${ohdsiSchema}.sec_permission_id_seq, 'conceptset:*:version:*:expression:*:get','Get expression for concept set items for certain source'),
    (NEXT VALUE for ${ohdsiSchema}.sec_permission_id_seq, 'cohort-characterization:*:version:get','Get list of characterization versions'),
    (NEXT VALUE for ${ohdsiSchema}.sec_permission_id_seq, 'cohort-characterization:*:version:*:get','Get characterization version'),
    (NEXT VALUE for ${ohdsiSchema}.sec_permission_id_seq, 'cohort-characterization:*:version:*:put','Update characterization version info'),
    (NEXT VALUE for ${ohdsiSchema}.sec_permission_id_seq, 'cohort-characterization:*:version:*:delete','Delete characterization version info'),
    (NEXT VALUE for ${ohdsiSchema}.sec_permission_id_seq, 'cohort-characterization:*:version:*:createAsset:put','Copy characterization version as new cohort'),
    (NEXT VALUE for ${ohdsiSchema}.sec_permission_id_seq, 'ir:*:version:get','Get list of incidence rate analysis versions'),
    (NEXT VALUE for ${ohdsiSchema}.sec_permission_id_seq, 'ir:*:version:*:get','Get incidence rate analysis version'),
    (NEXT VALUE for ${ohdsiSchema}.sec_permission_id_seq, 'ir:*:version:*:put','Update incidence rate analysis version info'),
    (NEXT VALUE for ${ohdsiSchema}.sec_permission_id_seq, 'ir:*:version:*:delete','Delete incidence rate analysis version info'),
    (NEXT VALUE for ${ohdsiSchema}.sec_permission_id_seq, 'ir:*:version:*:createAsset:put','Copy incidence rate analysis version as new cohort'),
    (NEXT VALUE for ${ohdsiSchema}.sec_permission_id_seq, 'pathway-analysis:*:version:get','Get list of pathway analysis versions'),
    (NEXT VALUE for ${ohdsiSchema}.sec_permission_id_seq, 'pathway-analysis:*:version:*:get','Get pathway analysis version'),
    (NEXT VALUE for ${ohdsiSchema}.sec_permission_id_seq, 'pathway-analysis:*:version:*:put','Update pathway analysis version info'),
    (NEXT VALUE for ${ohdsiSchema}.sec_permission_id_seq, 'pathway-analysis:*:version:*:delete','Delete pathway analysis version info'),
    (NEXT VALUE for ${ohdsiSchema}.sec_permission_id_seq, 'pathway-analysis:*:version:*:createAsset:put','Copy pathway analysis version as new cohort');

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
                   'cohort-characterization:*:version:*:createAsset:put',
                   'ir:*:version:get',
                   'ir:*:version:*:get',
                   'ir:*:version:*:put',
                   'ir:*:version:*:delete',
                   'ir:*:version:*:createAsset:put',
                   'pathway-analysis:*:version:get',
                   'pathway-analysis:*:version:*:get',
                   'pathway-analysis:*:version:*:put',
                   'pathway-analysis:*:version:*:delete',
                   'pathway-analysis:*:version:*:createAsset:put')
  AND sr.name IN ('Atlas users');

-- Cohorts
CREATE TABLE ${ohdsiSchema}.cohort_version
(
    asset_id      INT                     NOT NULL,
    comment       VARCHAR(MAX)                  NULL,
    description   VARCHAR(MAX)                  NULL,
    version       INT                     NOT NULL DEFAULT 1,
    asset_json    VARCHAR(MAX)                  NOT NULL,
    archived      BIT                     NOT NULL DEFAULT 0,
    created_by_id INTEGER,
    created_date  DATETIME  NOT NULL DEFAULT (SYSDATETIMEOFFSET()),
    CONSTRAINT pk_cohort_version_id PRIMARY KEY (asset_id, version),
    CONSTRAINT fk_cohort_version_sec_user_creator FOREIGN KEY (created_by_id) REFERENCES ${ohdsiSchema}.sec_user (id),
    CONSTRAINT fk_cohort_version_asset_id FOREIGN KEY (asset_id) REFERENCES ${ohdsiSchema}.cohort_definition (id) ON DELETE CASCADE
);

CREATE INDEX cohort_version_asset_idx ON ${ohdsiSchema}.cohort_version (asset_id);

-- Cohort characterizations
CREATE TABLE ${ohdsiSchema}.cohort_characterization_version
(
    asset_id      bigint                     NOT NULL,
    comment       VARCHAR(MAX)                  NULL,
    version       INT                     NOT NULL DEFAULT 1,
    asset_json    VARCHAR(MAX)                  NOT NULL,
    archived      BIT                     NOT NULL DEFAULT 0,
    created_by_id INTEGER,
    created_date  DATETIME NOT NULL DEFAULT (SYSDATETIMEOFFSET()),
    CONSTRAINT pk_cc_version_id PRIMARY KEY (asset_id, version),
    CONSTRAINT fk_cc_version_sec_user_creator FOREIGN KEY (created_by_id) REFERENCES ${ohdsiSchema}.sec_user (id),
    CONSTRAINT fk_cc_version_asset_id FOREIGN KEY (asset_id) REFERENCES ${ohdsiSchema}.cohort_characterization (id) ON DELETE CASCADE
);

CREATE INDEX cc_version_asset_idx ON ${ohdsiSchema}.cohort_characterization_version (asset_id);

-- Concept sets
CREATE TABLE ${ohdsiSchema}.concept_set_version
(
    asset_id      INT                     NOT NULL,
    comment       VARCHAR(MAX)                  NULL,
    version       INT                     NOT NULL DEFAULT 1,
    asset_json    VARCHAR(MAX)                  NOT NULL,
    archived      BIT                     NOT NULL DEFAULT 0,
    created_by_id INTEGER,
    created_date  DATETIME NOT NULL DEFAULT (SYSDATETIMEOFFSET()),
    CONSTRAINT pk_concept_set_version_id PRIMARY KEY (asset_id, version),
    CONSTRAINT fk_concept_set_version_sec_user_creator FOREIGN KEY (created_by_id) REFERENCES ${ohdsiSchema}.sec_user (id),
    CONSTRAINT fk_concept_set_version_asset_id FOREIGN KEY (asset_id) REFERENCES ${ohdsiSchema}.concept_set (concept_set_id) ON DELETE CASCADE
);

CREATE INDEX concept_set_version_asset_idx ON ${ohdsiSchema}.concept_set_version (asset_id);

-- Incidence rates
CREATE TABLE ${ohdsiSchema}.ir_version
(
    asset_id      INT                     NOT NULL,
    comment       VARCHAR(MAX)                  NULL,
    description   VARCHAR(MAX)                  NULL,
    version       INT                     NOT NULL DEFAULT 1,
    asset_json    VARCHAR(MAX)                  NOT NULL,
    archived      BIT                     NOT NULL DEFAULT 0,
    created_by_id INTEGER,
    created_date  DATETIME NOT NULL DEFAULT (SYSDATETIMEOFFSET()),
    CONSTRAINT pk_ir_version_id PRIMARY KEY (asset_id, version),
    CONSTRAINT fk_ir_version_sec_user_creator FOREIGN KEY (created_by_id) REFERENCES ${ohdsiSchema}.sec_user (id),
    CONSTRAINT fk_ir_version_asset_id FOREIGN KEY (asset_id) REFERENCES ${ohdsiSchema}.ir_analysis (id) ON DELETE CASCADE
);

CREATE INDEX ir_version_asset_idx ON ${ohdsiSchema}.ir_version (asset_id);

-- Pathways
CREATE TABLE ${ohdsiSchema}.pathway_version
(
    asset_id      INT                     NOT NULL,
    comment       VARCHAR(MAX)                  NULL,
    version       INT                     NOT NULL DEFAULT 1,
    asset_json    VARCHAR(MAX)                  NOT NULL,
    archived      BIT                     NOT NULL DEFAULT 0,
    created_by_id INTEGER,
    created_date  DATETIME NOT NULL DEFAULT (SYSDATETIMEOFFSET()),
    CONSTRAINT pk_pathway_version_id PRIMARY KEY (asset_id, version),
    CONSTRAINT fk_pathway_version_sec_user_creator FOREIGN KEY (created_by_id) REFERENCES ${ohdsiSchema}.sec_user (id),
    CONSTRAINT fk_pathway_version_asset_id FOREIGN KEY (asset_id) REFERENCES ${ohdsiSchema}.pathway_analysis (id) ON DELETE CASCADE
);

CREATE INDEX pathway_version_asset_idx ON ${ohdsiSchema}.pathway_version (asset_id);
