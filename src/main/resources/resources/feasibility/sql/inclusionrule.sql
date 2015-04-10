INSERT INTO #inclusionRuleCohorts (inclusion_rule_id, subject_id, cohort_start_date, cohort_end_date)
select @inclusion_rule_id as inclusion_rule_id, person_id as subject_id, start_date as cohort_start_date, end_date as cohort_end_date
FROM 
(
  select person_id, start_date, end_date
  FROM #PrimaryCriteriaEvents
  @additionalCriteriaQuery
) Results
;