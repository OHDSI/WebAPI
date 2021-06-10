-- 0       Number of persons
--insert into @results_schema.heracles_results (cohort_definition_id, analysis_id, stratum_1, count_value)
select c1.cohort_definition_id,
  0 as analysis_id,
  '@source_name' as stratum_1, cast( '' as varchar(1) ) as stratum_2, cast( '' as varchar(1) ) as stratum_3, cast( '' as varchar(1) ) as stratum_4,
  COUNT_BIG(person_id) as count_value
into #results_0
from @CDM_schema.person p1
inner join #HERACLES_cohort_subject c1
on p1.person_id = c1.subject_id
group by c1.cohort_definition_id;

--insert into @results_schema.heracles_results_dist (cohort_definition_id, analysis_id, stratum_1, count_value)
select c1.cohort_definition_id, 0 as analysis_id, '@source_name' as stratum_1, cast( '' as varchar(1) ) as stratum_2,
                                cast( '' as varchar(1) ) as stratum_3,cast( '' as varchar(1) ) as stratum_4, cast( '' as varchar(1) ) as stratum_5,
                                0 as min_value, 0 as max_value, 0 as avg_value, 0 as stdev_value, 0 as median_value,
                                0 as p10_value, 0 as p25_value, 0 as p75_value, 0 as p90_value,
                                COUNT_BIG(person_id) as count_value
into #results_dist_0
from @CDM_schema.person p1
inner join #HERACLES_cohort_subject c1
on p1.person_id = c1.subject_id
group by c1.cohort_definition_id;
