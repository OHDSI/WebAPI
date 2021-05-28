SELECT
    concept_id,
    record_count,
    descendant_record_count,
    record_count as person_count,
    descendant_record_count as descendant_person_count
FROM @resultTableQualifier.achilles_result_concept_count;