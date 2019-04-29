--210   Number of visit records with invalid care_site_id
--insert into @results_schema.heracles_results (cohort_definition_id, analysis_id, count_value)
select c1.cohort_definition_id,
  210 as analysis_id,
  cast( '' as varchar(1) ) as stratum_1, cast( '' as varchar(1) ) as stratum_2, cast( '' as varchar(1) ) as stratum_3, cast( '' as varchar(1) ) as stratum_4,
  COUNT_BIG(vo1.visit_occurrence_id) as count_value
into #results_210
from
@CDM_schema.visit_occurrence vo1
inner join #HERACLES_cohort_subject c1
on vo1.person_id = c1.subject_id
left join @CDM_schema.care_site cs1
on vo1.care_site_id = cs1.care_site_id
where vo1.care_site_id is not null
and cs1.care_site_id is null
group by c1.cohort_definition_id
;