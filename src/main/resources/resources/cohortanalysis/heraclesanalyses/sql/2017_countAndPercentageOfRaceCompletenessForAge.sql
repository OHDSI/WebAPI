-- 2017                Count and percentage of race data completeness for age between 60+
--insert into @results_schema.heracles_results (cohort_definition_id, analysis_id, stratum_1, stratum_2, stratum_3)
select @cohort_definition_id as cohort_definition_id, 2017 as analysis_id, round(innerT.valid_percentage, 2) as stratum_1,
       innerT.all_data_count as stratum_2, innerT.valid_data_count as stratum_3, cast( '' as varchar(1) ) as stratum_4, (@smallcellcount + 9) as count_value
into #results_2017
from
(select valid_data.valid_data_count valid_data_count, all_data.all_data_count all_data_count,
CASE
WHEN all_data.all_data_count = 0 THEN -999
ELSE valid_data.valid_data_count * 100/all_data.all_data_count
END as valid_percentage
from
(SELECT count(distinct co.subject_id) as valid_data_count
FROM #HERACLES_cohort co
JOIN @CDM_schema.person p
ON     co.SUBJECT_ID = p.PERSON_ID
AND p.YEAR_OF_BIRTH <=
year(DATEADD(year, -60, getdate()))
LEFT JOIN @CDM_schema.concept c ON p.RACE_CONCEPT_ID = c.CONCEPT_ID
WHERE
p.RACE_CONCEPT_ID IS NOT NULL
and (lower(c.CONCEPT_NAME) not like 'other%'
and lower(c.CONCEPT_NAME) not like 'non%'
and lower(c.CONCEPT_NAME) not like '%not %'
and lower(c.CONCEPT_NAME) not like 'unknown'
)) valid_data,
(SELECT count(distinct co.subject_id) as all_data_count
FROM #HERACLES_cohort co
JOIN @CDM_schema.person p
ON     co.SUBJECT_ID = p.PERSON_ID
AND p.YEAR_OF_BIRTH <=
year(DATEADD(year, -60, getdate()))
) all_data) innerT;