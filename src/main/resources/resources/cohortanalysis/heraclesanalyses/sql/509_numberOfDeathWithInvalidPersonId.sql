-- 509   Number of death records with invalid person_id
--insert into @results_schema.heracles_results (cohort_definition_id, analysis_id, count_value)
select c1.cohort_definition_id, 509 as analysis_id,
                                cast( '' as varchar(1) ) as stratum_1, cast( '' as varchar(1) ) as stratum_2, cast( '' as varchar(1) ) as stratum_3, cast( '' as varchar(1) ) as stratum_4,
                                COUNT_BIG(d1.PERSON_ID) as count_value
into #results_509
from
@CDM_schema.death d1
inner join #HERACLES_cohort_subject c1
on d1.person_id = c1.subject_id
left join @CDM_schema.person p1
on d1.person_id = p1.person_id
where p1.person_id is null
group by c1.cohort_definition_id
;