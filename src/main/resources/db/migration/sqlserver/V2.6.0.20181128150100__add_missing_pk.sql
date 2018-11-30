ALTER TABLE ${ohdsiSchema}.analysis_execution ADD PRIMARY KEY (id);
GO
ALTER TABLE ${ohdsiSchema}.analysis_generation_info ADD PRIMARY KEY (job_execution_id);
GO

UPDATE ${ohdsiSchema}.cc_analysis SET fe_analysis_id = 0 WHERE cc_analysis.fe_analysis_id IS NULL;
ALTER TABLE ${ohdsiSchema}.cc_analysis ALTER COLUMN fe_analysis_id INT NOT NULL;
GO
ALTER TABLE ${ohdsiSchema}.cc_analysis ADD PRIMARY KEY (cohort_characterization_id, fe_analysis_id);
GO

ALTER TABLE ${ohdsiSchema}.cc_cohort ADD PRIMARY KEY (cohort_characterization_id, cohort_id);
GO
ALTER TABLE ${ohdsiSchema}.cca_execution_ext ADD PRIMARY KEY (cca_execution_id);
GO
ALTER TABLE ${ohdsiSchema}.cohort ADD PRIMARY KEY (cohort_definition_id, subject_id);
GO

UPDATE ${ohdsiSchema}.cohort_analysis_list_xref SET source_id = 0 WHERE source_id IS NULL;
ALTER TABLE ${ohdsiSchema}.cohort_analysis_list_xref ALTER COLUMN source_id INT NOT NULL;
GO
UPDATE ${ohdsiSchema}.cohort_analysis_list_xref SET cohort_id = 0 WHERE cohort_id IS NULL;
ALTER TABLE ${ohdsiSchema}.cohort_analysis_list_xref ALTER COLUMN cohort_id INT NOT NULL;
GO
UPDATE ${ohdsiSchema}.cohort_analysis_list_xref SET analysis_id = 0 WHERE analysis_id IS NULL;
ALTER TABLE ${ohdsiSchema}.cohort_analysis_list_xref ALTER COLUMN analysis_id INT NOT NULL;
GO
ALTER TABLE ${ohdsiSchema}.cohort_analysis_list_xref ADD PRIMARY KEY (source_id, cohort_id, analysis_id);
GO

UPDATE ${ohdsiSchema}.cohort_concept_map SET cohort_definition_id = 0 WHERE cohort_definition_id IS NULL;
ALTER TABLE ${ohdsiSchema}.cohort_concept_map ALTER COLUMN cohort_definition_id INT NOT NULL;
GO
ALTER TABLE ${ohdsiSchema}.cohort_concept_map ADD PRIMARY KEY (cohort_definition_id);
GO

ALTER TABLE ${ohdsiSchema}.cohort_inclusion ADD PRIMARY KEY (cohort_definition_id);
ALTER TABLE ${ohdsiSchema}.cohort_inclusion_result ADD PRIMARY KEY (cohort_definition_id);
ALTER TABLE ${ohdsiSchema}.cohort_inclusion_stats ADD PRIMARY KEY (cohort_definition_id);
ALTER TABLE ${ohdsiSchema}.cohort_summary_stats ADD PRIMARY KEY (cohort_definition_id);
ALTER TABLE ${ohdsiSchema}.feas_study_inclusion_stats ADD PRIMARY KEY (study_id);
ALTER TABLE ${ohdsiSchema}.feas_study_index_stats ADD PRIMARY KEY (study_id);
ALTER TABLE ${ohdsiSchema}.feas_study_result ADD PRIMARY KEY (study_id);
GO

UPDATE ${ohdsiSchema}.feasibility_inclusion SET name = '' WHERE name IS NULL;
ALTER TABLE ${ohdsiSchema}.feasibility_inclusion ALTER COLUMN name VARCHAR(255) NOT NULL;
GO
ALTER TABLE ${ohdsiSchema}.feasibility_inclusion ADD PRIMARY KEY (name);
GO

UPDATE ${ohdsiSchema}.heracles_analysis SET analysis_id = 0 WHERE analysis_id IS NULL;
ALTER TABLE ${ohdsiSchema}.heracles_analysis ALTER COLUMN analysis_id INT NOT NULL;
GO
ALTER TABLE ${ohdsiSchema}.heracles_analysis ADD PRIMARY KEY (analysis_id);

ALTER TABLE ${ohdsiSchema}.source ADD PRIMARY KEY (SOURCE_ID);

ALTER TABLE ${ohdsiSchema}.cohort_features ADD id INT IDENTITY PRIMARY KEY;
ALTER TABLE ${ohdsiSchema}.cohort_features_analysis_ref ADD id INT IDENTITY PRIMARY KEY;
ALTER TABLE ${ohdsiSchema}.cohort_features_dist ADD id INT IDENTITY PRIMARY KEY;
ALTER TABLE ${ohdsiSchema}.cohort_features_ref ADD id INT IDENTITY PRIMARY KEY;
ALTER TABLE ${ohdsiSchema}.heracles_heel_results ADD id INT IDENTITY PRIMARY KEY;
ALTER TABLE ${ohdsiSchema}.heracles_results ADD id INT IDENTITY PRIMARY KEY;
ALTER TABLE ${ohdsiSchema}.heracles_results_dist ADD id INT IDENTITY PRIMARY KEY;
ALTER TABLE ${ohdsiSchema}.ir_analysis_dist ADD id INT IDENTITY PRIMARY KEY;
ALTER TABLE ${ohdsiSchema}.ir_analysis_result ADD id INT IDENTITY PRIMARY KEY;
ALTER TABLE ${ohdsiSchema}.ir_analysis_strata_stats ADD id INT IDENTITY PRIMARY KEY;
ALTER TABLE ${ohdsiSchema}.ir_strata ADD id INT IDENTITY PRIMARY KEY;
ALTER TABLE ${ohdsiSchema}.penelope_laertes_uni_pivot ADD id INT IDENTITY PRIMARY KEY;