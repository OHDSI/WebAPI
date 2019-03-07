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
--}

--{109 IN (@list_of_analysis_ids) | 110 IN (@list_of_analysis_ids) | 116 IN (@list_of_analysis_ids) | 117 IN (@list_of_analysis_ids)}?{

IF OBJECT_ID('tempdb..#tmp_years', 'U') IS NOT NULL
DROP TABLE  #tmp_years;

IF OBJECT_ID('tempdb..#tmp_months', 'U') IS NOT NULL
DROP TABLE  #tmp_months;

WITH x AS (SELECT 0 AS n UNION ALL SELECT 1 AS n UNION ALL SELECT 2 AS n UNION ALL SELECT 3 AS n UNION ALL SELECT 4 AS n UNION ALL SELECT 5 AS n UNION ALL SELECT 6 AS n UNION ALL SELECT 7 AS n UNION ALL SELECT 8 AS n UNION ALL SELECT 9 AS n),
    years AS (SELECT ones.n + 10*tens.n + 100*hundreds.n + 1000*thousands.n as year
              FROM x ones,     x tens,      x hundreds,       x thousands)
SELECT year
INTO #tmp_years FROM years WHERE year BETWEEN 1900 AND 2201;

WITH months AS (SELECT 1 AS month UNION ALL SELECT 2 AS month UNION ALL SELECT 3 AS month UNION ALL SELECT 4 AS month UNION ALL SELECT 5 AS month UNION ALL SELECT 6 AS month UNION ALL SELECT 7 AS month UNION ALL SELECT 8 AS month UNION ALL SELECT 9 AS month UNION ALL SELECT 10 AS month UNION ALL SELECT 11 AS month UNION ALL SELECT 12 AS month)
SELECT month
INTO #tmp_months FROM months;
