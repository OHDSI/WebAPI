IF OBJECT_ID('@results_schema.pathway_analysis_stats', 'U') IS NULL
CREATE TABLE @results_schema.pathway_analysis_stats
(
  pathway_analysis_generation_id BIGINT NOT NULL,
  target_cohort_id INTEGER NOT NULL,
  target_cohort_count INTEGER NOT NULL,
  pathways_count INTEGER NOT NULL
);
