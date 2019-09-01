INSERT INTO @results_database_schema.cohort_generations (generation_id, subject_id, cohort_start_date, cohort_end_date)
VALUES (@generation_id, 1, TO_DATE('2000-01-01', 'YYYY-MM-DD'), TO_DATE('2010-01-01', 'YYYY-MM-DD'));

INSERT INTO @results_database_schema.cohort_inclusion (generation_id, rule_sequence, name, description)
VALUES (@generation_id, 0, 'Some name', 'Some description');

INSERT INTO @results_database_schema.cohort_inclusion_result (generation_id, mode_id, inclusion_rule_mask, person_count)
VALUES (@generation_id, 0, 0, 1);

INSERT INTO @results_database_schema.cohort_inclusion_stats (generation_id, rule_sequence, mode_id, person_count, gain_count, person_total)
VALUES (@generation_id, 0, 0, 1, 1, 1);

INSERT INTO @results_database_schema.cohort_summary_stats (generation_id, mode_id, base_count, final_count)
VALUES (@generation_id, 0, 1, 1);
