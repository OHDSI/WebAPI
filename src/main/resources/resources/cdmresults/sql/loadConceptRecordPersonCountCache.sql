SELECT
    concept_id,
    record_count,
    descendant_record_count,
    person_count,
    descendant_person_count
FROM @resultTableQualifier.achilles_result_concept_count;