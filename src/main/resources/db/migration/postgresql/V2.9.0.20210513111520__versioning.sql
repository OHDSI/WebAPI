CREATE SEQUENCE ${ohdsiSchema}.cohort_version_seq;
CREATE SEQUENCE ${ohdsiSchema}.cc_version_seq;
CREATE SEQUENCE ${ohdsiSchema}.concept_set_version_seq;
CREATE SEQUENCE ${ohdsiSchema}.ir_version_seq;
CREATE SEQUENCE ${ohdsiSchema}.pathway_version_seq;

-- Cohorts
CREATE TABLE ${ohdsiSchema}.cohort_versions
(
    id             int8                     NOT NULL DEFAULT nextval('${ohdsiSchema}.cohort_version_seq'),
    asset_id       int4                     NOT NULL,
    name           varchar                  NULL,
    version        int4                     NOT NULL DEFAULT 1,
    asset_json     varchar                  NOT NULL,
    archived       bool                     NOT NULL DEFAULT FALSE,
    created_by_id  INTEGER,
    created_date   TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT (now()),
    modified_by_id INTEGER,
    modified_date  TIMESTAMP WITH TIME ZONE,
    CONSTRAINT cohort_versions_un UNIQUE (asset_id, version),
    CONSTRAINT pk_cohort_versions_id PRIMARY KEY (id),
    CONSTRAINT fk_cohort_versions_sec_user_creator FOREIGN KEY (created_by_id) REFERENCES ${ohdsiSchema}.sec_user (id)
);

CREATE UNIQUE INDEX cohort_versions_asset_version_idx ON ${ohdsiSchema}.cohort_versions USING btree (asset_id, version);
CREATE INDEX cohort_versions_asset_idx ON ${ohdsiSchema}.cohort_versions USING btree (asset_id);

-- Cohort characterizations
CREATE TABLE ${ohdsiSchema}.cohort_characterization_versions
(
    id             int8                     NOT NULL DEFAULT nextval('${ohdsiSchema}.cc_version_seq'),
    asset_id       int4                     NOT NULL,
    name           varchar                  NULL,
    version        int4                     NOT NULL DEFAULT 1,
    asset_json     varchar                  NOT NULL,
    archived       bool                     NOT NULL DEFAULT FALSE,
    created_by_id  INTEGER,
    created_date   TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT (now()),
    modified_by_id INTEGER,
    modified_date  TIMESTAMP WITH TIME ZONE,
    CONSTRAINT cohort_characterization_versions_un UNIQUE (asset_id, version),
    CONSTRAINT pk_cc_versions_id PRIMARY KEY (id),
    CONSTRAINT fk_cc_versions_sec_user_creator FOREIGN KEY (created_by_id) REFERENCES ${ohdsiSchema}.sec_user (id)
);

CREATE UNIQUE INDEX cc_versions_asset_version_idx ON ${ohdsiSchema}.cohort_characterization_versions USING btree (asset_id, version);
CREATE INDEX cc_versions_asset_idx ON ${ohdsiSchema}.cohort_characterization_versions USING btree (asset_id);

-- Concept sets
CREATE TABLE ${ohdsiSchema}.concept_set_versions
(
    id             int8                     NOT NULL DEFAULT nextval('${ohdsiSchema}.concept_set_version_seq'),
    asset_id       int4                     NOT NULL,
    name           varchar                  NULL,
    version        int4                     NOT NULL DEFAULT 1,
    asset_json     varchar                  NOT NULL,
    archived       bool                     NOT NULL DEFAULT FALSE,
    created_by_id  INTEGER,
    created_date   TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT (now()),
    modified_by_id INTEGER,
    modified_date  TIMESTAMP WITH TIME ZONE,
    CONSTRAINT concept_set_versions_un UNIQUE (asset_id, version),
    CONSTRAINT pk_concept_set_versions_id PRIMARY KEY (id),
    CONSTRAINT fk_concept_set_versions_sec_user_creator FOREIGN KEY (created_by_id) REFERENCES ${ohdsiSchema}.sec_user (id)
);

CREATE UNIQUE INDEX concept_set_versions_asset_version_idx ON ${ohdsiSchema}.concept_set_versions USING btree (asset_id, version);
CREATE INDEX concept_set_versions_asset_idx ON ${ohdsiSchema}.concept_set_versions USING btree (asset_id);

-- Incidence rates
CREATE TABLE ${ohdsiSchema}.ir_versions
(
    id             int8                     NOT NULL DEFAULT nextval('${ohdsiSchema}.ir_version_seq'),
    asset_id       int4                     NOT NULL,
    name           varchar                  NULL,
    version        int4                     NOT NULL DEFAULT 1,
    asset_json     varchar                  NOT NULL,
    archived       bool                     NOT NULL DEFAULT FALSE,
    created_by_id  INTEGER,
    created_date   TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT (now()),
    modified_by_id INTEGER,
    modified_date  TIMESTAMP WITH TIME ZONE,
    CONSTRAINT ir_versions_un UNIQUE (asset_id, version),
    CONSTRAINT pk_ir_versions_id PRIMARY KEY (id),
    CONSTRAINT fk_ir_versions_sec_user_creator FOREIGN KEY (created_by_id) REFERENCES ${ohdsiSchema}.sec_user (id)
);

CREATE UNIQUE INDEX ir_versions_asset_version_idx ON ${ohdsiSchema}.ir_versions USING btree (asset_id, version);
CREATE INDEX ir_versions_asset_idx ON ${ohdsiSchema}.ir_versions USING btree (asset_id);

-- Pathways
CREATE TABLE ${ohdsiSchema}.pathway_versions
(
    id             int8                     NOT NULL DEFAULT nextval('${ohdsiSchema}.pathway_version_seq'),
    asset_id       int4                     NOT NULL,
    name           varchar                  NULL,
    version        int4                     NOT NULL DEFAULT 1,
    asset_json     varchar                  NOT NULL,
    archived       bool                     NOT NULL DEFAULT FALSE,
    created_by_id  INTEGER,
    created_date   TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT (now()),
    modified_by_id INTEGER,
    modified_date  TIMESTAMP WITH TIME ZONE,
    CONSTRAINT pathway_versions_un UNIQUE (asset_id, version),
    CONSTRAINT pk_pathway_versions_id PRIMARY KEY (id),
    CONSTRAINT fk_pathway_versions_sec_user_creator FOREIGN KEY (created_by_id) REFERENCES ${ohdsiSchema}.sec_user (id)
);

CREATE UNIQUE INDEX pathway_versions_asset_version_idx ON ${ohdsiSchema}.pathway_versions USING btree (asset_id, version);
CREATE INDEX pathway_versions_asset_idx ON ${ohdsiSchema}.pathway_versions USING btree (asset_id);