INSERT INTO @target_database_schema.pathway_analysis_codes (pathway_analysis_generation_id, code, name, is_combo)
        VALUES (@generation_id, @code, @name, @is_combo);