select N1.cohort_definition_id
	, N1.stratum_1
	, N1.stratum_2
	, N1.stratum_3
{@is_summary == FALSE} ? {
	, P.period_type
	, P.period_start_date
	, P.period_end_date }
	, N1.count_value as person_total -- subjects with records in period
	, ((N1.count_value * 1.0)/D1.count_value) * 100.0 as person_percent -- subjects with records in period/subjects in period
	, N2.total as visits_total -- total visits
	, (N2.total * 1000.0)/D1.count_value as visits_per_1000 --total records in period/subjects in period
	, (N2.total * 1000.0)/N1.count_value as visits_per_1000_with_visit -- total reocrds in period/subjects with visits in period
	, (((N2.total/D1.total)*1000) /365.25) as visits_per_1000_per_year --total visits in period/exposure in period
	, N3.total as los_total -- total length of stay
	, N3.avg_value as los_average -- average length of stay
FROM
(
	select cohort_definition_id, stratum_1, stratum_2, stratum_3, count_value
	from @results_schema.heracles_results
	where analysis_id = @subject_with_records_analysis_id
/*
	(Report 1,2,3) --> (analysis_id1 4001) -- 4001 Number of subjects with visits by period_id, by visit_concept_id, by visit_type_concept_id in the 365d prior to first cohort start date
	(Report 4,5,6) --> (analysis_id1 4007) -- 4007 Number of subjects with visits by period_id, by visit_concept_id, by visit_type_concept_id during the cohort period
*/
) N1 -- number of subjects with records
JOIN
(
	select cohort_definition_id, stratum_1, stratum_2, stratum_3, count_value, avg_value, count_value*avg_value as total
	from @results_schema.heracles_results_dist
	where analysis_id = @visit_stat_analysis_id
/*
(Report 1) --> (analysis_id2 4002) -- 4002 Distribution of number of visit occurrence records per subject by period_id, by visit_concept_id, by visit_type_concept_id  in 365d prior to cohort start date
(Report 2) --> (analysis_id2 4003) -- 4003 Distribution of number of visit dates per subject by period_id, by visit_concept_id, by visit_type_concept_id in 365d prior to first cohort start date
(Report 3) --> (analysis_id2 4004) -- 4004 Distribution of number of care_site+visit dates per subject by period_id, by visit_concept_id, by visit_type_concept_id in 365d prior to first cohort start date
(Report 4) --> (analysis_id2 4008) -- 4008 Distribution of number of visit occurrence records per subject by period_id, by visit_concept_id, by visit_type_concept_id during the cohort period
(Report 5) --> (analysis_id2 4009) -- 4009 Distribution of number of visit dates per subject by period_id, by visit_concept_id, by visit_type_concept_id in 365d prior to first cohort start date
(Report 6) --> (analysis_id2 4010) -- 4010 Distribution of number of care_site+visit dates per subject by period_id, by visit_concept_id, by visit_type_concept_id in 365d prior to first cohort start date
*/
) N2 -- distribution of records per subject
	on  N1.cohort_definition_id = N2.cohort_definition_id
    and N1.stratum_1 = N2.stratum_1
    and N1.stratum_2 = N2.stratum_2
    and N1.stratum_3 = N2.stratum_3
left join
(
	select cohort_definition_id, stratum_1, stratum_2, stratum_3, count_value, avg_value, count_value * avg_value as total
	from @results_schema.heracles_results_dist
	where analysis_id = @los_analysis_id
/*
	(Report 1,2,3) --> (analysis_id3 4005) -- 4005	Distribution of observation period days for inpatient visits per subject by period_id, by visit_concept_id, by visit_type_concept_id in the 365 days prior to first cohort_start_date
	(Report 4,5,6) --> (analysis_id3 4011) -- 4011	Distribution of observation period days for inpatient visits per subject by period_id, by visit_concept_id, by visit_type_concept_id during cohort period
*/
) N3 on N1.cohort_definition_id = N3.cohort_definition_id
	and N1.stratum_1 = N3.stratum_1
	and N1.stratum_2 = N3.stratum_2
	and N1.stratum_3 = N3.stratum_3
join
(
	select cohort_definition_id, stratum_1, stratum_2, stratum_3, count_value, avg_value, count_value * avg_value as total --exposure
	from @results_schema.heracles_results_dist
	where analysis_id = @subjects_analysis_id
/*
	(Report 1,2,3) --> (analysis_id4 4000) -- 4000	Distribution of observation period days by period_id  in the 365 days prior to first cohort_start_date
	(Report 4,5,6) --> (analysis_id4 4006) -- 4006 Distribution of observation period days per subject, by period_id during cohort period
*/
) as D1 -- subjects in time period
on  N1.cohort_definition_id = D1.cohort_definition_id
	and N1.stratum_1 = D1.stratum_1
{@is_summary == FALSE} ? {left join @results_schema.heracles_periods P on N1.stratum_1 = cast(P.period_id as VARCHAR) }
where N1.cohort_definition_id = @cohort_definition_id
{@is_summary} ? {	and N1.stratum_1 = ''} : { and P.period_type = '@period_type'}
	and N1.stratum_2 = '@visit_concept_id'
	and N2.stratum_3 = '@visit_type_concept_id'
{@is_summary == FALSE} ? {ORDER BY P.PERIOD_START_DATE}
;
