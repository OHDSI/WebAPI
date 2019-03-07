-- 1201                Number of visits by place of service
--insert into @results_schema.heracles_results (cohort_definition_id, analysis_id, stratum_1, count_value)
select c1.cohort_definition_id,
  1201 as analysis_id,
  cs1.place_of_service_concept_id as stratum_1,
  cast( '' as varchar(1) ) as stratum_2, cast( '' as varchar(1) ) as stratum_3, cast( '' as varchar(1) ) as stratum_4,
  COUNT_BIG(visit_occurrence_id) as count_value
into #results_1201
from @CDM_schema.visit_occurrence vo1
inner join #HERACLES_cohort_subject c1
on vo1.person_id = c1.subject_id
inner join @CDM_schema.care_site cs1
on vo1.care_site_id = cs1.care_site_id
where vo1.care_site_id is not null
and cs1.place_of_service_concept_id is not null
group by c1.cohort_definition_id,
cs1.place_of_service_concept_id;