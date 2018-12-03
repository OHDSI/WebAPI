CREATE SEQUENCE ${ohdsiSchema}.cc_strata_seq;

CREATE TABLE ${ohdsiSchema}.cc_strata(
  id NUMBER(19) NOT NULL,
  cohort_characterization_id NUMBER(19) NOT NULL,
  name VARCHAR(255) NOT NULL,
  expression CLOB,
  CONSTRAINT pk_cc_strata_id PRIMARY KEY(id)
);

ALTER TABLE ${ohdsiSchema}.cc_strata
    ADD CONSTRAINT fk_cc_strata_cc FOREIGN KEY (cohort_characterization_id)
    REFERENCES ${ohdsiSchema}.cohort_characterization(id);

CREATE SEQUENCE ${ohdsiSchema}.cc_strata_conceptset_seq;

CREATE TABLE ${ohdsiSchema}.cc_strata_conceptset(
  id NUMBER(19) NOT NULL,
  cohort_characterization_id NUMBER(19) NOT NULL,
  expression CLOB,
  CONSTRAINT pk_cc_strata_conceptset_id PRIMARY KEY(id)
);

ALTER TABLE ${ohdsiSchema}.cc_strata_conceptset
  ADD CONSTRAINT fk_cc_strata_conceptset_cc FOREIGN KEY (cohort_characterization_id)
  REFERENCES ${ohdsiSchema}.cohort_characterization(id);

ALTER TABLE ${ohdsiSchema}.cohort_characterization ADD stratified_by VARCHAR(255);
ALTER TABLE ${ohdsiSchema}.cohort_characterization ADD strata_only CHAR(1) DEFAULT '0';