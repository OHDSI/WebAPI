select 
              count(distinct subject_id) people
              , gender
              , age
              , conditions
              , drugs
from
(
       select 
                     c.*
                     , gc.concept_name as gender
                     , cast(floor((YEAR(cohort_start_date) - year_of_birth) / 10) * 10 as varchar(5)) + '-' +
                           cast(floor((YEAR(cohort_start_date) - year_of_birth) / 10 + 1) * 10 - 1 as varchar(5)) as age
                     , conditions.conditions
                     , drugs.drugs
       from 
              @resultsTableQualifier.cohort c
       join 
              (
                     select person_id, 
                           ROUND(count(*), CAST(- floor(LOG10(abs(count(*) + 0.01)))  AS INT)) conditions
                     from @tableQualifier.condition_occurrence co
                     group by person_id
              )  as conditions
       on c.subject_id = conditions.person_id
       join 
              (
                     select person_id, 
                           ROUND(count(*), CAST(- floor(LOG10(abs(count(*) + 0.01)))  AS INT)) drugs
                     from @tableQualifier.drug_exposure de 
                     group by person_id
              ) as drugs
       			on c.subject_id = drugs.person_id
       join 
              @tableQualifier.person p 
       			on c.subject_id = p.person_id
       join 
              @tableQualifier.concept gc 
       			on p.gender_concept_id = gc.concept_id
       where cohort_definition_id = @cohortDefinitionId
) as f
group by gender, age, conditions, drugs
order by 1 desc
