SELECT *
FROM @target_database_schema.pathway_analysis_stats
WHERE pathway_analysis_generation_id = @generation_id;