SELECT * FROM @target_database_schema.pathway_code 
                JOIN @target_database_schema.cohort_pathway ON pathway_code.pathway_generation_id = cohort_pathway.pathway_generation_id
                JOIN @target_database_schema.pathways_count ON cohort_pathway.cohort_id = pathways_count.cohort_id
WHERE pathway_code.pathway_generation_id = @pathway_generation_id;