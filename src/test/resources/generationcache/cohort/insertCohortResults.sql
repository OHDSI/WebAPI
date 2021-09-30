INSERT INTO @results_database_schema.cohort_cache (design_hash, subject_id, cohort_start_date, cohort_end_date)
VALUES (@design_hash, 1, TO_DATE('2000-01-01', 'YYYY-MM-DD'), TO_DATE('2010-01-01', 'YYYY-MM-DD'));

INSERT INTO @results_database_schema.cohort_inclusion_result_cache (design_hash, mode_id, inclusion_rule_mask, person_count)
VALUES (@design_hash, 0, 0, 1);

INSERT INTO @results_database_schema.cohort_inclusion_stats_cache (design_hash, rule_sequence, mode_id, person_count, gain_count, person_total)
VALUES (@design_hash, 0, 0, 1, 1, 1);

INSERT INTO @results_database_schema.cohort_summary_stats_cache (design_hash, mode_id, base_count, final_count)
VALUES (@design_hash, 0, 1, 1);
