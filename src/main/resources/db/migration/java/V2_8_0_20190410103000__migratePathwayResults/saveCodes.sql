INSERT INTO @results_schema.pathway_analysis_codes (pathway_analysis_generation_id, code, name, is_combo)
        VALUES (?,?,CAST(? AS VARCHAR(2000)),?);