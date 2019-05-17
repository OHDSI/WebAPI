-- 2031                entropy
--INSERT INTO @results_schema.heracles_results (cohort_definition_id,
--analysis_id,
--stratum_1,
--stratum_2)
SELECT @cohort_definition_id AS cohort_definition_id,
       2031 AS analysis_id,
       entropyT.d as stratum_1,
       cast(round(entropyT.entropy, 3) as varchar(20)) as stratum_2,
       cast( '' as varchar(1) ) as stratum_3, cast( '' as varchar(1) ) as stratum_4, (@smallcellcount + 9) as count_value
into #results_2031
FROM
(select
obs_date as d,
sum(probTimesLog) entropy from
(select obs_date, value_as_string, (1.0 * cnt) / (1.0 * total_per_day) prob,
--                      (-1.0) * (((1.0 * cnt) / (1.0 * total_per_day))* log(2, (1.0 * cnt) / (1.0 * total_per_day))) probTimesLog,
(-1.0) * (((1.0 * cnt) / (1.0 * total_per_day)) * (log((1.0 * cnt) / (1.0 * total_per_day))/log(2))) probTimesLog,
cnt, total_per_day
from
(select obs_date, value_as_string, cnt, sum(cnt)
over (partition by obs_date)
as total_per_day
from
(
select
all_observ.value_as_string,
DATEFROMPARTS(YEAR(all_observ.observation_date),MONTH(all_observ.observation_date),DAY(all_observ.observation_date)) as obs_date,
COUNT(*) as cnt
from
(select * from @CDM_schema.observation observ
join #HERACLES_cohort co
on co.SUBJECT_ID = observ.PERSON_ID
and observ.observation_date >= co.cohort_start_date
and observ.observation_date <= co.cohort_end_date) all_observ
GROUP BY all_observ.value_as_string, DATEFROMPARTS(YEAR(all_observ.observation_date),MONTH(all_observ.observation_date),DAY(all_observ.observation_date))
) value_day_cnt
) with_sum
) allProb
group by obs_date
) entropyT;