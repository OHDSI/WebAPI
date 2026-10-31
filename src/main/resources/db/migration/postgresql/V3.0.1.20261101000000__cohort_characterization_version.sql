-- The B2.99.0006 baseline omits cohort_characterization_version, so on a fresh install every
-- characterization update fails while saving the previous version. Databases upgraded from 2.x
-- already have it from V2.9.0.20210513111520, hence IF NOT EXISTS.
CREATE TABLE IF NOT EXISTS ${ohdsiSchema}.cohort_characterization_version (
    asset_id bigint NOT NULL,
    comment character varying,
    version integer DEFAULT 1 NOT NULL,
    asset_json character varying NOT NULL,
    archived boolean DEFAULT false NOT NULL,
    created_by_id integer,
    created_date timestamp with time zone DEFAULT now() NOT NULL,
    CONSTRAINT pk_cc_version_id PRIMARY KEY (asset_id, version),
    CONSTRAINT fk_cc_version_sec_user_creator FOREIGN KEY (created_by_id) REFERENCES ${ohdsiSchema}.sec_user (id),
    CONSTRAINT fk_cc_version_asset_id FOREIGN KEY (asset_id) REFERENCES ${ohdsiSchema}.cohort_characterization (id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS cc_version_asset_idx ON ${ohdsiSchema}.cohort_characterization_version USING btree (asset_id);
