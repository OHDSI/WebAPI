CREATE INDEX HRD_IDX_COHORT_DEF_ID ON @results_schema.HERACLES_RESULTS_DIST (cohort_definition_id);
CREATE INDEX HRD_IDX_COHORT_ID_ANALYSIS_ID ON @results_schema.HERACLES_RESULTS_DIST (cohort_definition_id, analysis_id);
CREATE INDEX HRD_IDX_COHORT_DEF_ID_DT ON @results_schema.HERACLES_RESULTS_DIST (cohort_definition_id, last_update_time);
CREATE INDEX HRD_IDX_COHORT_ID_FIRST_RES ON @results_schema.HERACLES_RESULTS_DIST (cohort_definition_id, analysis_id, count_value, stratum_1);
CREATE INDEX HR_IDX_COHORT_DEF_ID ON @results_schema.HERACLES_RESULTS (cohort_definition_id);
CREATE INDEX HR_IDX_COHORT_ID_ANALYSIS_ID ON @results_schema.HERACLES_RESULTS (cohort_definition_id, analysis_id);
CREATE INDEX HR_IDX_COHORT_DEF_ID_DT ON @results_schema.HERACLES_RESULTS (cohort_definition_id, last_update_time);
CREATE INDEX HR_IDX_COHORT_ID_FIRST_RES ON @results_schema.HERACLES_RESULTS (cohort_definition_id, analysis_id, count_value, stratum_1);
CREATE INDEX HH_IDX_COHORT_ID_ANALYSIS_ID ON @results_schema.HERACLES_HEEL_RESULTS (cohort_definition_id, analysis_id);
