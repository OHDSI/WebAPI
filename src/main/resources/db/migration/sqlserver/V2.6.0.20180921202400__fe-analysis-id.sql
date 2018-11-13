ALTER TABLE ${ohdsiSchema}.cc_analysis DROP CONSTRAINT fk_c_char_a_fe_analysis;
ALTER TABLE ${ohdsiSchema}.cc_analysis ALTER COLUMN fe_analysis_id INTEGER;

ALTER TABLE ${ohdsiSchema}.fe_analysis_criteria DROP CONSTRAINT fk_fec_fe_analysis;
ALTER TABLE ${ohdsiSchema}.fe_analysis_criteria ALTER COLUMN fe_analysis_id INTEGER;

ALTER TABLE ${ohdsiSchema}.fe_analysis DROP CONSTRAINT PK_fe;
ALTER TABLE ${ohdsiSchema}.fe_analysis DROP CONSTRAINT df_fe_analysis_id;
ALTER TABLE ${ohdsiSchema}.fe_analysis ALTER COLUMN id INTEGER NOT NULL;
ALTER TABLE ${ohdsiSchema}.fe_analysis ADD CONSTRAINT df_fe_analysis_id DEFAULT (NEXT VALUE FOR ${ohdsiSchema}.fe_analysis_sequence) FOR id;
ALTER TABLE ${ohdsiSchema}.fe_analysis ADD CONSTRAINT PK_fe PRIMARY KEY (id);

ALTER TABLE ${ohdsiSchema}.fe_analysis_criteria ADD CONSTRAINT fk_fec_fe_analysis FOREIGN KEY (fe_analysis_id)
	REFERENCES ${ohdsiSchema}.fe_analysis(id)
	ON UPDATE NO ACTION ON DELETE CASCADE;

ALTER TABLE ${ohdsiSchema}.cc_analysis ADD CONSTRAINT fk_c_char_a_fe_analysis FOREIGN KEY (fe_analysis_id)
	REFERENCES ${ohdsiSchema}.fe_analysis(id)
	ON UPDATE NO ACTION ON DELETE CASCADE;