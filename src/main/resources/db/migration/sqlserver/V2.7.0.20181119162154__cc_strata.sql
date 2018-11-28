CREATE SEQUENCE ${ohdsiSchema}.cc_strata_seq START WITH 1;

CREATE TABLE ${ohdsiSchema}.cc_strata(
  id BIGINT NOT NULL,
  cohort_characterization_id BIGINT NOT NULL,
  name VARCHAR(255) NOT NULL,
  expression VARCHAR(MAX)
);

ALTER TABLE ${ohdsiSchema}.cc_strata
    ADD CONSTRAINT df_cc_strata_id DEFAULT (NEXT VALUE FOR ${ohdsiSchema}.cc_strata_seq) FOR id;

ALTER TABLE ${ohdsiSchema}.cc_strata
    ADD CONSTRAINT pk_cc_strata_id PRIMARY KEY(id);

ALTER TABLE ${ohdsiSchema}.cc_strata
    ADD CONSTRAINT fk_cc_strata_cc FOREIGN KEY (cohort_characterization_id)
    REFERENCES ${ohdsiSchema}.cohort_characterization(id) ON UPDATE NO ACTION ON DELETE CASCADE;

CREATE SEQUENCE ${ohdsiSchema}.cc_strata_conceptset_seq START WITH 1;

CREATE TABLE ${ohdsiSchema}.cc_strata_conceptset(
  id BIGINT NOT NULL,
  cohort_characterization_id BIGINT NOT NULL,
  expression VARCHAR(MAX)
);

ALTER TABLE ${ohdsiSchema}.cc_strata_conceptset
    ADD CONSTRAINT df_cc_strata_conceptset_id DEFAULT (NEXT VALUE FOR ${ohdsiSchema}.cc_strata_conceptset_seq) FOR id;

ALTER TABLE ${ohdsiSchema}.cc_strata_conceptset
    ADD CONSTRAINT pk_cc_strata_conceptset_id PRIMARY KEY(id);

ALTER TABLE ${ohdsiSchema}.cc_strata_conceptset
    ADD CONSTRAINT fk_cc_strata_conceptset_cc FOREIGN KEY (cohort_characterization_id)
    REFERENCES ${ohdsiSchema}.cohort_characterization(id) ON UPDATE NO ACTION ON DELETE CASCADE;

ALTER TABLE ${ohdsiSchema}.cohort_characterization ADD stratified_by VARCHAR(255);
ALTER TABLE ${ohdsiSchema}.cohort_characterization ADD strata_only BIT DEFAULT 0;