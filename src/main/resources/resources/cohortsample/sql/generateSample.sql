select p.person_id,
       c.cohort_definition_id as cohort_definition_id,
       p.gender_concept_id as gender_concept_id,
       cast(year(c.cohort_start_date) - p.year_of_birth as int) as age
from @results_schema.cohort c
join @CDM_schema.person p
on p.person_id = c.subject_id
where c.cohort_definition_id = @cohort_definition_id
@expression
order by RAND()
;
