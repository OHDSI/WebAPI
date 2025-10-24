-- V2.16.0.20251107000000__remove_analysis_modules.sql
-- Remove Statistical Analysis, Pathway Analysis, Execution Engine, and Shiny Infrastructure modules
-- This is a BREAKING CHANGE for v3.0 - see docs/MIGRATION_GUIDE_v3.0.md

-- Drop views first (to avoid dependency issues)
IF OBJECT_ID('dbo.estimation_gen_view', 'V') IS NOT NULL DROP VIEW dbo.estimation_gen_view;
IF OBJECT_ID('dbo.pathway_analysis_generation_view', 'V') IS NOT NULL DROP VIEW dbo.pathway_analysis_generation_view;
IF OBJECT_ID('dbo.cc_generation', 'V') IS NOT NULL DROP VIEW dbo.cc_generation;
IF OBJECT_ID('dbo.estimation_analysis_generation', 'V') IS NOT NULL DROP VIEW dbo.estimation_analysis_generation;
IF OBJECT_ID('dbo.prediction_analysis_generation', 'V') IS NOT NULL DROP VIEW dbo.prediction_analysis_generation;

-- Drop cohort characterization tables (before fe_analysis to avoid FK violations)
IF OBJECT_ID('dbo.cohort_characterization_tag', 'U') IS NOT NULL DROP TABLE dbo.cohort_characterization_tag;
IF OBJECT_ID('dbo.cohort_characterization_version', 'U') IS NOT NULL DROP TABLE dbo.cohort_characterization_version;
IF OBJECT_ID('dbo.cc_execution', 'U') IS NOT NULL DROP TABLE dbo.cc_execution;
IF OBJECT_ID('dbo.cc_generation', 'U') IS NOT NULL DROP TABLE dbo.cc_generation;
IF OBJECT_ID('dbo.cc_cohort', 'U') IS NOT NULL DROP TABLE dbo.cc_cohort;
IF OBJECT_ID('dbo.cc_strata_conceptset', 'U') IS NOT NULL DROP TABLE dbo.cc_strata_conceptset;
IF OBJECT_ID('dbo.cc_analysis', 'U') IS NOT NULL DROP TABLE dbo.cc_analysis;
IF OBJECT_ID('dbo.cc_strata', 'U') IS NOT NULL DROP TABLE dbo.cc_strata;
IF OBJECT_ID('dbo.cc_param', 'U') IS NOT NULL DROP TABLE dbo.cc_param;
IF OBJECT_ID('dbo.cohort_characterization', 'U') IS NOT NULL DROP TABLE dbo.cohort_characterization;
IF OBJECT_ID('dbo.cohort_characterization_seq', 'SO') IS NOT NULL DROP SEQUENCE dbo.cohort_characterization_seq;
IF OBJECT_ID('dbo.cc_param_sequence', 'SO') IS NOT NULL DROP SEQUENCE dbo.cc_param_sequence;
IF OBJECT_ID('dbo.cc_strata_seq', 'SO') IS NOT NULL DROP SEQUENCE dbo.cc_strata_seq;
IF OBJECT_ID('dbo.cc_strata_conceptset_seq', 'SO') IS NOT NULL DROP SEQUENCE dbo.cc_strata_conceptset_seq;
IF OBJECT_ID('dbo.cc_analysis_seq', 'SO') IS NOT NULL DROP SEQUENCE dbo.cc_analysis_seq;

-- Drop estimation tables
IF OBJECT_ID('dbo.estimation_analysis_generation', 'U') IS NOT NULL DROP TABLE dbo.estimation_analysis_generation;
IF OBJECT_ID('dbo.estimation', 'U') IS NOT NULL DROP TABLE dbo.estimation;
IF OBJECT_ID('dbo.estimation_seq', 'SO') IS NOT NULL DROP SEQUENCE dbo.estimation_seq;

-- Drop prediction tables
IF OBJECT_ID('dbo.prediction_analysis_generation', 'U') IS NOT NULL DROP TABLE dbo.prediction_analysis_generation;
IF OBJECT_ID('dbo.prediction', 'U') IS NOT NULL DROP TABLE dbo.prediction;
IF OBJECT_ID('dbo.prediction_seq', 'SO') IS NOT NULL DROP SEQUENCE dbo.prediction_seq;

-- Drop feature analysis tables
IF OBJECT_ID('dbo.fe_analysis_aggregate', 'U') IS NOT NULL DROP TABLE dbo.fe_analysis_aggregate;
IF OBJECT_ID('dbo.fe_analysis_criteria', 'U') IS NOT NULL DROP TABLE dbo.fe_analysis_criteria;
IF OBJECT_ID('dbo.fe_analysis', 'U') IS NOT NULL DROP TABLE dbo.fe_analysis;
IF OBJECT_ID('dbo.fe_analysis_sequence', 'SO') IS NOT NULL DROP SEQUENCE dbo.fe_analysis_sequence;
IF OBJECT_ID('dbo.fe_analysis_criteria_sequence', 'SO') IS NOT NULL DROP SEQUENCE dbo.fe_analysis_criteria_sequence;

-- Drop incidence rate tables
IF OBJECT_ID('dbo.ir_tag', 'U') IS NOT NULL DROP TABLE dbo.ir_tag;
IF OBJECT_ID('dbo.ir_strata', 'U') IS NOT NULL DROP TABLE dbo.ir_strata;
IF OBJECT_ID('dbo.ir_analysis_result', 'U') IS NOT NULL DROP TABLE dbo.ir_analysis_result;
IF OBJECT_ID('dbo.ir_analysis_strata_stats', 'U') IS NOT NULL DROP TABLE dbo.ir_analysis_strata_stats;
IF OBJECT_ID('dbo.ir_execution', 'U') IS NOT NULL DROP TABLE dbo.ir_execution;
IF OBJECT_ID('dbo.ir_analysis_details', 'U') IS NOT NULL DROP TABLE dbo.ir_analysis_details;
IF OBJECT_ID('dbo.ir_analysis', 'U') IS NOT NULL DROP TABLE dbo.ir_analysis;
IF OBJECT_ID('dbo.ir_analysis_sequence', 'SO') IS NOT NULL DROP SEQUENCE dbo.ir_analysis_sequence;

-- Drop pathway tables
IF OBJECT_ID('dbo.pathway_tag', 'U') IS NOT NULL DROP TABLE dbo.pathway_tag;
IF OBJECT_ID('dbo.pathway_event_cohort', 'U') IS NOT NULL DROP TABLE dbo.pathway_event_cohort;
IF OBJECT_ID('dbo.pathway_target_cohort', 'U') IS NOT NULL DROP TABLE dbo.pathway_target_cohort;
IF OBJECT_ID('dbo.pathway_analysis', 'U') IS NOT NULL DROP TABLE dbo.pathway_analysis;
IF OBJECT_ID('dbo.pathway_analysis_sequence', 'SO') IS NOT NULL DROP SEQUENCE dbo.pathway_analysis_sequence;
IF OBJECT_ID('dbo.pathway_cohort_sequence', 'SO') IS NOT NULL DROP SEQUENCE dbo.pathway_cohort_sequence;

-- Drop execution engine tables
IF OBJECT_ID('dbo.input_files', 'U') IS NOT NULL DROP TABLE dbo.input_files;
IF OBJECT_ID('dbo.output_files', 'U') IS NOT NULL DROP TABLE dbo.output_files;
IF OBJECT_ID('dbo.analysis_execution', 'U') IS NOT NULL DROP TABLE dbo.analysis_execution;
IF OBJECT_ID('dbo.output_file_seq', 'SO') IS NOT NULL DROP SEQUENCE dbo.output_file_seq;
IF OBJECT_ID('dbo.input_file_seq', 'SO') IS NOT NULL DROP SEQUENCE dbo.input_file_seq;

-- Drop shiny infrastructure tables
IF OBJECT_ID('dbo.shiny_published', 'U') IS NOT NULL DROP TABLE dbo.shiny_published;
IF OBJECT_ID('dbo.shiny_published_sequence', 'SO') IS NOT NULL DROP SEQUENCE dbo.shiny_published_sequence;

-- Note: Results schema tables (pathway_analysis_codes, pathway_analysis_events, etc.)
-- are in separate results schemas and must be dropped manually if desired.
-- This migration only handles WebAPI schema tables.
