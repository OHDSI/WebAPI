select N1.cohort_definition_id
	, N1.stratum_1
	, N1.stratum_2
	, N1.stratum_3
	, P.period_type
	, P.period_start_date
	, P.period_end_date
	, N1.count_value as person_total
	, ((N1.count_value * 1.0)/D1.count_value) * 100.0 as person_percent
	, N2.count_value as visits_total
	, (N2.count_value * 1000.0)/D1.count_value as visits_per_1000
	, (N2.count_value * 1000.0)/N1.count_value as visits_per_1000_with_visit
	, (N1.count_value * 1000.0)/(((D1.avg_value * 1.0)*D1.count_value)/365.25) as visits_per_1000_per_year
	, N3.count_value as los_total
	, (case when N3.count_value is not null then N3.count_value * 1.0 else null end)/N1.count_value as los_average
FROM
(
	select cohort_definition_id, stratum_1, stratum_2, stratum_3, count_value
	from @results_schema.heracles_results
	where analysis_id = @subject_with_records_analysis_id
/*
	(Report 1,2,3) --> (analysis_id1 4001)
	(Report 4,5,6) --> (analysis_id1 4007)
*/
) N1 -- number of subjects with records
JOIN
(
	select cohort_definition_id, stratum_1, stratum_2, stratum_3, count_value, avg_value
	from @results_schema.heracles_results_dist
	where analysis_id = @visit_stat_analysis_id
/*
(Report 1) --> (analysis_id2 4002)
(Report 2) --> (analysis_id2 4003)
(Report 3) --> (analysis_id2 4004)
(Report 4) --> (analysis_id2 4008)
(Report 5) --> (analysis_id2 4009)
(Report 6) --> (analysis_id2 4010)
*/
) N2 -- distribution of records per subject
	on  N1.cohort_definition_id = N2.cohort_definition_id
    and N1.stratum_1 = N2.stratum_1
    and N1.stratum_2 = N2.stratum_2
    and N1.stratum_3 = N2.stratum_3
left join
(
	select cohort_definition_id, stratum_1, stratum_2, stratum_3, count_value, avg_value
	from @results_schema.heracles_results_dist
	where analysis_id = @los_analysis_id
/*
	(Report 1,2,3) --> (analysis_id3 4005)
	(Report 4,5,6) --> (analysis_id3 4011)
*/
) N3 on N1.cohort_definition_id = N3.cohort_definition_id
	and N1.stratum_1 = N3.stratum_1
	and N1.stratum_2 = N3.stratum_2
	and N1.stratum_3 = N3.stratum_3
join
(
	select cohort_definition_id, stratum_1, stratum_2, stratum_3, count_value, avg_value
	from @results_schema.heracles_results_dist
	where analysis_id = @subjects_analysis_id
/*
	(Report 1,2,3) --> (analysis_id4 4000)
	(Report 4,5,6) --> (analysis_id4 4006)
*/
) as D1 on  N1.cohort_definition_id = D1.cohort_definition_id
	and N1.stratum_1 = D1.stratum_1
	and N1.stratum_2 = D1.stratum_2
	and N1.stratum_3 = D1.stratum_3
left join @results_schema.heracles_periods P on cast(N1.stratum_1 as integer) = P.period_id
where N1.cohort_definition_id = @cohort_definition_id
{@is_summary} ? {	and N1.stratum_1 = ''} : { and P.period_type = '@period_type'}
	and N1.stratum_2 = '@visit_concept_id'
	and N2.stratum_3 = '@visit_type_concept_id'
;
