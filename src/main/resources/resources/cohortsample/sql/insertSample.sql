INSERT INTO @results_schema.cohort_sample (cohort_definition_id, size, age_min, age_max, gender_concept_id, created_by_id, created_date)
VALUES (@cohortDefinitionId, @size, @ageMin, @ageMax, @genderConceptId, @createdById, @createdDate)
;
