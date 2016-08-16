select count(distinct subject_id) people,
		gender, age, conditions, drugs
from
(select c.*, gc.concept_name as gender,
        cast(floor((year(cohort_start_date) - year_of_birth) / 10) * 10 as varchar(5)) + '-' +
            cast(floor((year(cohort_start_date) - year_of_birth) / 10 + 1) * 10 - 1 as varchar(5)) as age,
        (select round(count(*), cast(- floor(log10(abs(count(*) + 0.01))) as int)) 
         from @tableQualifier.condition_occurrence co 
         where co.person_id = c.subject_id) as conditions,
        (select round(count(*), cast(- floor(log10(abs(count(*) + 0.01))) as int))
         from @tableQualifier.drug_exposure de 
         where de.person_id = c.subject_id) as drugs
from @resultsTableQualifier.cohort c
join @tableQualifier.person p on c.subject_id = p.person_id
join @tableQualifier.concept gc on p.gender_concept_id = gc.concept_id
where cohort_definition_id = @cohortDefinitionId
) cohort_people
group by gender, age, conditions, drugs
order by 1 desc
