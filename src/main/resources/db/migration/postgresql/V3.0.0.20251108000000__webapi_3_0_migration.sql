-- WebAPI 3.0.0 Migration
-- This migration combines:
-- 1. Removal of deprecated analysis modules (Statistical Analysis, Pathway, Execution Engine, Shiny)
-- 2. Spring Batch 4 to Spring Batch 5 schema migration
-- This is a BREAKING CHANGE for v3.0 - see docs/MIGRATION_GUIDE_v3.0.md

-- =====================================================================
-- PART 1: Remove deprecated analysis modules
-- =====================================================================

-- Drop views first (to avoid dependency issues)
DROP VIEW IF EXISTS estimation_gen_view;
DROP VIEW IF EXISTS pathway_analysis_generation_view;
DROP VIEW IF EXISTS cc_generation;
DROP VIEW IF EXISTS estimation_analysis_generation;
DROP VIEW IF EXISTS prediction_analysis_generation;

-- Drop cohort characterization tables (before fe_analysis to avoid FK violations)
DROP TABLE IF EXISTS cohort_characterization_tag CASCADE;
DROP TABLE IF EXISTS cohort_characterization_version CASCADE;
DROP TABLE IF EXISTS cc_execution CASCADE;
DROP TABLE IF EXISTS cc_generation CASCADE;
DROP TABLE IF EXISTS cc_cohort CASCADE;
DROP TABLE IF EXISTS cc_strata_conceptset CASCADE;
DROP TABLE IF EXISTS cc_analysis CASCADE;
DROP TABLE IF EXISTS cc_strata CASCADE;
DROP TABLE IF EXISTS cc_param CASCADE;
DROP TABLE IF EXISTS cohort_characterization CASCADE;
DROP SEQUENCE IF EXISTS cohort_characterization_seq;
DROP SEQUENCE IF EXISTS cc_param_sequence;
DROP SEQUENCE IF EXISTS cc_strata_seq;
DROP SEQUENCE IF EXISTS cc_strata_conceptset_seq;
DROP SEQUENCE IF EXISTS cc_analysis_seq;

-- Drop estimation tables
DROP TABLE IF EXISTS estimation_analysis_generation CASCADE;
DROP TABLE IF EXISTS estimation CASCADE;
DROP SEQUENCE IF EXISTS estimation_seq;

-- Drop prediction tables
DROP TABLE IF EXISTS prediction_analysis_generation CASCADE;
DROP TABLE IF EXISTS prediction CASCADE;
DROP SEQUENCE IF EXISTS prediction_seq;

-- Drop feature analysis tables
DROP TABLE IF EXISTS fe_analysis_aggregate CASCADE;
DROP TABLE IF EXISTS fe_analysis_criteria CASCADE;
DROP TABLE IF EXISTS fe_analysis CASCADE;
DROP SEQUENCE IF EXISTS fe_analysis_sequence;
DROP SEQUENCE IF EXISTS fe_analysis_criteria_sequence;

-- Drop incidence rate tables
DROP TABLE IF EXISTS ir_tag CASCADE;
DROP TABLE IF EXISTS ir_strata CASCADE;
DROP TABLE IF EXISTS ir_analysis_result CASCADE;
DROP TABLE IF EXISTS ir_analysis_strata_stats CASCADE;
DROP TABLE IF EXISTS ir_execution CASCADE;
DROP TABLE IF EXISTS ir_analysis_details CASCADE;
DROP TABLE IF EXISTS ir_analysis CASCADE;
DROP SEQUENCE IF EXISTS ir_analysis_sequence;

-- Drop pathway tables
DROP TABLE IF EXISTS pathway_tag CASCADE;
DROP TABLE IF EXISTS pathway_event_cohort CASCADE;
DROP TABLE IF EXISTS pathway_target_cohort CASCADE;
DROP TABLE IF EXISTS pathway_analysis CASCADE;
DROP SEQUENCE IF EXISTS pathway_analysis_sequence;
DROP SEQUENCE IF EXISTS pathway_cohort_sequence;

-- Drop execution engine tables
DROP TABLE IF EXISTS input_files CASCADE;
DROP TABLE IF EXISTS output_files CASCADE;
DROP TABLE IF EXISTS analysis_execution CASCADE;
DROP SEQUENCE IF EXISTS output_file_seq;
DROP SEQUENCE IF EXISTS input_file_seq;

-- Drop shiny infrastructure tables
DROP TABLE IF EXISTS shiny_published CASCADE;
DROP SEQUENCE IF EXISTS shiny_published_sequence;

-- Note: Results schema tables (pathway_analysis_codes, pathway_analysis_events, etc.)
-- are in separate results schemas and must be dropped manually if desired.
-- This migration only handles WebAPI schema tables.

-- =====================================================================
-- PART 2: Spring Batch 4 to Spring Batch 5 migration
-- =====================================================================

-- 1. Update BATCH_JOB_EXECUTION_PARAMS table structure
-- Drop the old table and recreate with new Spring Batch 5 column names
DROP TABLE IF EXISTS ${ohdsiSchema}.BATCH_JOB_EXECUTION_PARAMS CASCADE;

CREATE TABLE ${ohdsiSchema}.BATCH_JOB_EXECUTION_PARAMS  (
	JOB_EXECUTION_ID BIGINT NOT NULL ,
	PARAMETER_NAME VARCHAR(100) NOT NULL ,
	PARAMETER_TYPE VARCHAR(100) NOT NULL ,
	PARAMETER_VALUE VARCHAR(2500) ,
	IDENTIFYING CHAR(1) NOT NULL ,
	constraint JOB_EXEC_PARAMS_FK foreign key (JOB_EXECUTION_ID)
	references ${ohdsiSchema}.BATCH_JOB_EXECUTION(JOB_EXECUTION_ID)
);

CREATE INDEX BJEP_JOB_EXEC_PARAMS_IDX ON ${ohdsiSchema}.BATCH_JOB_EXECUTION_PARAMS (JOB_EXECUTION_ID);

-- 2. Add missing CREATE_TIME column to BATCH_STEP_EXECUTION
ALTER TABLE ${ohdsiSchema}.BATCH_STEP_EXECUTION
ADD COLUMN IF NOT EXISTS CREATE_TIME TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

-- 3. Make START_TIME nullable for Spring Batch 5 compatibility
ALTER TABLE ${ohdsiSchema}.BATCH_STEP_EXECUTION
ALTER COLUMN START_TIME DROP NOT NULL;
