DELETE FROM @results_database_schema.cohort WHERE cohort_definition_id = @cohort_definition_id;
DELETE FROM @results_database_schema.cohort_inclusion_result WHERE cohort_definition_id = @cohort_definition_id;
DELETE FROM @results_database_schema.cohort_inclusion_stats WHERE cohort_definition_id = @cohort_definition_id;
DELETE FROM @results_database_schema.cohort_summary_stats WHERE cohort_definition_id = @cohort_definition_id;
DELETE FROM @results_database_schema.cohort_censor_stats WHERE cohort_definition_id = @cohort_definition_id;

INSERT INTO @results_database_schema.cohort (cohort_definition_id, subject_id, cohort_start_date, cohort_end_date)
SELECT @cohort_definition_id, subject_id, cohort_start_date, cohort_end_date
FROM @results_database_schema.cohort_cache cc
WHERE cc.design_hash = @design_hash;

INSERT INTO @results_database_schema.cohort_inclusion_result (cohort_definition_id, mode_id, inclusion_rule_mask, person_count)
SELECT @cohort_definition_id, mode_id, inclusion_rule_mask, person_count
FROM @results_database_schema.cohort_inclusion_result_cache irc
WHERE irc.design_hash = @design_hash;

INSERT INTO @results_database_schema.cohort_inclusion_stats (cohort_definition_id, rule_sequence, mode_id, person_count, gain_count, person_total)
SELECT @cohort_definition_id, rule_sequence, mode_id, person_count, gain_count, person_total
FROM @results_database_schema.cohort_inclusion_stats_cache isc
WHERE isc.design_hash = @design_hash;

INSERT INTO @results_database_schema.cohort_summary_stats (cohort_definition_id, mode_id, base_count, final_count)
SELECT @cohort_definition_id, mode_id, base_count, final_count
FROM @results_database_schema.cohort_summary_stats_cache ssc
WHERE ssc.design_hash = @design_hash;

INSERT INTO @results_database_schema.cohort_censor_stats (cohort_definition_id, lost_count)
SELECT @cohort_definition_id, lost_count
FROM @results_database_schema.cohort_censor_stats_cache ccs
WHERE ccs.design_hash = @design_hash;

