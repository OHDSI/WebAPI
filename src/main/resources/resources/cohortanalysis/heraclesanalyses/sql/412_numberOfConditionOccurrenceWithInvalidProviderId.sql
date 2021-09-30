-- 412   Number of condition occurrence records with invalid provider_id
--insert into @results_schema.heracles_results (cohort_definition_id, analysis_id, count_value)
select c1.cohort_definition_id,
  412 as analysis_id,
  cast( '' as varchar(1) ) as stratum_1, cast( '' as varchar(1) ) as stratum_2, cast( '' as varchar(1) ) as stratum_3, cast( '' as varchar(1) ) as stratum_4,
  COUNT_BIG(co1.condition_occurrence_id) as count_value
into #results_412
from
@CDM_schema.condition_occurrence co1
inner join #HERACLES_cohort_subject c1
on co1.person_id = c1.subject_id
left join @CDM_schema.provider p1
on p1.provider_id =   {@CDM_version == '4'}?{ co1.associated_provider_id } {@CDM_version == '5'}?{ co1.provider_id }
where {@CDM_version == '4'}?{ co1.associated_provider_id } {@CDM_version == '5'}?{ co1.provider_id }  is not null
and p1.provider_id is null
group by c1.cohort_definition_id
;