select target_cohort_id, target_cohort_count, pathways_count
from @target_database_schema.pathway_analysis_stats
where pathway_analysis_generation_id = @generation_id;
