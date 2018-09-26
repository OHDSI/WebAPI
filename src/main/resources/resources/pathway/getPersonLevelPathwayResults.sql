SELECT
  combo_id,
  subject_id,
  cohort_start_date,
  cohort_end_date
FROM @target_database_schema.pathway_analysis_events
WHERE pathway_analysis_generation_id = @pathway_analysis_generation_id
AND target_cohort_id = @target_cohort_id