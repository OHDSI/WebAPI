TRUNCATE TABLE @results_schema.pathway_analysis_paths;

INSERT INTO @results_schema.pathway_analysis_paths (pathway_analysis_generation_id, target_cohort_id, step_1, step_2, step_3, step_4, step_5, step_6, step_7, step_8, step_9, step_10, count_value)
select pathway_analysis_generation_id, target_cohort_id,
	step_1, step_2, step_3, step_4, step_5, step_6, step_7, step_8, step_9, step_10,
  count_big(subject_id) as count_value
from
(
  select e.pathway_analysis_generation_id, e.target_cohort_id, e.subject_id,
    MAX(case when ordinal = 1 then combo_id end) as step_1,
    MAX(case when ordinal = 2 then combo_id end) as step_2,
    MAX(case when ordinal = 3 then combo_id end) as step_3,
    MAX(case when ordinal = 4 then combo_id end) as step_4,
    MAX(case when ordinal = 5 then combo_id end) as step_5,
    MAX(case when ordinal = 6 then combo_id end) as step_6,
    MAX(case when ordinal = 7 then combo_id end) as step_7,
    MAX(case when ordinal = 8 then combo_id end) as step_8,
    MAX(case when ordinal = 9 then combo_id end) as step_9,
    MAX(case when ordinal = 10 then combo_id end) as step_10
  from @results_schema.pathway_analysis_events e
	GROUP BY e.pathway_analysis_generation_id, e.target_cohort_id, e.subject_id
) t1
group by pathway_analysis_generation_id, target_cohort_id, 
	step_1, step_2, step_3, step_4, step_5, step_6, step_7, step_8, step_9, step_10
;
