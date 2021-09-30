DELETE FROM @results_database_schema.cohort_cache WHERE design_hash = @design_hash;
DELETE FROM @results_database_schema.cohort_inclusion_result_cache WHERE design_hash = @design_hash;
DELETE FROM @results_database_schema.cohort_inclusion_stats_cache WHERE design_hash = @design_hash;
DELETE FROM @results_database_schema.cohort_summary_stats_cache WHERE design_hash = @design_hash;
DELETE FROM @results_database_schema.cohort_censor_stats_cache WHERE design_hash = @design_hash;
