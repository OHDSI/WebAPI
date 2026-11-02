-- Feasiblity Study
drop table ${ohdsiSchema}.feas_study_generation_info;
drop table ${ohdsiSchema}.feas_study_inclusion_stats;
drop table ${ohdsiSchema}.feas_study_index_stats;
drop table ${ohdsiSchema}.feas_study_result;
drop table ${ohdsiSchema}.feasibility_inclusion;
drop table ${ohdsiSchema}.feasibility_study;

-- CCA
drop table ${ohdsiSchema}.cca;
drop table ${ohdsiSchema}.cca_execution;
drop table ${ohdsiSchema}.cca_execution_ext;
drop table ${ohdsiSchema}.estimation;

-- PLP/Prediction
drop table ${ohdsiSchema}.plp;
drop table ${ohdsiSchema}.prediction;

-- heracles
drop table ${ohdsiSchema}.cohort_analysis_list_xref;
drop table ${ohdsiSchema}.cohort_analysis_gen_info;
drop table ${ohdsiSchema}.heracles_analysis;
drop table ${ohdsiSchema}.heracles_heel_results;
drop table ${ohdsiSchema}.heracles_results;
drop table ${ohdsiSchema}.heracles_results_dist;
drop table ${ohdsiSchema}.heracles_visualization_data;

-- Other Tables
drop table ${ohdsiSchema}.input_files;
drop table ${ohdsiSchema}.output_file_contents;
drop table ${ohdsiSchema}.output_files;

-- Results Schema Example Tables
drop table ${ohdsiSchema}.cohort;
drop table ${ohdsiSchema}.cohort_inclusion;
drop table ${ohdsiSchema}.cohort_inclusion_result;
drop table ${ohdsiSchema}.cohort_inclusion_stats;
drop table ${ohdsiSchema}.cohort_summary_stats;
drop table ${ohdsiSchema}.exampleapp_widget;
drop table ${ohdsiSchema}.ir_analysis_dist;
drop table ${ohdsiSchema}.ir_analysis_result;
drop table ${ohdsiSchema}.ir_analysis_strata_stats;
drop table ${ohdsiSchema}.ir_strata;

-- SEQUENCES

-- Feasibility Study
drop sequence ${ohdsiSchema}.feasibility_study_sequence;

-- CCA
drop sequence ${ohdsiSchema}.cca_sequence;
drop sequence ${ohdsiSchema}.cca_execution_sequence;

-- PLP/Prediction
drop sequence ${ohdsiSchema}.plp_sequence;
drop sequence ${ohdsiSchema}.prediction_seq;
drop sequence ${ohdsiSchema}.estimation_seq;

-- Heracles
drop sequence ${ohdsiSchema}.heracles_vis_data_sequence;
drop sequence ${ohdsiSchema}.heracles_viz_data_sequence;

-- Other Tables
drop sequence ${ohdsiSchema}.output_file_seq;
drop sequence ${ohdsiSchema}.input_file_seq;