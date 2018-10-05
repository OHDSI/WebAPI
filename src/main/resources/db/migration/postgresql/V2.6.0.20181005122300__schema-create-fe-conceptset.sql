CREATE SEQUENCE ${ohdsiSchema}.fe_conceptset_sequence;

CREATE TABLE ${ohdsiSchema}.fe_analysis_conceptset (
  id bigint NOT NULL DEFAULT NEXTVAL('${ohdsiSchema}.fe_conceptset_sequence'),
  fe_analysis_id int NOT NULL,
  expression varchar
);

ALTER TABLE ${ohdsiSchema}.fe_analysis_conceptset
    ADD CONSTRAINT pk_fe_conceptset_id PRIMARY KEY (id);

ALTER TABLE ${ohdsiSchema}.fe_analysis_conceptset
    ADD CONSTRAINT fk_fe_conceptset_fe_analysis FOREIGN KEY (fe_analysis_id)
      REFERENCES ${ohdsiSchema}.fe_analysis(id) ON UPDATE NO ACTION ON DELETE CASCADE;

ALTER TABLE ${ohdsiSchema}.fe_analysis_criteria DROP COLUMN conceptsets;