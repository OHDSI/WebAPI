create table #PrimaryCriteriaEvents
(
	person_id bigint,
	start_date datetime,
	end_date datetime,
	op_start_date datetime,
	op_end_date datetime
)
;

INSERT INTO #PrimaryCriteriaEvents (person_id, start_date, end_date, op_start_date, op_end_date)
select c.subject_id, c.cohort_start_date, max(c.cohort_end_date) as end_date, op.observation_period_start_date, op.observation_period_end_date
FROM @cohortTable C
JOIN @CDM_schema.observation_period OP on C.subject_id = OP.person_id and C.cohort_start_date between OP.observation_period_start_date and OP.observation_period_end_date
WHERE cohort_definition_id = @indexCohortId
GROUP BY c.subject_id, c.cohort_start_date, op.observation_period_start_date, op.observation_period_end_date
;
