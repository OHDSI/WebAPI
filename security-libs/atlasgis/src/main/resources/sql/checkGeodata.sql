SELECT
    location_history_id,
    location_id,
    relationship_type_concept_id,
    domain_id,
    entity_id,
    start_date,
    end_date
FROM @cdmSchema.location_history
WHERE location_history_id = 1
