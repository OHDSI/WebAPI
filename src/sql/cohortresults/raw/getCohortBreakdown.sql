select count(distinct subject_id) as people,
			gender, age, conditions, drugs
from (
	select c.*, gc.concept_name as gender,
		CONCAT(
		  cast(floor((year(cohort_start_date) - year_of_birth) / 10) * 10 as varchar(5)), '-',
		  cast(floor((year(cohort_start_date) - year_of_birth) / 10 + 1) * 10 - 1 as varchar(5))
		) as age,
		coalesce(conditions.conditions, 0) as conditions,
		coalesce(drugs.drugs, 0) as drugs
	from @resultsTableQualifier.cohort c
	left join (
		select person_id,
			round(count(*),
			cast(- floor(log10(abs(count(*) + 0.01))) as int)) as conditions
		from @tableQualifier.condition_occurrence co
		group by person_id
	) conditions on c.subject_id = conditions.person_id
	left join (
		select person_id,
			round(count(*),
			cast(- floor(log10(abs(count(*) + 0.01))) as int)) drugs
		from @tableQualifier.drug_exposure de 
		group by person_id
	) drugs on c.subject_id = drugs.person_id
	join @tableQualifier.person p on c.subject_id = p.person_id
	join @tableQualifier.concept gc on p.gender_concept_id = gc.concept_id
	where cohort_definition_id = @cohortDefinitionId
) cohort_people
group by gender, age, conditions, drugs
order by 1 desc

