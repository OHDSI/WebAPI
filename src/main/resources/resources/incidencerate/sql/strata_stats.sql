select r.analysis_id, r.target_id, r.outcome_id, r.strata_sequence, s.name, sum(person_count) as person_count, sum(time_at_risk) as time_at_risk, sum(cases) as cases
from @results_database_schema.ir_analysis_strata_stats r
join @results_database_schema.ir_strata s on r.analysis_id = s.analysis_id and r.strata_sequence = s.strata_sequence
where r.analysis_id = @analysis_id
GROUP BY r.analysis_id, r.target_id, r.outcome_id, r.strata_sequence, s.name
