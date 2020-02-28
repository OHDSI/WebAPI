SELECT *
FROM (
     SELECT
         cohort.subject_id AS person_id,
         cohort.cohort_definition_id AS cohort_definition_id,
         person.gender_concept_id AS gender_concept_id,
         CAST(year(cohort.cohort_start_date) - person.year_of_birth AS INT) AS age
     FROM @results_schema.cohort
     JOIN @CDM_schema.person ON person_id = subject_id
) AS cte
WHERE cohort_definition_id = @cohort_definition_id
      @expression
ORDER BY RAND()
;