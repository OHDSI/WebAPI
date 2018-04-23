with cost_long as
(
	select cohort_definition_id, stratum_1, stratum_2, stratum_3, stratum_4, count_value*avg_value as total
	from @results_schema.heracles_results_dist
	where analysis_id = @cost_analysis_id
				and cohort_definition_id = @cohort_definition_id
				and stratum_5 = '@cost_type_concept_id' -- default is 31968. @cost_type_concept_id may be used as a filter in UI
				and stratum_4 in ('31978', '31973', '31980') -- allowed, charged, paid
				/*
					(Report 1,2,3) --> (analysis_id1 4022) -- 4022	Distribution of greater than 0 US$ cost per subject by period_id, by drug_concept_id, by drug_type_concept_id, by cost_concept_id in the 365d prior to first cohort start date
					(Report 4,5,6) --> (analysis_id1 4023) -- 4023	Distribution of greater than 0 US$ cost per subject by period_id, by drug_concept_id, by drug_type_concept_id, by cost_concept_id, by cost_type_concept_id during the cohort period

				*/
)
, cost_wide as
(
	select cl.cohort_definition_id, cl.stratum_1, cl.stratum_2, cl.stratum_3
				, case when allowed.total is null then 0 else allowed.total end as allowed
				, case when charged.total is null then 0 else charged.total end as charged
				, case when paid.total is null then 0 else paid.total end as paid
	from
	(
		select distinct cohort_definition_id, stratum_1, stratum_2, stratum_3
		from cost_long
	) cl
	left join
	(
			select cohort_definition_id, stratum_1, stratum_2, stratum_3, total
			from cost_long
			where cast(stratum_4 as integer) = 31978 -- allowed
	) as allowed
	on cl.cohort_definition_id = allowed.cohort_definition_id and cl.stratum_1 = allowed.stratum_1 and cl.stratum_2 = allowed.stratum_2 and cl.stratum_3 = allowed.stratum_3
	left join
	(
			select cohort_definition_id, stratum_1, stratum_2, stratum_3, total
			from cost_long
			where cast(stratum_4 as integer) = 31973 -- charged
	) as charged
	on cl.cohort_definition_id = charged.cohort_definition_id and cl.stratum_1 = charged.stratum_1 and cl.stratum_2 = charged.stratum_2 and cl.stratum_3 = charged.stratum_3
	left join
	(
			select cohort_definition_id, stratum_1, stratum_2, stratum_3, total
			from cost_long
			where cast(stratum_4 as integer) = 31980 -- paid
	) as paid
	on cl.cohort_definition_id = paid.cohort_definition_id and cl.stratum_1 = paid.stratum_1 and cl.stratum_2 = paid.stratum_2 and cl.stratum_3 = paid.stratum_3
)
select N1.cohort_definition_id
	, N1.stratum_1
	, N1.stratum_2
	, N1.stratum_3
	{@is_summary == FALSE} ? {, P.period_type
	, P.period_start_date
	, P.period_end_date } : 
{	, C.concept_name as drug_concept_name
	, C.concept_class_id as drug_concept_class
	, C.vocabulary_id as drug_vocabulary_id }
	, N1.count_value as person_total -- subjects with records in period
	, ((N1.count_value * 1.0)/nullif(D1.count_value,0)) * 100.0 as person_percent -- subjects with records in period/subjects in period
	, N2.total as records_total -- total records
	, (N2.total * 1000.0)/nullif(D1.count_value,0) as records_per_1000 --total records in period/subjects in period
	, (N2.total * 1000.0)/nullif(N1.count_value,0) as records_per_1000_with_record -- total reocrds in period/subjects with visits in period
	, (((N2.total/nullif(D1.total,0))*1000)*365.25) as records_per_1000_per_year --total records in period/exposure in period
	, N3.total as days_supply_total  -- total days supply
	, N3.avg_value as days_supply_average  -- average ddays supply
	, (((N3.total/nullif(D1.total,0))*1000)*365.25) as days_supply_per_1000_per_year --total days supply in period/exposure in period
	, N4.total as quantity_total	-- total quantity
	, N4.avg_value as quantity_average -- average quantity
	, (((N4.total/nullif(D1.total,0))*1000)*365.25) as quantity_per_1000_per_year --total quantity in period/exposure in period
	, cst.allowed -- Allowed
	, (cst.allowed/nullif(D1.total,0))*30.42 as allowed_pmpm -- allowed per member per month
	, cst.charged -- Charged
	, (cst.charged/nullif(D1.total,0))*30.42 as charged_pmpm -- charged per member per month
	, cst.paid -- paid
	, (cst.paid/nullif(D1.total,0))*30.42 as paid_pmpm -- paid per member per month
	, cst.allowed/nullif(cst.charged,0) as allowed_charged -- allowed to charged ratio
	, cst.paid/nullif(cst.allowed,0) as paid_allowed -- paid to allowed ratio
FROM
(
	select cohort_definition_id, stratum_1, stratum_2, stratum_3, count_value
	from @results_schema.heracles_results
	where analysis_id = @subject_with_records_analysis_id
	AND stratum_2 <> ''
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
left JOIN
cost_wide cst on N1.cohort_definition_id = cst.cohort_definition_id
	and N1.stratum_1 = cst.stratum_1
	and N1.stratum_2 = cst.stratum_2
	and N1.stratum_3 = cst.stratum_3
{@is_summary == FALSE} ?
{left join @results_schema.heracles_periods P on N1.stratum_1 = cast(P.period_id as VARCHAR(255)) } :
{join @vocabulary_schema.concept c on c.concept_id = cast(N1.stratum_2 as INTEGER)}
where N1.cohort_definition_id = @cohort_definition_id
{@is_summary} ? {	and N1.stratum_1 = ''} : { and P.period_type = '@period_type'
	and N1.stratum_2 = '@drug_concept_id'}
	and N1.stratum_3 = '@drug_type_concept_id'
{@is_summary == FALSE} ? {ORDER BY P.PERIOD_START_DATE}
;
