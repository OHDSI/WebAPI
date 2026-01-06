CREATE SEQUENCE ${ohdsiSchema}.cc_strata_seq;

CREATE TABLE ${ohdsiSchema}.cc_strata(
  id BIGINT NOT NULL DEFAULT nextval('${ohdsiSchema}.cc_strata_seq'),
  cohort_characterization_id BIGINT NOT NULL,
  name VARCHAR NOT NULL,
  expression VARCHAR,
  CONSTRAINT pk_cc_strata_id PRIMARY KEY(id)
);

ALTER TABLE ${ohdsiSchema}.cc_strata
    ADD CONSTRAINT fk_cc_strata_cc FOREIGN KEY (cohort_characterization_id)
    REFERENCES ${ohdsiSchema}.cohort_characterization(id) ON UPDATE NO ACTION ON DELETE CASCADE;

CREATE SEQUENCE ${ohdsiSchema}.cc_strata_conceptset_seq;

CREATE TABLE ${ohdsiSchema}.cc_strata_conceptset(
  id BIGINT NOT NULL DEFAULT nextval('${ohdsiSchema}.cc_strata_conceptset_seq'),
  cohort_characterization_id BIGINT NOT NULL,
  expression VARCHAR,
  CONSTRAINT pk_cc_strata_conceptset_id PRIMARY KEY(id)
);

ALTER TABLE ${ohdsiSchema}.cc_strata_conceptset
    ADD CONSTRAINT fk_cc_strata_conceptset_cc FOREIGN KEY (cohort_characterization_id)
    REFERENCES ${ohdsiSchema}.cohort_characterization(id) ON UPDATE NO ACTION ON DELETE CASCADE;

ALTER TABLE ${ohdsiSchema}.cohort_characterization ADD stratified_by VARCHAR;
ALTER TABLE ${ohdsiSchema}.cohort_characterization ADD strata_only BOOLEAN DEFAULT FALSE;