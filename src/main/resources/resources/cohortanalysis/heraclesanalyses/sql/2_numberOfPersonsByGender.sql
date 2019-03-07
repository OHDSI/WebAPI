-- 2       Number of persons by gender
--insert into @results_schema.heracles_results (cohort_definition_id, analysis_id, stratum_1, count_value)
select c1.cohort_definition_id, 2 as analysis_id,  gender_concept_id as stratum_1,
                                cast( '' as varchar(1) ) as stratum_2, cast( '' as varchar(1) ) as stratum_3, cast( '' as varchar(1) ) as stratum_4,
                                COUNT_BIG(person_id) as count_value
into #results_2
from @CDM_schema.person p1
inner join #HERACLES_cohort_subject c1
on p1.person_id = c1.subject_id
group by c1.cohort_definition_id, GENDER_CONCEPT_ID
;
