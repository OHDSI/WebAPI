select code, name, is_combo
from @target_database_schema.pathway_analysis_codes
where pathway_analysis_generation_id = @generation_id;
