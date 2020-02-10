SELECT *
FROM (
     SELECT
         p.person_id AS person_id,
         c.cohort_definition_id AS cohort_definition_id,
         p.gender_concept_id AS gender_concept_id,
         cast(year(c.cohort_start_date) - p.year_of_birth AS INT) AS age
     FROM @results_schema.cohort  c
          JOIN @CDM_schema.person p
               ON p.person_id = c.subject_id
) c
WHERE c.cohort_definition_id = @cohort_definition_id
@expression
ORDER BY RAND()
;