CREATE SEQUENCE ${ohdsiSchema}.fe_conceptset_sequence;

CREATE TABLE ${ohdsiSchema}.fe_analysis_conceptset (
  id NUMBER(19) NOT NULL,
  fe_analysis_id NUMBER(19) NOT NULL,
  expression CLOB
);

ALTER TABLE ${ohdsiSchema}.fe_analysis_conceptset
    ADD CONSTRAINT pk_fe_conceptset_id PRIMARY KEY (id);

ALTER TABLE ${ohdsiSchema}.fe_analysis_conceptset
    ADD CONSTRAINT fk_fe_conceptset_fe_analysis FOREIGN KEY (fe_analysis_id)
      REFERENCES ${ohdsiSchema}.fe_analysis(id);

ALTER TABLE ${ohdsiSchema}.fe_analysis_criteria DROP COLUMN conceptsets;