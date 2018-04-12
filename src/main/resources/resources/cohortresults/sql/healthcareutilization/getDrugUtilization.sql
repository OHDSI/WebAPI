select N1.cohort_definition_id
	, N1.stratum_1
	, N1.stratum_2
	, N1.stratum_3
	{@is_summary == FALSE} ? {, P.period_type
	, P.period_start_date
	, P.period_end_date } : {, C.concept_name as drug_concept_name}
	, N1.count_value as person_total
	, ((N1.count_value * 1.0)/D1.count_value) * 100.0 as person_percent
	, N2.count_value as exposures_total
	, (N2.count_value * 1000.0)/D1.count_value as exposures_per_1000
	, (N2.count_value * 1000.0)/N1.count_value as exposures_per_1000_with_exposure
	, (N1.count_value * 1000.0)/(((D1.avg_value * 1.0)*D1.count_value)/365.25) as exposures_per_1000_per_year
	, N3.count_value as days_supply_total
	, (case when N3.count_value is not null then N3.count_value * 1.0 else null end)/N1.count_value as days_supply_average
	, N4.count_value as quantity_total
	, (case when N4.count_value is not null then N4.count_value * 1.0 else null end)/N1.count_value as quantity_average
FROM
(
	select cohort_definition_id, stratum_1, stratum_2, stratum_3, count_value
	from @results_schema.heracles_results
	where analysis_id = @subject_with_records_analysis_id
/*
	(Report 1) --> (analysis_id1 4012)
	(Report 2) --> (analysis_id1 4016)
*/
) N1 -- number of subjects with records
JOIN
(
	select cohort_definition_id, stratum_1, stratum_2, stratum_3, count_value, avg_value
	from @results_schema.heracles_results_dist
	where analysis_id = @drug_analysis_id
/*
(Report 1) --> (analysis_id2 4013)
(Report 2) --> (analysis_id2 4017)
*/
) N2 on  N1.cohort_definition_id = N2.cohort_definition_id -- distribution of records per subject
    and N1.stratum_1 = N2.stratum_1
    and N1.stratum_2 = N2.stratum_2
    and N1.stratum_3 = N2.stratum_3
left join
(
	select cohort_definition_id, stratum_1, stratum_2, stratum_3, count_value, avg_value
	from @results_schema.heracles_results_dist
	where analysis_id = @days_supply_analysis_id
/*
	(Report 1) --> (analysis_id3 4014)
	(Report 2) --> (analysis_id3 4018)
*/
) N3 on N1.cohort_definition_id = N3.cohort_definition_id -- distribution of days_supply per subject
	and N1.stratum_1 = N3.stratum_1
	and N1.stratum_2 = N3.stratum_2
	and N1.stratum_3 = N3.stratum_3
left join
(
	select cohort_definition_id, stratum_1, stratum_2, stratum_3, count_value, avg_value
	from @results_schema.heracles_results_dist
	where analysis_id = @quantity_analysis_id
/*
	(Report 1) --> (analysis_id3 4015)
	(Report 2) --> (analysis_id3 4019)
*/
) N4 on N1.cohort_definition_id = N4.cohort_definition_id -- distribution of quantity per subject
	and N1.stratum_1 = N4.stratum_1
	and N1.stratum_2 = N4.stratum_2
	and N1.stratum_3 = N4.stratum_3
join
(
	select cohort_definition_id, stratum_1, stratum_2, stratum_3, count_value, avg_value
	from @results_schema.heracles_results_dist
	where analysis_id = @subjects_analysis_id
/*
	(Report 1) --> (analysis_id4 4000)
	(Report 2) --> (analysis_id4 4006)
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
