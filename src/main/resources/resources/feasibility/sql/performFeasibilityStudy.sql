@codesetQuery
@indexCohortQuery

create table #inclusionRuleCohorts 
(
  inclusion_rule_id bigint,
  event_id bigint
)
;

@inclusionCohortInserts

-- the matching group with all bits set ( POWER(2,# of inclusion rules) - 1 = inclusion_rule_mask
DELETE FROM @cohortTable where cohort_definition_id = @resultCohortId;
INSERT INTO @cohortTable (cohort_definition_id, subject_id, cohort_start_date, cohort_end_date)
select @resultCohortId as cohort_definition_id, MG.person_id, MG.start_date, MG.end_date
from
(
  select C.event_id, C.person_id, C.start_date, C.end_date, SUM(coalesce(POWER(cast(2 as bigint), I.inclusion_rule_id), 0)) as inclusion_rule_mask
  from #primary_events C
  LEFT JOIN #inclusionRuleCohorts I on I.event_id = C.event_id
  GROUP BY C.event_id, C.person_id, C.start_date, C.end_date
) MG -- matching groups
CROSS JOIN (select count(*) as total_rules from #inclusionRules where study_id = @studyId) RuleTotal
WHERE MG.inclusion_rule_mask = POWER(cast(2 as bigint),RuleTotal.total_rules)-1
;

-- calculate matching group counts
delete from @ohdsi_database_schema.feas_study_result where study_id = @studyId;
insert into @ohdsi_database_schema.feas_study_result (study_id, inclusion_rule_mask, person_count)
select @studyId as study_id, inclusion_rule_mask, count(*) as person_count
from
(
  select C.event_id, SUM(coalesce(POWER(cast(2 as bigint), I.inclusion_rule_id), 0)) as inclusion_rule_mask
  from #primary_events C
  LEFT JOIN #inclusionRuleCohorts I on c.event_id = i.event_id
  GROUP BY C.event_id
) MG -- matching groups
group by inclusion_rule_mask
;

-- calculate gain counts
delete from @ohdsi_database_schema.feas_study_inclusion_stats where study_id = @studyId;
insert into @ohdsi_database_schema.feas_study_inclusion_stats (study_id, rule_sequence, name, person_count, gain_count, person_total)
select ir.study_id, ir.sequence, ir.name, coalesce(T.person_count, 0) as person_count, coalesce(SR.person_count, 0) gain_count, EventTotal.total
from #inclusionRules ir
left join
(
  select i.inclusion_rule_id, count(i.event_id) as person_count
  from #primary_events C
  JOIN #inclusionRuleCohorts i on C.event_id = i.event_id
  group by i.inclusion_rule_id
) T on ir.sequence = T.inclusion_rule_id
CROSS JOIN (select count(*) as total_rules from #inclusionRules where study_id = @studyId) RuleTotal
CROSS JOIN (select count(event_id) as total from #primary_events) EventTotal
LEFT JOIN @ohdsi_database_schema.feas_study_result SR on SR.study_id = @studyId AND (POWER(cast(2 as bigint),RuleTotal.total_rules) - POWER(cast(2 as bigint),ir.sequence) - 1) = SR.inclusion_rule_mask -- POWER(2,rule count) - POWER(2,rule sequence) - 1 is the mask for 'all except this rule' 
WHERE ir.study_id = @studyId
;

-- calculate totals
delete from @ohdsi_database_schema.feas_study_index_stats where study_id = @studyId;
insert into @ohdsi_database_schema.feas_study_index_stats (study_id, person_count, match_count)
select @studyId as study_id, 
(select count(event_id) as total from #primary_events) as person_count,
coalesce((
  select sr.person_count 
  from @ohdsi_database_schema.feas_study_result sr
  CROSS JOIN (select count(*) as total_rules from #inclusionRules where study_id = @studyId) RuleTotal
  where study_id = @studyId and sr.inclusion_rule_mask = POWER(cast(2 as bigint),RuleTotal.total_rules)-1
),0) as match_count
;

TRUNCATE TABLE #inclusionRuleCohorts;
DROP TABLE #inclusionRuleCohorts;

TRUNCATE TABLE #primary_events;
DROP TABLE #primary_events;

TRUNCATE TABLE #Codesets;
DROP TABLE #Codesets;
