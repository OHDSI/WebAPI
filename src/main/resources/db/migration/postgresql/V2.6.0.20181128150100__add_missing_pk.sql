SELECT DISTINCT cohort_characterization_id, cohort_id 
INTO ${ohdsiSchema}.cc_cohort_tmp
FROM ${ohdsiSchema}.cc_cohort;

DELETE FROM ${ohdsiSchema}.cc_cohort;

INSERT INTO ${ohdsiSchema}.cc_cohort (cohort_characterization_id, cohort_id)
SELECT cohort_characterization_id, cohort_id 
FROM ${ohdsiSchema}.cc_cohort_tmp;

TRUNCATE TABLE ${ohdsiSchema}.cc_cohort_tmp;
DROP TABLE ${ohdsiSchema}.cc_cohort_tmp;


ALTER TABLE ${ohdsiSchema}.analysis_execution ADD PRIMARY KEY (id);
ALTER TABLE ${ohdsiSchema}.analysis_generation_info ADD PRIMARY KEY (job_execution_id);
ALTER TABLE ${ohdsiSchema}.cc_analysis ADD PRIMARY KEY (cohort_characterization_id, fe_analysis_id);
ALTER TABLE ${ohdsiSchema}.cc_cohort ADD PRIMARY KEY (cohort_characterization_id, cohort_id);
ALTER TABLE ${ohdsiSchema}.cca_execution_ext ADD PRIMARY KEY (cca_execution_id);
ALTER TABLE ${ohdsiSchema}.cohort ADD PRIMARY KEY (cohort_definition_id, subject_id);
ALTER TABLE ${ohdsiSchema}.cohort_analysis_list_xref ADD PRIMARY KEY (source_id, cohort_id, analysis_id);
ALTER TABLE ${ohdsiSchema}.cohort_concept_map ADD PRIMARY KEY (cohort_definition_id);
ALTER TABLE ${ohdsiSchema}.cohort_inclusion ADD PRIMARY KEY (cohort_definition_id);
ALTER TABLE ${ohdsiSchema}.cohort_inclusion_result ADD PRIMARY KEY (cohort_definition_id);
ALTER TABLE ${ohdsiSchema}.cohort_inclusion_stats ADD PRIMARY KEY (cohort_definition_id);
ALTER TABLE ${ohdsiSchema}.cohort_summary_stats ADD PRIMARY KEY (cohort_definition_id);
ALTER TABLE ${ohdsiSchema}.feas_study_inclusion_stats ADD PRIMARY KEY (study_id);
ALTER TABLE ${ohdsiSchema}.feas_study_index_stats ADD PRIMARY KEY (study_id);
ALTER TABLE ${ohdsiSchema}.feas_study_result ADD PRIMARY KEY (study_id);
ALTER TABLE ${ohdsiSchema}.feasibility_inclusion ADD PRIMARY KEY (study_id, sequence);
ALTER TABLE ${ohdsiSchema}.heracles_analysis ADD PRIMARY KEY (analysis_id);
ALTER TABLE ${ohdsiSchema}.penelope_laertes_universe ADD PRIMARY KEY (id);

ALTER TABLE ${ohdsiSchema}.cohort_features ADD COLUMN id SERIAL PRIMARY KEY;
ALTER TABLE ${ohdsiSchema}.cohort_features_analysis_ref ADD COLUMN id SERIAL PRIMARY KEY;
ALTER TABLE ${ohdsiSchema}.cohort_features_dist ADD COLUMN id SERIAL PRIMARY KEY;
ALTER TABLE ${ohdsiSchema}.cohort_features_ref ADD COLUMN id SERIAL PRIMARY KEY;
ALTER TABLE ${ohdsiSchema}.heracles_heel_results ADD COLUMN id SERIAL PRIMARY KEY;
ALTER TABLE ${ohdsiSchema}.heracles_results ADD COLUMN id SERIAL PRIMARY KEY;
ALTER TABLE ${ohdsiSchema}.heracles_results_dist ADD COLUMN id SERIAL PRIMARY KEY;
ALTER TABLE ${ohdsiSchema}.ir_analysis_dist ADD COLUMN id SERIAL PRIMARY KEY;
ALTER TABLE ${ohdsiSchema}.ir_analysis_result ADD COLUMN id SERIAL PRIMARY KEY;
ALTER TABLE ${ohdsiSchema}.ir_analysis_strata_stats ADD COLUMN id SERIAL PRIMARY KEY;
ALTER TABLE ${ohdsiSchema}.ir_strata ADD COLUMN id SERIAL PRIMARY KEY;
ALTER TABLE ${ohdsiSchema}.penelope_laertes_uni_pivot ADD COLUMN id SERIAL PRIMARY KEY;