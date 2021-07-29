-- 109   Number of persons with continuous observation in each year
-- Note: using temp table instead of nested query because this gives vastly improved performance in Oracle

WITH op_date_range AS (select
                         min(observation_period_start_date) op_start_date,
                         max(observation_period_end_date) op_end_date
                       from @CDM_schema.person p1
                         inner join #HERACLES_cohort_subject c1
                                     on p1.person_id = c1.subject_id
inner join
@CDM_schema.observation_period op1
on p1.person_id = op1.person_id)
select year as obs_year, DATEFROMPARTS(year, 1, 1) as obs_year_start, DATEFROMPARTS(year,12,31) as obs_year_end
into #temp_dates_1
from #tmp_years, op_date_range where year between YEAR(op_date_range.op_start_date) AND YEAR(op_date_range.op_end_date)
;


--INSERT INTO @results_schema.heracles_results (cohort_definition_id, analysis_id, stratum_1, count_value)
select c1.cohort_definition_id,
  109 AS analysis_id,
  obs_year AS stratum_1,
  cast( '' as varchar(1) ) as stratum_2, cast( '' as varchar(1) ) as stratum_3, cast( '' as varchar(1) ) as stratum_4,
  COUNT_BIG(DISTINCT p1.person_id) AS count_value
into #results_109
FROM @CDM_schema.person p1
inner join #HERACLES_cohort_subject c1
on p1.person_id = c1.subject_id
inner join
@CDM_schema.observation_period op1
on p1.person_id = op1.person_id,
#temp_dates_1
WHERE
observation_period_start_date <= obs_year_start
AND
observation_period_end_date >= obs_year_end
GROUP BY
c1.cohort_definition_id, obs_year
;

TRUNCATE TABLE #temp_dates_1;
DROP TABLE #temp_dates_1;