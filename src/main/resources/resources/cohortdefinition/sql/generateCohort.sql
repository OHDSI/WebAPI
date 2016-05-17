@codesetQuery
@primaryEventsQuery

SELECT event_id, person_id, start_date, end_date, op_start_date, op_end_date
INTO #cohort_candidate
FROM 
(
  select RawEvents.*, row_number() over (partition by RawEvents.person_id order by RawEvents.start_date @EventSort) as ordinal
  FROM
  (
    select pe.event_id, pe.person_id, pe.start_date, pe.end_date, pe.op_start_date, pe.op_end_date
    FROM #PrimaryCriteriaEvents pe
    @additionalCriteriaQuery
  ) RawEvents
) Results
@ResultLimitFilter
;

create table #inclusionRuleCohorts 
(
  inclusion_rule_id bigint,
  event_id bigint
)
;
@inclusionCohortInserts

-- the matching group with all bits set ( POWER(2,# of inclusion rules) - 1 = inclusion_rule_mask
DELETE FROM @target_database_schema.@target_cohort_table where cohort_definition_id = @target_cohort_id;
INSERT INTO @target_database_schema.@target_cohort_table (cohort_definition_id, subject_id, cohort_start_date, cohort_end_date)
select @target_cohort_id as cohort_definition_id, MG.person_id, MG.start_date, MG.end_date
from
(
  select C.event_id, C.person_id, C.start_date, C.end_date, SUM(coalesce(POWER(cast(2 as bigint), I.inclusion_rule_id), 0)) as inclusion_rule_mask
  from #cohort_candidate C
  LEFT JOIN #inclusionRuleCohorts I on I.event_id = C.event_id
  GROUP BY C.event_id, C.person_id, C.start_date, C.end_date
) MG -- matching groups
{@ruleTotal != 0}?{
WHERE (MG.inclusion_rule_mask = POWER(cast(2 as bigint),@ruleTotal)-1)
}
;

-- calculte matching group counts
delete from @results_database_schema.cohort_inclusion_result where cohort_definition_id = @target_cohort_id;
insert into @results_database_schema.cohort_inclusion_result (cohort_definition_id, inclusion_rule_mask, person_count)
select @target_cohort_id as cohort_definition_id, inclusion_rule_mask, count(*) as person_count
from
(
  select C.event_id, SUM(coalesce(POWER(cast(2 as bigint), I.inclusion_rule_id), 0)) as inclusion_rule_mask
  from #cohort_candidate C
  LEFT JOIN #inclusionRuleCohorts I on c.event_id = i.event_id
  GROUP BY C.event_id
) MG -- matching groups
group by inclusion_rule_mask
;

-- calculate gain counts
delete from @results_database_schema.cohort_inclusion_stats where cohort_definition_id = @target_cohort_id;
insert into @results_database_schema.cohort_inclusion_stats (cohort_definition_id, rule_sequence, person_count, gain_count, person_total)
select ir.cohort_definition_id, ir.rule_sequence, coalesce(T.person_count, 0) as person_count, coalesce(SR.person_count, 0) gain_count, EventTotal.total
from @results_database_schema.cohort_inclusion ir
left join
(
  select i.inclusion_rule_id, count(i.event_id) as person_count
  from #cohort_candidate C
  JOIN #inclusionRuleCohorts i on C.event_id = i.event_id
  group by i.inclusion_rule_id
) T on ir.rule_sequence = T.inclusion_rule_id
CROSS JOIN (select count(*) as total_rules from @results_database_schema.cohort_inclusion where cohort_definition_id = @target_cohort_id) RuleTotal
CROSS JOIN (select count(event_id) as total from #cohort_candidate) EventTotal
LEFT JOIN @results_database_schema.cohort_inclusion_result SR on SR.cohort_definition_id = @target_cohort_id AND (POWER(cast(2 as bigint),RuleTotal.total_rules) - POWER(cast(2 as bigint),ir.rule_sequence) - 1) = SR.inclusion_rule_mask -- POWER(2,rule count) - POWER(2,rule sequence) - 1 is the mask for 'all except this rule' 
WHERE ir.cohort_definition_id = @target_cohort_id
;

-- calculate totals
delete from @results_database_schema.cohort_summary_stats where cohort_definition_id = @target_cohort_id;
insert into @results_database_schema.cohort_summary_stats (cohort_definition_id, base_count, final_count)
select @target_cohort_id as cohort_definition_id, 
(select count(event_id) as total from #cohort_candidate) as person_count,
coalesce((
  select sr.person_count 
  from @results_database_schema.cohort_inclusion_result sr
  CROSS JOIN (select count(*) as total_rules from @results_database_schema.cohort_inclusion where cohort_definition_id = @target_cohort_id) RuleTotal
  where cohort_definition_id = @target_cohort_id and sr.inclusion_rule_mask = POWER(cast(2 as bigint),RuleTotal.total_rules)-1
),0) as final_count
;

TRUNCATE TABLE #inclusionRuleCohorts;
DROP TABLE #inclusionRuleCohorts;

TRUNCATE TABLE #PrimaryCriteriaEvents;
DROP TABLE #PrimaryCriteriaEvents;

TRUNCATE TABLE #Codesets;
DROP TABLE #Codesets;
