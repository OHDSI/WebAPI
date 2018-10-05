CREATE SEQUENCE ${ohdsiSchema}.fe_conceptset_sequence START WITH 1;

CREATE TABLE ${ohdsiSchema}.fe_analysis_conceptset (
  id bigint NOT NULL,
  fe_analysis_id int NOT NULL,
  expression varchar(max)
);

ALTER TABLE ${ohdsiSchema}.fe_analysis_conceptset
    ADD CONSTRAINT df_fe_conceptset_id DEFAULT (NEXT VALUE FOR ${ohdsiSchema}.fe_conceptset_sequence) FOR id;

ALTER TABLE ${ohdsiSchema}.fe_analysis_conceptset
    ADD CONSTRAINT pk_fe_conceptset_id PRIMARY KEY (id);

ALTER TABLE ${ohdsiSchema}.fe_analysis_conceptset
    ADD CONSTRAINT fk_fe_conceptset_fe_analysis FOREIGN KEY (fe_analysis_id)
      REFERENCES ${ohdsiSchema}.fe_analysis(id) ON UPDATE NO ACTION ON DELETE CASCADE;

ALTER TABLE ${ohdsiSchema}.fe_analysis_criteria DROP COLUMN conceptsets;