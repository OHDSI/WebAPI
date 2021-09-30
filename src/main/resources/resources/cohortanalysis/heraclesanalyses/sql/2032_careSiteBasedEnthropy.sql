-- 2032                care_site based entropy
--INSERT INTO @results_schema.heracles_results (cohort_definition_id,
--analysis_id,
--stratum_1,
--stratum_2,
--stratum_3,
--stratum_4)
SELECT @cohort_definition_id AS cohort_definition_id,
       2032 AS analysis_id,
       entropyT.care_site_id as stratum_1,
       entropyT.site_source_value as stratum_2,
       entropyT.d as stratum_3,
       cast(round(entropyT.entropy, 3) as varchar(20)) as stratum_4,
       (@smallcellcount + 9) as count_value
into #results_2032
FROM
(
SELECT care_site_id     AS care_site_id,
site_source_value AS site_source_value,
obs_date         AS d,
SUM (probTimesLog) entropy
FROM (SELECT care_site_id,
site_source_value,
obs_date,
value_as_string,
(1.0 * cnt) / (1.0 * total_per_day) prob,
(-1.0) * (((1.0 * cnt) / (1.0 * total_per_day)) * (log((1.0 * cnt) / (1.0 * total_per_day))/log(2))) probTimesLog,
cnt,
total_per_day
FROM (SELECT care_site_id,
site_source_value,
obs_date,
value_as_string,
cnt,
SUM (cnt) OVER (PARTITION BY care_site_id, obs_date)
AS total_per_day
FROM (SELECT
all_observ.CARE_SITE_ID  AS care_site_id,
all_observ.site_source_value AS site_source_value,
all_observ.value_as_string AS value_as_string,
DATEFROMPARTS(YEAR(all_observ.observation_date),MONTH(all_observ.observation_date),DAY(all_observ.observation_date)) as obs_date,
COUNT(*) AS cnt
FROM (SELECT CASE
WHEN caresite.CARE_SITE_ID IS NULL
THEN
-1
ELSE
caresite.CARE_SITE_ID
END
AS care_site_id,
caresite.CARE_SITE_SOURCE_VALUE
AS site_source_value,
observ.*
FROM @CDM_schema.observation observ
JOIN #HERACLES_cohort co
ON     co.SUBJECT_ID =
observ.PERSON_ID
AND observ.observation_date >=
co.cohort_start_date
AND observ.observation_date <=
co.cohort_end_date
LEFT JOIN @CDM_schema.provider provider
ON provider.PROVIDER_ID =
observ.PROVIDER_ID
LEFT JOIN @CDM_schema.care_site caresite
ON caresite.CARE_SITE_ID =
provider.CARE_SITE_ID)
all_observ
GROUP BY all_observ.CARE_SITE_ID,
all_observ.site_source_value,
all_observ.value_as_string,
DATEFROMPARTS(YEAR(all_observ.observation_date),MONTH(all_observ.observation_date),DAY(all_observ.observation_date))
) value_day_cnt) with_sum) allProb
GROUP BY care_site_id, site_source_value, obs_date
) entropyT;