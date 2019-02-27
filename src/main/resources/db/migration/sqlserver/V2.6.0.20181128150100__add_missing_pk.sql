SELECT DISTINCT cohort_characterization_id, cohort_id 
INTO ${ohdsiSchema}.cc_cohort_tmp
FROM ${ohdsiSchema}.cc_cohort;

DELETE FROM ${ohdsiSchema}.cc_cohort;

INSERT INTO ${ohdsiSchema}.cc_cohort (cohort_characterization_id, cohort_id)
SELECT cohort_characterization_id, cohort_id 
FROM ${ohdsiSchema}.cc_cohort_tmp;

TRUNCATE TABLE ${ohdsiSchema}.cc_cohort_tmp;
DROP TABLE ${ohdsiSchema}.cc_cohort_tmp;
GO

IF OBJECT_ID('${ohdsiSchema}.pk_analysis_exec', 'PK') IS NULL
ALTER TABLE ${ohdsiSchema}.analysis_execution ADD CONSTRAINT pk_analysis_exec PRIMARY KEY (id);
GO
IF OBJECT_ID('${ohdsiSchema}.pk_an_gen_info', 'PK') IS NULL
ALTER TABLE ${ohdsiSchema}.analysis_generation_info ADD CONSTRAINT pk_an_gen_info PRIMARY KEY (job_execution_id);
GO

UPDATE ${ohdsiSchema}.cc_analysis SET fe_analysis_id = 0 WHERE cc_analysis.fe_analysis_id IS NULL;
ALTER TABLE ${ohdsiSchema}.cc_analysis ALTER COLUMN fe_analysis_id INT NOT NULL;
GO
IF OBJECT_ID('${ohdsiSchema}.pk_cc_analysis', 'PK') IS NULL
ALTER TABLE ${ohdsiSchema}.cc_analysis ADD CONSTRAINT pk_cc_analysis PRIMARY KEY (cohort_characterization_id, fe_analysis_id);
GO

IF OBJECT_ID('${ohdsiSchema}.pk_cc_cohort', 'PK') IS NULL
ALTER TABLE ${ohdsiSchema}.cc_cohort ADD CONSTRAINT pk_cc_cohort PRIMARY KEY (cohort_characterization_id, cohort_id);
GO
IF OBJECT_ID('${ohdsiSchema}.pk_cca_exec_ext', 'PK') IS NULL
ALTER TABLE ${ohdsiSchema}.cca_execution_ext ADD CONSTRAINT pk_cca_exec_ext PRIMARY KEY (cca_execution_id);
GO
IF OBJECT_ID('${ohdsiSchema}.pk_cohort', 'PK') IS NULL
ALTER TABLE ${ohdsiSchema}.cohort ADD CONSTRAINT pk_cohort PRIMARY KEY (cohort_definition_id, subject_id);
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
IF OBJECT_ID('${ohdsiSchema}.pk_cohort_an_list_xref', 'PK') IS NULL
ALTER TABLE ${ohdsiSchema}.cohort_analysis_list_xref ADD CONSTRAINT pk_cohort_an_list_xref PRIMARY KEY (source_id, cohort_id, analysis_id);
GO

UPDATE ${ohdsiSchema}.cohort_concept_map SET cohort_definition_id = 0 WHERE cohort_definition_id IS NULL;
ALTER TABLE ${ohdsiSchema}.cohort_concept_map ALTER COLUMN cohort_definition_id INT NOT NULL;
GO
IF OBJECT_ID('${ohdsiSchema}.pk_cohort_concept_map', 'PK') IS NULL
ALTER TABLE ${ohdsiSchema}.cohort_concept_map ADD CONSTRAINT pk_cohort_concept_map PRIMARY KEY (cohort_definition_id);
GO

IF OBJECT_ID('${ohdsiSchema}.pk_cohort_incl', 'PK') IS NULL
ALTER TABLE ${ohdsiSchema}.cohort_inclusion ADD CONSTRAINT pk_cohort_incl PRIMARY KEY (cohort_definition_id);
IF OBJECT_ID('${ohdsiSchema}.pk_cohort_incl_res', 'PK') IS NULL
ALTER TABLE ${ohdsiSchema}.cohort_inclusion_result ADD CONSTRAINT pk_cohort_incl_res PRIMARY KEY (cohort_definition_id);
IF OBJECT_ID('${ohdsiSchema}.pk_cohort_incl_stat', 'PK') IS NULL
ALTER TABLE ${ohdsiSchema}.cohort_inclusion_stats ADD CONSTRAINT pk_cohort_incl_stat PRIMARY KEY (cohort_definition_id);
IF OBJECT_ID('${ohdsiSchema}.pk_cohort_summary_stat', 'PK') IS NULL
ALTER TABLE ${ohdsiSchema}.cohort_summary_stats ADD CONSTRAINT pk_cohort_summary_stat PRIMARY KEY (cohort_definition_id);
IF OBJECT_ID('${ohdsiSchema}.pk_feas_st_incl_stats', 'PK') IS NULL
ALTER TABLE ${ohdsiSchema}.feas_study_inclusion_stats ADD CONSTRAINT pk_feas_st_incl_stats PRIMARY KEY (study_id);
IF OBJECT_ID('${ohdsiSchema}.pk_feas_st_indx_stats', 'PK') IS NULL
ALTER TABLE ${ohdsiSchema}.feas_study_index_stats ADD CONSTRAINT pk_feas_st_indx_stats PRIMARY KEY (study_id);
IF OBJECT_ID('${ohdsiSchema}.pk_feas_study_result', 'PK') IS NULL
ALTER TABLE ${ohdsiSchema}.feas_study_result ADD CONSTRAINT pk_feas_study_result PRIMARY KEY (study_id);
GO

UPDATE ${ohdsiSchema}.feasibility_inclusion SET name = '' WHERE name IS NULL;
ALTER TABLE ${ohdsiSchema}.feasibility_inclusion ALTER COLUMN name VARCHAR(255) NOT NULL;
GO
ALTER TABLE ${ohdsiSchema}.feasibility_inclusion ADD CONSTRAINT pk_feas_inclusion PRIMARY KEY (study_id, sequence);
GO

UPDATE ${ohdsiSchema}.heracles_analysis SET analysis_id = 0 WHERE analysis_id IS NULL;
ALTER TABLE ${ohdsiSchema}.heracles_analysis ALTER COLUMN analysis_id INT NOT NULL;
GO
ALTER TABLE ${ohdsiSchema}.heracles_analysis ADD CONSTRAINT pk_heracles_analysis PRIMARY KEY (analysis_id);

ALTER TABLE ${ohdsiSchema}.source ADD CONSTRAINT pk_source PRIMARY KEY (SOURCE_ID);

ALTER TABLE ${ohdsiSchema}.cohort_features ADD id INT IDENTITY CONSTRAINT pk_cohort_features PRIMARY KEY;
ALTER TABLE ${ohdsiSchema}.cohort_features_analysis_ref ADD id INT IDENTITY CONSTRAINT pk_coh_features_an_ref PRIMARY KEY;
ALTER TABLE ${ohdsiSchema}.cohort_features_dist ADD id INT IDENTITY CONSTRAINT pk_coh_features_dist PRIMARY KEY;
ALTER TABLE ${ohdsiSchema}.cohort_features_ref ADD id INT IDENTITY CONSTRAINT pk_coh_features_ref PRIMARY KEY;
ALTER TABLE ${ohdsiSchema}.heracles_heel_results ADD id INT IDENTITY CONSTRAINT pk_heracles_heel_res PRIMARY KEY;
ALTER TABLE ${ohdsiSchema}.heracles_results ADD id INT IDENTITY CONSTRAINT pk_heracles_res PRIMARY KEY;
ALTER TABLE ${ohdsiSchema}.heracles_results_dist ADD id INT IDENTITY CONSTRAINT pk_heracles_res_dist PRIMARY KEY;
ALTER TABLE ${ohdsiSchema}.ir_analysis_dist ADD id INT IDENTITY CONSTRAINT pk_ir_analysis_dist PRIMARY KEY;
ALTER TABLE ${ohdsiSchema}.ir_analysis_result ADD id INT IDENTITY CONSTRAINT pk_ir_analysis_res PRIMARY KEY;
ALTER TABLE ${ohdsiSchema}.ir_analysis_strata_stats ADD id INT IDENTITY CONSTRAINT pk_ir_an_strata_stats PRIMARY KEY;
ALTER TABLE ${ohdsiSchema}.ir_strata ADD id INT IDENTITY CONSTRAINT pk_ir_strata PRIMARY KEY;
ALTER TABLE ${ohdsiSchema}.penelope_laertes_uni_pivot ADD id INT IDENTITY CONSTRAINT pk_penelope_lae_uni_piv PRIMARY KEY;