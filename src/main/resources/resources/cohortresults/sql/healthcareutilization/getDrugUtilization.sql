select N1.cohort_definition_id
	, N1.stratum_1
	, N1.stratum_2
	, N1.stratum_3
	{@is_summary == FALSE} ? {, P.period_type
	, P.period_start_date
	, P.period_end_date } : {, C.concept_name as drug_concept_name}
	, N1.count_value as person_total -- subjects with records in period
	, ((N1.count_value * 1.0)/D1.count_value) * 100.0 as person_percent -- subjects with records in period/subjects in period
	, N2.total as records_total -- total records
	, (N2.total * 1000.0)/D1.count_value as records_per_1000 --total records in period/subjects in period
	, (N2.total * 1000.0)/N1.count_value as records_per_1000_with_record -- total reocrds in period/subjects with visits in period
	, (((N2.total/D1.total)*1000) /365.25) as records_per_1000_per_year --total records in period/exposure in period
	, N3.total as days_supply_total  -- total days supply
	, N3.avg_value as days_supply_average  -- average ddays supply
	, (((N3.total/D1.total)*1000) /365.25) as days_supply_per_1000_per_year --total days supply in period/exposure in period
	, N4.total as quantity_total	-- total quantity
	, N4.avg_value as quantity_average -- average quantity
	, (((N4.total/D1.total)*1000) /365.25) as quantity_per_1000_per_year --total quantity in period/exposure in period
FROM
(
	select cohort_definition_id, stratum_1, stratum_2, stratum_3, count_value
	from @results_schema.heracles_results
	where analysis_id = @subject_with_records_analysis_id
/*
	(Report 1) --> (analysis_id1 4012) -- 4012 Number of subjects with Drug Exposure by period_id, by drug_concept_id, by drug_type_concept_id in the 365d prior to first cohort start date
	(Report 2) --> (analysis_id1 4016) -- 4016 Number of subjects with Drug Exposure by period_id, by drug_concept_id during the cohort period
*/
) N1 -- number of subjects with records
JOIN
(
	select cohort_definition_id, stratum_1, stratum_2, stratum_3, count_value, avg_value, (count_value * 1.0 * avg_value) as total
	from @results_schema.heracles_results_dist
	where analysis_id = @drug_analysis_id
/*
(Report 1) --> (analysis_id2 4013) -- 4013 Distribution of number of Drug Exposure records per subject, by period_id, by drug_concept_id in 365d prior to first cohort start date
(Report 2) --> (analysis_id2 4017) -- 4017 Distribution of number of Drug Exposure records per subject, by period_id, by drug_concept_id during the cohort period
*/
) N2 on  N1.cohort_definition_id = N2.cohort_definition_id -- distribution of records per subject
    and N1.stratum_1 = N2.stratum_1
    and N1.stratum_2 = N2.stratum_2
    and N1.stratum_3 = N2.stratum_3
left join
(
	select cohort_definition_id, stratum_1, stratum_2, stratum_3, count_value, avg_value, (count_value * 1.0 * avg_value) as total
	from @results_schema.heracles_results_dist
	where analysis_id = @days_supply_analysis_id
/*
	(Report 1) --> (analysis_id3 4014) -- 4014	Distribution of greater than 0 drug day supply per subject by period_id, by drug_concept_id in the 365d prior to first cohort start date
	(Report 2) --> (analysis_id3 4018) -- 4018	Distribution of greater than 0 drug day supply per subject by period_id, by drug_concept_id during the cohort period
*/
) N3 on N1.cohort_definition_id = N3.cohort_definition_id -- distribution of days_supply per subject
	and N1.stratum_1 = N3.stratum_1
	and N1.stratum_2 = N3.stratum_2
	and N1.stratum_3 = N3.stratum_3
left join
(
	select cohort_definition_id, stratum_1, stratum_2, stratum_3, count_value, avg_value, (count_value * 1.0 * avg_value) as total
	from @results_schema.heracles_results_dist
	where analysis_id = @quantity_analysis_id
/*
	(Report 1) --> (analysis_id3 4015) -- 4015	Distribution of greater than 0 drug quantity per subject by period_id, by drug_concept_id in the 365d prior to first cohort start date
	(Report 2) --> (analysis_id3 4019) -- 4019	Distribution of greater than 0 drug quantity per subject by period_id, by drug_concept_id during the cohort period
*/
) N4 on N1.cohort_definition_id = N4.cohort_definition_id -- distribution of quantity per subject
	and N1.stratum_1 = N4.stratum_1
	and N1.stratum_2 = N4.stratum_2
	and N1.stratum_3 = N4.stratum_3
join
(
	select cohort_definition_id, stratum_1, stratum_2, stratum_3, count_value, avg_value, (count_value * 1.0 * avg_value) as total
	from @results_schema.heracles_results_dist
	where analysis_id = @subjects_analysis_id
/*
	(Report 1) --> (analysis_id4 4000) -- 4000	Distribution of observation period days by period_id  in the 365 days prior to first cohort_start_date
	(Report 2) --> (analysis_id4 4006) -- 4006 Distribution of observation period days per subject, by period_id during cohort period
*/
) as D1 on  N1.cohort_definition_id = D1.cohort_definition_id
	and N1.stratum_1 = D1.stratum_1
{@is_summary == FALSE} ?
{left join @results_schema.heracles_periods P on N1.stratum_1 = cast(P.period_id as VARCHAR) } :
{join @vocabulary_schema.concept c on cast(c.concept_id as varchar(19)) = N1.stratum_2}
where N1.cohort_definition_id = @cohort_definition_id
{@is_summary} ? {	and N1.stratum_1 = ''} : { and P.period_type = '@period_type'
	and N1.stratum_2 = '@drug_concept_id'}
	and N1.stratum_3 = '@drug_type_concept_id'
{@is_summary == FALSE} ? {ORDER BY P.PERIOD_START_DATE}
;
