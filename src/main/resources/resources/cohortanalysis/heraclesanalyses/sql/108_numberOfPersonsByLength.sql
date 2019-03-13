-- 108   Number of persons by length of first observation period, in 30d increments
--insert into @results_schema.heracles_results (cohort_definition_id, analysis_id, stratum_1, count_value)
select c1.cohort_definition_id,
  108 as analysis_id,
  CAST(floor(DATEDIFF(dd, op1.observation_period_start_date, op1.observation_period_end_date)/30) AS INT) as stratum_1,
  cast( '' as varchar(1) ) as stratum_2, cast( '' as varchar(1) ) as stratum_3, cast( '' as varchar(1) ) as stratum_4,
  COUNT_BIG(p1.person_id) as count_value
into #results_108
from @CDM_schema.person p1
inner join #HERACLES_cohort_subject c1
on p1.person_id = c1.subject_id
inner join
(select person_id,
observation_period_START_DATE,
observation_period_END_DATE,
ROW_NUMBER() over (PARTITION by person_id order by observation_period_start_date asc) as rn1
from @CDM_schema.observation_period
) op1
on p1.PERSON_ID = op1.PERSON_ID
where op1.rn1 = 1
group by c1.cohort_definition_id, CAST(floor(DATEDIFF(dd, op1.observation_period_start_date, op1.observation_period_end_date)/30) AS INT)
;
